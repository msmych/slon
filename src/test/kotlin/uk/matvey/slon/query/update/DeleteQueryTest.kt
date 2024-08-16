package uk.matvey.slon.query.update

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import uk.matvey.slon.TestContainersSetup
import uk.matvey.slon.param.TextParam.Companion.text
import uk.matvey.slon.param.UuidParam.Companion.uuid
import uk.matvey.slon.repo.Repo
import uk.matvey.slon.repo.RepoKit.deleteFrom
import uk.matvey.slon.repo.RepoKit.executePlain
import uk.matvey.slon.repo.RepoKit.insertInto
import uk.matvey.slon.repo.RepoKit.queryOneNullable
import java.util.UUID.randomUUID

class DeleteQueryTest : TestContainersSetup() {

    @Test
    fun `should delete records`() = runTest {
        // given
        val id = randomUUID()

        repo.insertInto("delete_query_test") {
            values(
                "id" to uuid(id),
                "name" to text(randomUUID().toString())
            )
        }

        // when
        repo.deleteFrom("delete_query_test", "id = ?", uuid(id))

        // then
        val result = repo.queryOneNullable("select * from delete_query_test where id = ?", listOf(uuid(id))) {}
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