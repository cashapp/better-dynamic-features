// THIS IS COPIED FROM AGP (with some modifications)
// https://android.googlesource.com/platform/tools/base/+/refs/tags/studio-2022.1.1/build-system/gradle-core/src/main/java/com/android/build/gradle/internal/tasks/DexFileDependenciesTask.kt

/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.android.build.gradle.internal.tasks

import com.android.build.api.variant.impl.getFeatureLevel
import com.android.build.gradle.internal.component.ConsumableCreationConfig
import com.android.build.gradle.internal.errors.MessageReceiverImpl
import com.android.build.gradle.internal.profile.ProfileAwareWorkAction
import com.android.build.gradle.internal.publishing.AndroidArtifacts
import com.android.build.gradle.internal.scope.InternalArtifactType
import com.android.build.gradle.internal.tasks.factory.VariantTaskCreationAction
import com.android.build.gradle.internal.utils.getDesugarLibConfig
import com.android.build.gradle.internal.utils.setDisallowChanges
import com.android.build.gradle.options.SyncOptions
import com.android.builder.dexing.ClassFileInputs
import com.android.builder.dexing.DexArchiveBuilder
import com.android.builder.dexing.DexParameters
import com.android.builder.dexing.r8.ClassFileProviderFactory
import com.android.sdklib.AndroidVersion
import com.google.common.io.Closer
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.CompileClasspath
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskProvider
import java.io.File

@CacheableTask
@BuildAnalyzer(primaryTaskCategory = TaskCategory.DEXING)
abstract class DexFileDependenciesTask: NonIncrementalTask() {

  @get:OutputDirectory
  abstract val outputDirectory: DirectoryProperty

  // https://issuetracker.google.com/issues/266780231
  @get:Optional
  @get:OutputDirectory
  abstract val outputKeepRules: DirectoryProperty

  @get:Classpath
  abstract val classes: ConfigurableFileCollection

  @get:CompileClasspath
  abstract val classpath: ConfigurableFileCollection

  @get:CompileClasspath
  abstract val bootClasspath: ConfigurableFileCollection

  @get:Input
  abstract val minSdkVersion: Property<Int>

  @get:Input
  abstract val debuggable: Property<Boolean>

  @get:Optional
  @get:Input
  abstract val libConfiguration: Property<String>

  private lateinit var errorFormatMode: SyncOptions.ErrorFormatMode

  /**
   * When a lateinit property is set from within another class, the Kotlin compiler generates a synthetic
   * setter method. This property was referenced in [CreationAction] which was defined below, but
   * since we have it commented out the compiler stopped generating a synthetic setter while compiling
   * our monkey-patch, which results in a NoSuchMethodError at runtime.
   *
   * To work around this, we define a random class with a random method that accesses the lateinit property.
   * This forces the compiler to generate that setter.
   */
  class CompilerCrime {
    fun ohMyGod(task: DexFileDependenciesTask) {
      task.errorFormatMode = SyncOptions.ErrorFormatMode.HUMAN_READABLE
    }
  }

  // TODO: make incremental
  override fun doTaskAction() {
    val inputs = classes.files.toList()
    val totalClasspath = inputs + classpath.files

    inputs.forEachIndexed { index, input ->
      // Desugar each jar with reference to all the others
      workerExecutor.noIsolation().submit(DexFileDependenciesWorkerAction::class.java) {
        it.initializeFromAndroidVariantTask(this)
        it.minSdkVersion.set(minSdkVersion)
        it.debuggable.set(debuggable)
        it.bootClasspath.from(bootClasspath)
        it.classpath.from(totalClasspath)
        it.input.set(input)
        it.outputFile.set(outputDirectory.dir("${index}_${input.name}"))
        it.errorFormatMode.set(errorFormatMode)
        it.libConfiguration.set(libConfiguration)
        it.outputKeepRules.set(outputKeepRules.dir("${index}_${input.name}"))
      }
    }
  }

  abstract class WorkerActionParams: ProfileAwareWorkAction.Parameters() {
    abstract val minSdkVersion: Property<Int>
    abstract val debuggable: Property<Boolean>
    abstract val bootClasspath: ConfigurableFileCollection
    abstract val classpath: ConfigurableFileCollection
    abstract val input: Property<File>
    abstract val outputFile: DirectoryProperty
    abstract val errorFormatMode: Property<SyncOptions.ErrorFormatMode>
    abstract val libConfiguration: Property<String>
    abstract val outputKeepRules: DirectoryProperty
  }

  abstract class DexFileDependenciesWorkerAction : ProfileAwareWorkAction<WorkerActionParams>() {

    override fun run() {
      val bootClasspath = parameters.bootClasspath.map(File::toPath)
      val classpath = parameters.classpath.map(File::toPath)
      Closer.create().use { closer ->
        val d8DexBuilder = DexArchiveBuilder.createD8DexBuilder(
          DexParameters(
            minSdkVersion = parameters.minSdkVersion.get(),
            debuggable = parameters.debuggable.get(),
            dexPerClass = false,
            withDesugaring = true,
            desugarBootclasspath = ClassFileProviderFactory(bootClasspath).also {
              closer.register(it)
            },
            desugarClasspath = ClassFileProviderFactory(classpath).also {
              closer.register(it)
            },
            coreLibDesugarConfig = parameters.libConfiguration.orNull,
            coreLibDesugarOutputKeepRuleFile = parameters.outputKeepRules.asFile.orNull,
            messageReceiver = MessageReceiverImpl(
              errorFormatMode = parameters.errorFormatMode.get(),
              logger = Logging.getLogger(DexFileDependenciesWorkerAction::class.java)
            )
          )
        )


        ClassFileInputs.fromPath(parameters.input.get().toPath()).use { classFileInput ->
          classFileInput.entries { _, _ -> true }.use { classesInput ->
            d8DexBuilder.convert(
              classesInput,
              parameters.outputFile.asFile.get().toPath()
            )
          }
        }
      }
    }
  }

  /**
   * We don't monkey-patch this class because `creationConfig.artifacts.setInitialProvider()` is
   * an `internal` method that we can't access. Rather than having to monkey-patch a whole chain of
   * other internal classes and methods, we can just omit this class and the original AGP version
   * will be loaded at runtime.
   *
   * This requires some crimes though. See [CompilerCrime].
   */
  /*class CreationAction(creationConfig: ConsumableCreationConfig) :
    VariantTaskCreationAction<DexFileDependenciesTask, ConsumableCreationConfig>(
      creationConfig
    ) {
    override val name: String = computeTaskName("desugar", "FileDependencies")
    override val type = DexFileDependenciesTask::class.java

    override fun handleProvider(
      taskProvider: TaskProvider<DexFileDependenciesTask>
    ) {
      super.handleProvider(taskProvider)
      creationConfig.artifacts.setInitialProvider(
        taskProvider,
        DexFileDependenciesTask::outputDirectory
      ).on(InternalArtifactType.EXTERNAL_FILE_LIB_DEX_ARCHIVES)

      if (creationConfig.needsShrinkDesugarLibrary) {
        creationConfig.artifacts
          .setInitialProvider(taskProvider, DexFileDependenciesTask::outputKeepRules)
          .on(InternalArtifactType.DESUGAR_LIB_EXTERNAL_FILE_LIB_KEEP_RULES)
      }
    }

    override fun configure(
      task: DexFileDependenciesTask
    ) {
      super.configure(task)
      task.debuggable
        .setDisallowChanges(creationConfig.debuggable)
      task.classes.from(
        creationConfig.variantDependencies.getArtifactFileCollection(
          AndroidArtifacts.ConsumedConfigType.RUNTIME_CLASSPATH,
          AndroidArtifacts.ArtifactScope.FILE,
          AndroidArtifacts.ArtifactType.PROCESSED_JAR
        )
      ).disallowChanges()
      val minSdkVersionForDexing = creationConfig.minSdkVersionForDexing.getFeatureLevel()
      task.minSdkVersion.setDisallowChanges(minSdkVersionForDexing)

      // If min sdk version for dexing is >= N(24) then we can avoid adding extra classes to
      // the desugar classpaths.
      if (minSdkVersionForDexing < AndroidVersion.VersionCodes.N) {
        task.classpath.from(
          creationConfig.variantDependencies.getArtifactFileCollection(
            AndroidArtifacts.ConsumedConfigType.RUNTIME_CLASSPATH,
            AndroidArtifacts.ArtifactScope.REPOSITORY_MODULE,
            AndroidArtifacts.ArtifactType.PROCESSED_JAR
          )
        )
        task.bootClasspath.from(creationConfig.global.bootClasspath)
      }

      task.errorFormatMode =
        SyncOptions.getErrorFormatMode(creationConfig.services.projectOptions)

      if (creationConfig.isCoreLibraryDesugaringEnabled) {
        task.libConfiguration.set(getDesugarLibConfig(creationConfig.services))
        task.bootClasspath.from(creationConfig.global.bootClasspath)
      }

      task.classpath.disallowChanges()
      task.bootClasspath.disallowChanges()
      task.libConfiguration.disallowChanges()
    }
  }*/
}
