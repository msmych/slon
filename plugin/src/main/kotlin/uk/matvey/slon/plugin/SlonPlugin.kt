package uk.matvey.slon.plugin

import org.gradle.api.Plugin
import org.gradle.api.Project

abstract class SlonPlugin: Plugin<Project> {

    override fun apply(project: Project) {
        val extension = project.extensions.create("slon", SlonExtension::class.java)
        val taskProvider = project.tasks.register("generateJooq", GenerateJooqTask::class.java)

        taskProvider.configure {
            it.group = "build"
            it.description = "Generates jOOQ classes based on Flyway migrations"

            it.imageName.set(extension.imageName)
            it.inputSchema.set(extension.inputSchema)
            it.flywayDir.set(extension.flywayDir)
            it.jooqDir.set(extension.jooqDir)
            it.packageName.set(extension.packageName)
            it.forcedTypes.set(extension.forcedTypes)
        }
    }
}