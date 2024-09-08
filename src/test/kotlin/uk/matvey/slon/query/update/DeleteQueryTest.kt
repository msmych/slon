package uk.matvey.slon.query.update

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import uk.matvey.kit.random.RandomKit.randomAlphabetic
import uk.matvey.slon.TestContainersSetup
import uk.matvey.slon.value.PgText.Companion.toPgText
import uk.matvey.slon.value.PgUuid.Companion.toPgUuid
import uk.matvey.slon.repo.Repo
import uk.matvey.slon.repo.RepoKit.deleteFrom
import uk.matvey.slon.repo.RepoKit.executePlain
import uk.matvey.slon.repo.RepoKit.insertInto
import uk.matvey.slon.repo.RepoKit.queryOneOrNull
import java.util.UUID.randomUUID

class DeleteQueryTest : TestContainersSetup() {

    @Test
    fun `should delete records`() = runTest {
        // given
        val id = randomUUID()

        repo.insertInto("delete_query_test") {
            values(
                "id" to id.toPgUuid(),
                "name" to randomAlphabetic().toPgText(),
            )
        }

        // when
        repo.deleteFrom("delete_query_test", "id = ?", id.toPgUuid())

        // then
        val result = repo.queryOneOrNull("select * from delete_query_test where id = ?", listOf(id.toPgUuid())) {}
        assertThat(result).isNull()
    }

    companion object {

        private lateinit var repo: Repo

        @BeforeAll
        @JvmStatic
        fun initSetup() = runTest {
            repo = Repo(dataSource())
            repo.executePlain(
                """
                create table if not exists delete_query_test (
                    id uuid null,
                    name text null
                )
                """.trimIndent()
            )
        }
    }
}