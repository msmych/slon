package uk.matvey.slon.plugin

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test

class SlonPluginTest {

    @Test
    fun `should register task`() {
        // given
        val project = ProjectBuilder.builder().build()

        // when
        project.plugins.apply("uk.matvey.slon")

        // then
        assertThat(project.tasks.findByName("generateJooq")).isNotNull
    }
}