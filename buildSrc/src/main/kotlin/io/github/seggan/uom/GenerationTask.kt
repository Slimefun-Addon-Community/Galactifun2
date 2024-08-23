package io.github.seggan.uom

import com.squareup.kotlinpoet.FileSpec
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

open class GenerationTask : DefaultTask() {

    @OutputDirectory
    val outputDir: File = project.layout.buildDirectory.dir("generated/uom").get().asFile

    @TaskAction
    fun generate() {
        val config = project.extensions.getByType(UomConfig::class.java)

        val packagePath = config.pkg.replace(".", "/")
        val file = outputDir.resolve("$packagePath/Uom.kt")

        val fileBuilder = FileSpec.builder(config.pkg, "Uom")
        for (measure in config.measures) {
            config.generateUomClassForMeasure(measure, fileBuilder)
        }

        fileBuilder.build().writeTo(file)
    }
}