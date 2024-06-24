package uk.matvey.slon.query.update

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import uk.matvey.slon.Repo
import uk.matvey.slon.TestContainersSetup
import uk.matvey.slon.exception.OptimisticLockException
import uk.matvey.slon.param.TextParam.Companion.text
import uk.matvey.slon.param.UuidParam.Companion.uuid
import uk.matvey.slon.query.update.DeleteQuery.Builder.Companion.deleteFrom
import uk.matvey.slon.query.update.RawUpdateQuery.Companion.rawUpdate
import uk.matvey.slon.query.update.UpdateQuery.Builder.Companion.update
import java.util.UUID.randomUUID

class OptimisticUpdateQueryTest : TestContainersSetup() {

    @Test
    fun `should throw optimistic lock exception for update`() {
        // when / then
        assertThatThrownBy {
            repo.access { a ->
                a.execute(
                    update("optimistic_update_query_test")
                        .set("name", text("New Name"))
                        .where("id = ?", uuid(randomUUID()))
                        .optimistic()
                )
            }
        }
            .isInstanceOf(OptimisticLockException::class.java)
            .hasMessage("Condition was not satisfied")
    }

    @Test
    fun `should throw optimistic lock exception for delete`() {
        // when / then
        assertThatThrownBy {
            repo.access { a ->
                a.execute(
                    deleteFrom("optimistic_update_query_test")
                        .where("id = ?", uuid(randomUUID()))
                        .optimistic()
                )
            }
        }
            .isInstanceOf(OptimisticLockException::class.java)
            .hasMessage("Condition was not satisfied")
    }

    companion object {

        private lateinit var repo: Repo

        @BeforeAll
        @JvmStatic
        fun initSetup() {
            repo = Repo(dataSource())
            repo.access { a ->
                a.execute(
                    rawUpdate(
                        """
                create table if not exists optimistic_update_query_test (
                    id uuid null,
                    name text null,
                    created_at timestamp null
                )
                """.trimIndent()
                    )
                )
            }
        }
    }
}