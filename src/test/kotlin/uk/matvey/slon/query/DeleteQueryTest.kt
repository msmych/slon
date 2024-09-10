package uk.matvey.slon.query

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import uk.matvey.kit.random.RandomKit.randomAlphabetic
import uk.matvey.slon.TestContainersSetup
import uk.matvey.slon.access.AccessKit.queryOneOrNull
import uk.matvey.slon.access.AccessKit.update
import uk.matvey.slon.query.DeleteQueryBuilder.Companion.deleteFrom
import uk.matvey.slon.query.InsertOneQueryBuilder.Companion.insertOneInto
import uk.matvey.slon.repo.Repo
import uk.matvey.slon.value.PgUuid.Companion.toPgUuid
import java.util.UUID.randomUUID

class DeleteQueryTest : TestContainersSetup() {

    @Test
    fun `should delete records`() = runTest {
        // given
        val id = randomUUID()

        repo.access { a ->
            a.execute(
                insertOneInto("delete_query_test") {
                    set("id", id)
                    set("name", randomAlphabetic())
                }
            )
        }

        // when
        repo.access { a ->
            a.execute(
                deleteFrom("delete_query_test")
                    .where("id = ?", id.toPgUuid())
            )
        }

        // then
        val result = repo.access { a ->
            a.queryOneOrNull("select * from delete_query_test where id = ?", listOf(id.toPgUuid())) { }
        }
        assertThat(result).isNull()
    }

    companion object {

        private lateinit var repo: Repo

        @BeforeAll
        @JvmStatic
        fun initSetup() = runTest {
            repo = Repo(dataSource())
            repo.access { a ->
                a.update(
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
}