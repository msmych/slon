package uk.matvey.slon.query.update

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import uk.matvey.slon.Repo
import uk.matvey.slon.TestContainersSetup
import uk.matvey.slon.param.TextParam.Companion.text
import uk.matvey.slon.param.UuidParam.Companion.uuid
import uk.matvey.slon.query.update.UpdateQuery.Builder.Companion.update
import java.util.UUID.randomUUID

class UpdateQueryTest : TestContainersSetup() {

    @Test
    fun `should update records`() {
        // given
        val id = randomUUID()
        val name = "name"

        val newName = randomUUID().toString()
        repo.insertOne("update_query_test", "id" to uuid(id), "name" to text(name))

        // when
        repo.execute(
            update("update_query_test")
                .set("name", text(newName))
                .where("id = ?", uuid(id))
        )

        // then
        repo.queryOne("select * from update_query_test where id = ?", listOf(uuid(id))) { r ->
            assertThat(r.string("name")).isEqualTo(newName)
        }
    }

    companion object {

        private lateinit var repo: Repo

        @BeforeAll
        @JvmStatic
        fun initSetup() {
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