package uk.matvey.slon.query.update

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.matvey.slon.TestContainersSetup
import uk.matvey.slon.exception.OptimisticLockException
import uk.matvey.slon.param.TextParam.Companion.text
import uk.matvey.slon.param.UuidParam.Companion.uuid
import uk.matvey.slon.query.update.DeleteQueryBuilder.Companion.deleteFrom
import uk.matvey.slon.query.update.UpdateQueryBuilder.Companion.update
import uk.matvey.slon.repo.Repo
import uk.matvey.slon.repo.RepoKit.execute
import uk.matvey.slon.repo.RepoKit.executePlain
import java.util.UUID.randomUUID

class RequireSingleUpdateQueryTest : TestContainersSetup() {

    @Test
    fun `should throw optimistic lock exception for update`() = runTest {
        // when / then
        val exception = assertThrows<OptimisticLockException> {
            repo.execute(
                update("optimistic_update_query_test")
                    .set("name", text("New Name"))
                    .where("id = ?", uuid(randomUUID()))
                    .requireSingleUpdate()
            )
        }
        assertThat(exception.message).isEqualTo("Condition was not satisfied")
    }

    @Test
    fun `should throw optimistic lock exception for delete`() = runTest {
        // when / then
        val exception = assertThrows<OptimisticLockException> {
            repo.execute(
                deleteFrom("optimistic_update_query_test")
                    .where("id = ?", uuid(randomUUID()))
                    .requireSingleUpdate()
            )
        }
        assertThat(exception.message).isEqualTo("Condition was not satisfied")
    }

    companion object {

        private lateinit var repo: Repo

        @BeforeAll
        @JvmStatic
        fun initSetup() = runTest {
            repo = Repo(dataSource())
            repo.executePlain(
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