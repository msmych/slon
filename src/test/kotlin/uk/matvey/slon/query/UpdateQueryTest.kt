package uk.matvey.slon.query

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import uk.matvey.kit.random.RandomKit.randomAlphabetic
import uk.matvey.slon.TestContainersSetup
import uk.matvey.slon.access.AccessKit.queryAll
import uk.matvey.slon.access.AccessKit.update
import uk.matvey.slon.query.InsertOneQueryBuilder.Companion.insertOneInto
import uk.matvey.slon.query.UpdateQueryBuilder.Companion.update
import uk.matvey.slon.repo.Repo
import uk.matvey.slon.value.PgUuid.Companion.toPgUuid
import java.util.UUID.randomUUID

class UpdateQueryTest : TestContainersSetup() {

    @Test
    fun `should update records`() = runTest {
        // given
        val id = randomUUID()
        val name = "name"

        val newName = randomAlphabetic()
        repo.access { a ->
            a.execute(
                insertOneInto("update_query_test") {
                    set("id", id)
                    set("name", name)
                }
            )
        }

        // when
        repo.access { a ->
            a.execute(
                update("update_query_test") {
                    set("name", newName)
                    where("id = ?", id.toPgUuid())
                }
            )
        }

        // then
        repo.access { a ->
            a.queryAll(
                "select * from update_query_test where id = ?",
                listOf(id.toPgUuid()),
            ) { r ->
                assertThat(r.string("name")).isEqualTo(newName)
            }
        }
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
}