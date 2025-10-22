package uk.matvey.slon.plugin

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File

class GenerateJooqTaskTest {

    @field:TempDir
    lateinit var projectDir: File

    private val buildFile by lazy { projectDir.resolve("build.gradle.kts") }
    private val settingsFile by lazy { projectDir.resolve("build.gradle.kts") }

    @Test
    fun `should generate jOOQ classes`() {
        // given
        settingsFile.writeText("")
        buildFile.writeText(
            """
            plugins {
                id("uk.matvey.slon")
            }
            
            slon {
                flywayDir.set(file("$projectDir/src/main/resources/db/migration"))
                jooqDir.set(file("$projectDir/src/main/jooq"))
                packageName.set("uk.matvey.generated.jooq")
            }
        """.trimIndent()
        )
        val flyway = File(projectDir, "src/main/resources/db/migration")
        flyway.mkdirs()
        val migration = File(flyway, "V00__accounts.sql")
        migration.writeText("""
            create table if not exists accounts (
                id serial primary key,
                name varchar(20)
            );
        """.trimIndent())
        val jooq = File(projectDir, "src/main/jooq")
        jooq.mkdirs()

        // when
        val result = GradleRunner.create()
            .forwardOutput()
            .withPluginClasspath()
            .withProjectDir(projectDir)
            .withArguments("generateJooq")
            .build()

        // then
        assertThat(result.output).contains("BUILD SUCCESSFUL")
        val accountsFile = File("$projectDir/src/main/jooq/uk/matvey/generated/jooq/tables/Accounts.java")
        assertThat(accountsFile.readText()).contains("public class Accounts")
    }
}