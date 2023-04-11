package app.cash.better.dynamic.features.magic

import com.reandroid.archive.APKArchive
import com.reandroid.archive.FileInputSource
import com.reandroid.arsc.chunk.xml.ResXmlAttribute
import com.reandroid.arsc.chunk.xml.ResXmlDocument
import com.reandroid.arsc.chunk.xml.ResXmlElement
import com.reandroid.arsc.chunk.xml.ResXmlIDMap
import com.reandroid.arsc.container.SingleBlockContainer
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

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

  private val resourceMapping: Map<Int, Int> by lazy {
    resourceMappingFile.asFile.get()
      .readLines()
      .map { it.split(" ") }
      .associate { (_, key, value) -> key.toInt() to value.toInt() }
  }

  @TaskAction
  fun rewrite() {
    when (mode.get()) {
      Mode.BinaryXml -> rewriteBinaryXml()
      Mode.ProtoXml -> logger.warn("Proto XML rewriting is currently unimplemented.")
      null -> {}
    }
  }

  private fun rewriteBinaryXml() {
    val copyFile = temporaryDir.resolve("copy.ap_")
    copyFile.delete()
    val copy = processedResourceArchive.asFile.get().copyTo(copyFile)

    val apkArchive = APKArchive.loadZippedApk(copy)

    temporaryDir.resolve("res/layouts").mkdirs()

    apkArchive.listInputSources().filter { it.name.startsWith("res/layout") }.forEach { source ->
      val document = source.openStream().use { ResXmlDocument().apply { readBytes(it) } }
      processLayoutFile(document)

      val tempFile = temporaryDir.resolve(source.name)
      tempFile.parentFile.mkdirs()
      document.writeBytes(tempFile.outputStream())

      // Replace the entry in the APK
      apkArchive.add(FileInputSource(tempFile, source.name))
    }

    val outputArchive = outputArchive.asFile.get()
    apkArchive.writeApk(outputArchive.outputStream())
  }

  private fun processLayoutFile(xmlDocument: ResXmlDocument) {
    xmlDocument.walkElements { element ->
      element.listAttributes().forEach { attr ->
        println(attr.name)
        // Rewrite attribute IDs
        if (resourceMapping.containsKey(attr.nameResourceID)) {
          val original = attr.nameResourceID

          val index = attr.getResXmlIDMap()!!.getByResId(attr.nameResourceID).index
          attr.getResXmlIDMap()!!.resXmlIDArray.addResourceId(index, resourceMapping.getValue(attr.nameResourceID))
          logger.info("Replaced attribute %s %x with %x".format(attr.name, original, attr.nameResourceID))
        }

        // Rewrite ID IDs (the resource ID of android:id attributes)
        // TODO: This will require rewriting the R class as well
        // if (attr.name == "id" && attr.namePrefix == "android" && resourceMapping.containsKey(attr.data)) {
        //   attr.data = resourceMapping.getValue(attr.data)
        // }
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
