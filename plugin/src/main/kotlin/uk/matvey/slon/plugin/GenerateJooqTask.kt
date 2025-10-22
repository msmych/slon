package uk.matvey.slon.plugin

import org.flywaydb.core.Flyway
import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.jooq.codegen.GenerationTool
import org.jooq.meta.jaxb.Configuration
import org.jooq.meta.jaxb.Database
import org.jooq.meta.jaxb.ForcedType
import org.jooq.meta.jaxb.Generator
import org.jooq.meta.jaxb.Jdbc
import org.jooq.meta.jaxb.Target
import org.testcontainers.containers.PostgreSQLContainer

abstract class GenerateJooqTask : DefaultTask() {

    @get:Input
    @get:Optional
    abstract val imageName: Property<String>

    @get:Input
    @get:Optional
    abstract val inputSchema: Property<String>

    @get:InputDirectory
    abstract val flywayDir: DirectoryProperty

    @get:OutputDirectory
    abstract val jooqDir: DirectoryProperty

    @get:Input
    abstract val packageName: Property<String>

    @get:Input
    abstract val forcedTypes: ListProperty<ForcedType>

    @TaskAction
    fun generateJooq() {
        val dbContainer = PostgreSQLContainer(imageName.getOrElse("postgres:18-alpine"))
            .withDatabaseName("postgres")
            .withUsername("postgres")
            .withPassword("postgres")
        dbContainer.start()
        Flyway.configure()
            .dataSource(dbContainer.jdbcUrl, dbContainer.username, dbContainer.password)
            .locations("filesystem:${flywayDir.get().asFile.absolutePath}")
            .load()
            .migrate()
        GenerationTool.generate(Configuration().apply {
            jdbc = Jdbc().apply {
                driver = "org.postgresql.Driver"
                url = dbContainer.jdbcUrl
                user = dbContainer.username
                password = dbContainer.password
            }
            generator = Generator().apply {
                val db = Database()
                db.name = "org.jooq.meta.postgres.PostgresDatabase"
                db.inputSchema = inputSchema.getOrElse("public")
                db.excludes = "flyway_schema_history"
                db.forcedTypes = forcedTypes.getOrElse(listOf())
                database = db
                val t = Target()
                t.packageName = packageName.get()
                t.directory = jooqDir.get().asFile.absolutePath
                target = t
            }
        })
        dbContainer.stop()
    }
}