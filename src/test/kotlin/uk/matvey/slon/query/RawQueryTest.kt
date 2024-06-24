package uk.matvey.slon.query

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import uk.matvey.slon.Repo
import uk.matvey.slon.TestContainersSetup

class RawQueryTest : TestContainersSetup() {

    @Test
    fun `should execute raw query`() {
        // when
        val result = repo.queryOne("show time zone") { it.string(1) }

        // then
        assertThat(result).isNotBlank()
    }

    companion object {

        private lateinit var repo: Repo

        @BeforeAll
        @JvmStatic
        fun initSetup() {
            repo = Repo(dataSource())
        }
    }
}