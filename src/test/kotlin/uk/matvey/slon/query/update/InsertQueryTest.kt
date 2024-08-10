package uk.matvey.slon.query.update

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import uk.matvey.slon.RecordReader
import uk.matvey.slon.TestContainersSetup
import uk.matvey.slon.param.PlainParam.Companion.genRandomUuid
import uk.matvey.slon.param.PlainParam.Companion.now
import uk.matvey.slon.param.PlainParam.Companion.plainParam
import uk.matvey.slon.param.TextParam.Companion.text
import uk.matvey.slon.param.TimestampParam.Companion.timestamp
import uk.matvey.slon.param.UuidParam.Companion.uuid
import uk.matvey.slon.repo.Repo
import uk.matvey.slon.repo.RepoKit.insertInto
import uk.matvey.slon.repo.RepoKit.query
import uk.matvey.slon.repo.RepoKit.queryOne
import uk.matvey.slon.repo.RepoKit.queryOneNullable
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit.MILLIS
import java.util.UUID
import java.util.UUID.randomUUID

class InsertQueryTest : TestContainersSetup() {

    data class InsertQueryTestRecord(
        val id: UUID,
        val name: String,
        val createdAt: Instant,
    ) {
        companion object {

            fun from(r: RecordReader): InsertQueryTestRecord {
                return InsertQueryTestRecord(
                    id = r.uuid("id"),
                    name = r.string("name"),
                    createdAt = r.instant("created_at")
                )
            }
        }
    }

    @Test
    fun `should insert record`() = runTest {
        // given
        val id = randomUUID()
        val name = randomUUID().toString()
        val createdAt = Instant.now().truncatedTo(MILLIS)

        // when / then
        repo.insertInto("insert_query_test") {
            set(
                "id" to uuid(id),
                "name" to text(name),
                "created_at" to timestamp(createdAt),
            )
        }

        val result = repo.queryOne(
            "select * from insert_query_test where id = ?",
            listOf(uuid(id)),
            InsertQueryTestRecord::from
        )

        assertThat(result.id).isEqualTo(id)
        assertThat(result.name).isEqualTo(name)
        assertThat(result.createdAt).isEqualTo(createdAt)
    }

    @Test
    fun `should insert multiple records`() = runTest {
        // given
        val name = randomUUID().toString()

        // when
        repo.insertInto("insert_query_test") {
            columns("id", "name", "created_at")
            values(uuid(randomUUID()), text(name), now())
            values(uuid(randomUUID()), text(name), plainParam("now() + interval '1 hours'"))
            values(uuid(randomUUID()), text(name), plainParam("now() + interval '2 hours'"))
        }

        // then
        val result = repo.query("select * from insert_query_test where name = ?", listOf(text(name))) { r ->
            assertThat(r.string("name")).isEqualTo(name)
        }
        assertThat(result).hasSize(3)
    }

    @Test
    fun `should support on conflict clause`() = runTest {
        // given
        val createdAt = Instant.now().truncatedTo(MILLIS)
        val name = randomUUID().toString()

        repo.insertInto("insert_query_test") {
            set(
                "id" to genRandomUuid(),
                "name" to text(name),
                "created_at" to timestamp(createdAt)
            )
        }

        // when
        repo.insertInto("insert_query_test") {
            set(
                "id" to genRandomUuid(),
                "name" to text(name),
                "created_at" to timestamp(createdAt)
            )
            onConflict(listOf("created_at"), "update set created_at = excluded.created_at + interval '1 hours'")
        }

        // then
        val result = repo.queryOneNullable("select * from insert_query_test where name = ?", listOf(text(name))) { r ->
            assertThat(r.instant("created_at")).isEqualTo(createdAt.plus(Duration.ofHours(1)))
        }
        assertThat(result).isNotNull
    }

    @Test
    fun `should support on conflict do nothing`() = runTest {
        // given
        val createdAt = Instant.now().truncatedTo(MILLIS)
        val name = randomUUID().toString()

        repo.insertInto("insert_query_test") {
            set(
                "id" to genRandomUuid(),
                "name" to text(name),
                "created_at" to timestamp(createdAt)
            )
        }

        // when
        repo.insertInto("insert_query_test") {
            set(
                "id" to genRandomUuid(),
                "name" to text(name),
                "created_at" to timestamp(createdAt)
            )
            onConflictDoNothing()
        }

        // then
        val result = repo.queryOneNullable("select * from insert_query_test where name = ?", listOf(text(name))) { r ->
            assertThat(r.instant("created_at")).isEqualTo(createdAt)
        }
        assertThat(result).isNotNull
    }

    companion object {

        private lateinit var repo: Repo

        @BeforeAll
        @JvmStatic
        fun initSetup() = runTest {
            repo = Repo(dataSource())
            repo.access { a ->
                a.executePlain(
                    """
                create table if not exists insert_query_test (
                    id uuid null,
                    name text null,
                    created_at timestamp not null
                )
                """.trimIndent()
                )
                a.executePlain(
                    """create unique index if not exists insert_query_test_created_at_idx
                    | on insert_query_test (created_at)""".trimMargin()
                )
            }
        }
    }
}