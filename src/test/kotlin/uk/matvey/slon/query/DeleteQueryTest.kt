package uk.matvey.slon.query

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import uk.matvey.kit.random.RandomKit.randomAlphabetic
import uk.matvey.slon.TestContainersSetup
import uk.matvey.slon.repo.Repo
import uk.matvey.slon.repo.RepoKit.delete
import uk.matvey.slon.repo.RepoKit.insertOneInto
import uk.matvey.slon.repo.RepoKit.plainUpdate
import uk.matvey.slon.repo.RepoKit.queryOneOrNull
import uk.matvey.slon.value.PgUuid.Companion.toPgUuid
import java.util.UUID.randomUUID

class DeleteQueryTest : TestContainersSetup() {

    @Test
    fun `should delete records`() {
        // given
        val id = randomUUID()

        repo.insertOneInto("delete_query_test") {
            set("id", id)
            set("name", randomAlphabetic())
        }

        // when
        repo.delete("delete_query_test", "id = ?", id.toPgUuid())

        // then
        val result = repo.queryOneOrNull("select * from delete_query_test where id = ?", listOf(id.toPgUuid())) { }
        assertThat(result).isNull()
    }

    companion object {

        private lateinit var repo: Repo

        @BeforeAll
        @JvmStatic
        fun initSetup() {
            repo = Repo(dataSource())
            repo.plainUpdate(
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