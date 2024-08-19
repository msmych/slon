package uk.matvey.slon.query

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import uk.matvey.kit.random.RandomKit.randomAlphabetic
import uk.matvey.slon.RecordReader
import uk.matvey.slon.TestContainersSetup
import uk.matvey.slon.param.PlainParam.Companion.genRandomUuid
import uk.matvey.slon.param.PlainParam.Companion.now
import uk.matvey.slon.param.TextParam.Companion.text
import uk.matvey.slon.param.TimestampParam.Companion.timestamp
import uk.matvey.slon.repo.Repo
import uk.matvey.slon.repo.RepoKit.insertInto
import uk.matvey.slon.repo.RepoKit.insertReturningOne
import uk.matvey.slon.repo.RepoKit.insertReturning
import uk.matvey.slon.repo.RepoKit.queryOneOrNull
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit.MILLIS
import java.util.UUID

class InsertReturningQueryTest : TestContainersSetup() {

    data class InsertReturningQueryTestRecord(
        val id: UUID,
        val name: String
    ) {
        companion object {
            fun from(r: RecordReader) = InsertReturningQueryTestRecord(
                id = r.uuid("id"),
                name = r.string("name")
            )
        }
    }

    @Test
    fun `should insert returning`() = runTest {
        // when / then
        repo.insertReturningOne("insert_returning_query_test") {
            values("id" to genRandomUuid(), "created_at" to now())
            returning<Unit>(listOf("id")) { r ->
                assertThat(r.uuidOrNull("id")).isNotNull()
            }
        }
    }

    @Test
    fun `should insert returning all`() = runTest {
        // when / then
        repo.insertReturning("insert_returning_query_test") {
            values("id" to genRandomUuid(), "name" to text(randomAlphabetic()), "created_at" to now())
            returning(InsertReturningQueryTestRecord::from)
        }
    }

    @Test
    fun `should support on conflict clause`() = runTest {
        // given
        val createdAt = Instant.now().truncatedTo(MILLIS)
        val name = randomAlphabetic()

        repo.insertInto("insert_returning_query_test") {
            values(
                "id" to genRandomUuid(),
                "name" to text(name),
                "created_at" to timestamp(createdAt)
            )
        }

        // when
        repo.insertInto("insert_returning_query_test") {
            values(
                "id" to genRandomUuid(),
                "name" to text(name),
                "created_at" to timestamp(createdAt)
            )
            onConflict(listOf("created_at"), "update set created_at = excluded.created_at + interval '1 hours'")
        }

        // then
        val result = repo.queryOneOrNull(
            "select * from insert_returning_query_test where name = ?",
            listOf(text(name))
        ) { r ->
            assertThat(r.instant("created_at")).isEqualTo(createdAt.plus(Duration.ofHours(1)))
        }
        assertThat(result).isNotNull
    }

    @Test
    fun `should support on conflict do nothing`() = runTest {
        // given
        val createdAt = Instant.now().truncatedTo(MILLIS)
        val name = randomAlphabetic()

        repo.insertInto("insert_returning_query_test") {
            values(
                "id" to genRandomUuid(),
                "name" to text(name),
                "created_at" to timestamp(createdAt)
            )
        }

        // when
        repo.insertInto("insert_returning_query_test") {
            values(
                "id" to genRandomUuid(),
                "name" to text(name),
                "created_at" to timestamp(createdAt)
            )
            onConflictDoNothing()
        }

        // then
        val result = repo.queryOneOrNull(
            "select * from insert_returning_query_test where name = ?",
            listOf(text(name))
        ) { r ->
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
                create table if not exists insert_returning_query_test (
                    id uuid null,
                    name text null,
                    created_at timestamp not null
                )
                """.trimIndent()
                )
                a.executePlain(
                    """create unique index if not exists insert_returning_query_test_created_at_idx
                    | on insert_returning_query_test (created_at)""".trimMargin()
                )
            }
        }
    }
}