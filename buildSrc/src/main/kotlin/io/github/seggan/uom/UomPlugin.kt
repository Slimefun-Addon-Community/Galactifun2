package io.github.seggan.uom

import com.squareup.kotlinpoet.FileSpec
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer

class UomPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.add("uom", UomConfig())
        val generateUom = project.tasks.register("generateUom", GenerationTask::class.java)
        val sourceSet = project.extensions.getByType(SourceSetContainer::class.java)
        sourceSet.getByName("main").java.srcDir(generateUom)
    }
}