package uk.matvey.slon.query.update

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import uk.matvey.slon.TestContainersSetup
import uk.matvey.slon.param.TextParam.Companion.text
import uk.matvey.slon.param.UuidParam.Companion.uuid
import uk.matvey.slon.repo.Repo
import uk.matvey.slon.repo.RepoKit.executePlain
import uk.matvey.slon.repo.RepoKit.insertInto
import uk.matvey.slon.repo.RepoKit.queryOne
import uk.matvey.slon.repo.RepoKit.update
import java.util.UUID.randomUUID

class UpdateQueryTest : TestContainersSetup() {

    @Test
    fun `should update records`() = runTest {
        // given
        val id = randomUUID()
        val name = "name"

        val newName = randomUUID().toString()
        repo.insertInto("update_query_test") {
            set(
                "id" to uuid(id),
                "name" to text(name)
            )
        }

        // when
        repo.update("update_query_test") {
            set("name", text(newName))
            where("id = ?", uuid(id))
        }

        // then
        repo.queryOne("select * from update_query_test where id = ?", listOf(uuid(id))) { r ->
            assertThat(r.string("name")).isEqualTo(newName)
        }
    }

    companion object {

        private lateinit var repo: Repo

        @BeforeAll
        @JvmStatic
        fun initSetup() = runTest {
            repo = Repo(dataSource())
            repo.executePlain(
                """
                create table if not exists update_query_test (
                    id uuid null,
                    name text null,
                    created_at timestamp null
                )
                """.trimIndent()
            )
        }
    }
}