package uk.matvey.slon.query

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import uk.matvey.slon.TestContainersSetup
import uk.matvey.slon.repo.Repo
import uk.matvey.slon.repo.RepoKit.queryOne

class RawQueryTest : TestContainersSetup() {

    @Test
    fun `should execute raw query`() = runTest {
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