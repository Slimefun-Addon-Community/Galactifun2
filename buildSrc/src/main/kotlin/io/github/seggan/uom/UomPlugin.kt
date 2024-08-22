package io.github.seggan.uom

import org.gradle.api.Plugin
import org.gradle.api.Project

class UomPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.extensions.add("uom", UomConfig())
    }
}