package uk.matvey.slon.query

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import uk.matvey.slon.InsertBuilder.Companion.insertInto
import uk.matvey.slon.RecordReader
import uk.matvey.slon.Repo
import uk.matvey.slon.TestContainersSetup
import uk.matvey.slon.param.PlainParam.Companion.genRandomUuid
import uk.matvey.slon.param.PlainParam.Companion.now
import uk.matvey.slon.param.TextParam.Companion.text
import uk.matvey.slon.param.TimestampParam.Companion.timestamp
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit.MILLIS
import java.util.UUID
import java.util.UUID.randomUUID

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
    fun `should insert returning`() {
        // when
        val result = repo.execute(
            insertInto("insert_returning_query_test")
                .set("id" to genRandomUuid(), "created_at" to now())
                .returning(listOf("id")) { r -> r.uuid("id") }
        )

        // then
        assertThat(result).hasSize(1)
    }

    @Test
    fun `should insert returning all`() {
        // when
        val result = repo.execute(
            insertInto("insert_returning_query_test")
                .set("id" to genRandomUuid(), "name" to text(randomUUID().toString()), "created_at" to now())
                .returning(InsertReturningQueryTestRecord::from)
        )

        // then
        assertThat(result).hasSize(1)
    }

    @Test
    fun `should support on conflict clause`() {
        // given
        val createdAt = Instant.now().truncatedTo(MILLIS)
        val name = randomUUID().toString()

        repo.insertOne(
            "insert_returning_query_test",
            "id" to genRandomUuid(),
            "name" to text(name),
            "created_at" to timestamp(createdAt)
        )

        // when
        repo.execute(
            insertInto("insert_returning_query_test")
                .set(
                    "id" to genRandomUuid(),
                    "name" to text(name),
                    "created_at" to timestamp(createdAt)
                )
                .onConflict("(created_at) do update set created_at = excluded.created_at + interval '1 hours'")
                .build()
        )

        // then
        val result = repo.queryOneNullable(
            "select * from insert_returning_query_test where name = ?",
            listOf(text(name))
        ) { r ->
            assertThat(r.instant("created_at")).isEqualTo(createdAt.plus(Duration.ofHours(1)))
        }
        assertThat(result).isNotNull
    }

    @Test
    fun `should support on conflict do nothing`() {
        // given
        val createdAt = Instant.now().truncatedTo(MILLIS)
        val name = randomUUID().toString()

        repo.insertOne(
            "insert_returning_query_test",
            "id" to genRandomUuid(),
            "name" to text(name),
            "created_at" to timestamp(createdAt)
        )

        // when
        repo.execute(
            insertInto("insert_returning_query_test")
                .set(
                    "id" to genRandomUuid(),
                    "name" to text(name),
                    "created_at" to timestamp(createdAt)
                )
                .onConflictDoNothing()
                .build()
        )

        // then
        val result = repo.queryOneNullable(
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
        fun initSetup() {
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