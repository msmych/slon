package uk.matvey.slon.query

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.matvey.slon.TestContainersSetup
import uk.matvey.slon.access.AccessKit.update
import uk.matvey.slon.access.AccessKit.updateSingle
import uk.matvey.slon.exception.UpdateCountMismatchException
import uk.matvey.slon.query.DeleteQueryBuilder.Companion.deleteFrom
import uk.matvey.slon.query.UpdateQueryBuilder.Companion.update
import uk.matvey.slon.repo.Repo
import uk.matvey.slon.value.PgUuid.Companion.toPgUuid
import java.util.UUID.randomUUID

class RequireSingleUpdateQueryTest : TestContainersSetup() {

    @Test
    fun `should throw optimistic lock exception for update`() = runTest {
        // when / then
        val exception = assertThrows<UpdateCountMismatchException> {
            repo.access { a ->
                a.updateSingle(
                    update("optimistic_update_query_test") {
                        set("name", "New Name")
                        where("id = ?", randomUUID().toPgUuid())
                    }
                )
            }
        }
        assertThat(exception.message).isEqualTo("Expected 1 updates but got 0")
    }

    @Test
    fun `should throw optimistic lock exception for delete`() = runTest {
        // when / then
        val exception = assertThrows<UpdateCountMismatchException> {
            repo.access { a ->
                a.updateSingle(
                    deleteFrom("optimistic_update_query_test")
                        .where("id = ?", randomUUID().toPgUuid())
                )
            }
        }
        assertThat(exception.message).isEqualTo("Expected 1 updates but got 0")
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
                create table if not exists optimistic_update_query_test (
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