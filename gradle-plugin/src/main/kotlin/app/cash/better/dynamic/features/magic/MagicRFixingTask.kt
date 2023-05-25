/*
 * Copyright (C) 2023 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package app.cash.better.dynamic.features.magic

import com.android.aapt.XmlElement
import com.android.aapt.XmlNode
import com.reandroid.archive.APKArchive
import com.reandroid.archive.FileInputSource
import com.reandroid.arsc.chunk.xml.ResXmlAttribute
import com.reandroid.arsc.chunk.xml.ResXmlDocument
import com.reandroid.arsc.chunk.xml.ResXmlElement
import com.reandroid.arsc.chunk.xml.ResXmlIDMap
import com.reandroid.arsc.container.SingleBlockContainer
import com.reandroid.arsc.value.ValueType
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.nio.file.FileSystems
import java.nio.file.Files
import kotlin.io.path.inputStream
import kotlin.io.path.isDirectory
import kotlin.io.path.outputStream

/**
 * This task rewrites the compiled layout resource files to use the correct `attr` and `id` references.
 */
@AaptMagic
abstract class MagicRFixingTask : DefaultTask() {
  @get:InputFile
  abstract val resourceMappingFile: RegularFileProperty

  /**
   * the intermediate `.ap_` file
   */
  @get:InputFile
  abstract val processedResourceArchive: RegularFileProperty

  @get:OutputFile
  abstract val outputArchive: RegularFileProperty

  @get:Input
  abstract val mode: Property<Mode>

  private fun readResourceMapping(): Map<Int, Int> =
    resourceMappingFile.asFile.get()
      .readLines()
      .map { it.split(" ") }
      .associate { (_, key, value) -> key.toInt() to value.toInt() }

  @TaskAction
  fun rewrite() {
    val resourceMapping = readResourceMapping()
    when (mode.get()) {
      Mode.BinaryXml -> rewriteBinaryXml(resourceMapping)
      Mode.ProtoXml -> rewriteProtoXml(resourceMapping)
      null -> {}
    }
  }

  private fun rewriteProtoXml(resourceMapping: Map<Int, Int>) {
    val copyFile = temporaryDir.resolve("copy.ap_")
    copyFile.delete()
    val copy = processedResourceArchive.asFile.get().copyTo(copyFile)

    val zip = FileSystems.newFileSystem(copy.toPath(), this::class.java.classLoader)
    Files.walk(zip.getPath("")).filter {
      it.startsWith(zip.getPath("res", "layout")) && !it.isDirectory()
    }.forEach {
      val rootNode = it.inputStream().use { input -> XmlNode.ADAPTER.decode(input) }
      val result = processProtoFile(rootNode, resourceMapping)

      it.outputStream().use { output -> XmlNode.ADAPTER.encode(output, result) }
    }

    zip.close()
    copyFile.copyTo(outputArchive.get().asFile, overwrite = true)
  }

  private fun processProtoFile(root: XmlNode, resourceMapping: Map<Int, Int>): XmlNode {
    val mappedXmlNode = root.walkElements { element ->
      val mappedAttributes = element.attribute.map { attr ->
        if (resourceMapping.containsKey(attr.resource_id)) {
          logger.info("Replaced attribute %s %x with %x".format(attr.name, attr.resource_id, resourceMapping.getValue(attr.resource_id)))
          return@map attr.copy(resource_id = resourceMapping.getValue(attr.resource_id))
        }

        if (attr.compiled_item?.ref?.id != null && resourceMapping.containsKey(attr.compiled_item.ref.id)) {
          logger.info("Updated attr ${attr.name} reference of %x with %x".format(attr.compiled_item.ref.id, resourceMapping.getValue(attr.compiled_item.ref.id)))
          return@map attr.copy(
            compiled_item = attr.compiled_item.copy(
              ref = attr.compiled_item.ref.copy(
                id = resourceMapping.getValue(attr.compiled_item.ref.id),
              ),
            ),
          )
        }

        return@map attr
      }

      element.copy(attribute = mappedAttributes)
    }

    return mappedXmlNode
  }

  private fun XmlNode.walkElements(block: (element: XmlElement) -> XmlElement): XmlNode {
    if (element == null) return this
    val mappedElement = block(element)

    val children = mappedElement.child.toMutableList()
    for (i in 0..children.lastIndex) {
      children[i] = children[i].walkElements(block)
    }

    return this.copy(element = mappedElement.copy(child = children))
  }

  private fun rewriteBinaryXml(resourceMapping: Map<Int, Int>) {
    val copyFile = temporaryDir.resolve("copy.ap_")
    copyFile.delete()
    val copy = processedResourceArchive.asFile.get().copyTo(copyFile)

    val apkArchive = APKArchive.loadZippedApk(copy)

    temporaryDir.resolve("res/layouts").mkdirs()

    apkArchive.listInputSources().filter { it.name.startsWith("res/layout") }.forEach { source ->
      val document = source.openStream().use { ResXmlDocument().apply { readBytes(it) } }
      processLayoutFile(document, resourceMapping)

      val tempFile = temporaryDir.resolve(source.name)
      tempFile.parentFile.mkdirs()
      document.writeBytes(tempFile.outputStream())

      // Replace the entry in the APK
      apkArchive.add(FileInputSource(tempFile, source.name))
    }

    val outputArchive = outputArchive.asFile.get()
    apkArchive.writeApk(outputArchive.outputStream())
  }

  private fun processLayoutFile(xmlDocument: ResXmlDocument, resourceMapping: Map<Int, Int>) {
    xmlDocument.walkElements { element ->
      element.listAttributes().forEach { attr ->
        // Rewrite attribute IDs
        if (resourceMapping.containsKey(attr.nameResourceID)) {
          val original = attr.nameResourceID

          val index = attr.getResXmlIDMap()!!.getByResId(attr.nameResourceID).index
          attr.getResXmlIDMap()!!.resXmlIDArray.addResourceId(index, resourceMapping.getValue(attr.nameResourceID))
          logger.info("Replaced attribute %s %x with %x".format(attr.name, original, attr.nameResourceID))
        }

        if (attr.valueType == ValueType.REFERENCE && resourceMapping.containsKey(attr.data)) {
          logger.info("Updated attr ${attr.fullName} reference of %x with %x".format(attr.data, resourceMapping.getValue(attr.data)))
          attr.setValueAsResourceId(resourceMapping.getValue(attr.data))
        }
      }
    }

    // Recomputes offsets in the binary data with our new ID(s)
    xmlDocument.refresh()
  }

  /**
   * Walk the binary XML tree (starting with the root) for all elements in the document.
   */
  @Suppress("UNCHECKED_CAST")
  private fun ResXmlDocument.walkElements(block: (element: ResXmlElement) -> Unit) {
    val root = (childes.last() as SingleBlockContainer<ResXmlElement>).item
    block(root)

    val queue = ArrayDeque<ResXmlElement>(root.listElements())
    while (queue.isNotEmpty()) {
      val next = queue.removeFirst()
      block(next)

      queue += next.listElements()
    }
  }

  private fun ResXmlAttribute.getResXmlIDMap(): ResXmlIDMap? {
    val xmlElement = this.parentResXmlElement
    return xmlElement?.resXmlIDMap
  }

  enum class Mode {
    BinaryXml,
    ProtoXml;
  }
}
