package uk.matvey.slon.query

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import uk.matvey.kit.random.RandomKit.randomAlphabetic
import uk.matvey.slon.RecordReader
import uk.matvey.slon.TestContainersSetup
import uk.matvey.slon.query.InsertOneQueryBuilder.Companion.insertOneInto
import uk.matvey.slon.query.OnConflictClause.Companion.doNothing
import uk.matvey.slon.query.Query.Companion.plainQuery
import uk.matvey.slon.query.Update.Companion.plainUpdate
import uk.matvey.slon.repo.Repo
import uk.matvey.slon.value.Pg
import uk.matvey.slon.value.Pg.Companion.genRandomUuid
import uk.matvey.slon.value.PgText.Companion.toPgText
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
        repo.access { a ->
            a.query(
                insertOneInto("insert_returning_query_test")
                    .set("id", genRandomUuid())
                    .set("created_at", Pg.now())
                    .returning(listOf("id")) { reader ->
                        assertThat(reader.uuidOrNull("id")).isNotNull()
                    }
            )
        }
    }

    @Test
    fun `should insert returning all`() = runTest {
        // when / then
        repo.access { a ->
            a.query(
                insertOneInto("insert_returning_query_test")
                    .set("id", genRandomUuid())
                    .set("name", randomAlphabetic())
                    .set("created_at", Pg.now())
                    .returning { reader ->
                        InsertReturningQueryTestRecord.from(reader)
                    }
            )
        }
    }

    @Test
    fun `should support on conflict clause`() = runTest {
        // given
        val createdAt = Instant.now().truncatedTo(MILLIS)
        val name = randomAlphabetic()

        repo.access { a ->
            a.execute(
                insertOneInto("insert_returning_query_test") {
                    set("id", genRandomUuid())
                    set("name", name)
                    set("created_at", createdAt)
                }
            )
        }

        // when
        repo.access { a ->
            a.execute(
                insertOneInto("insert_returning_query_test") {
                    set("id", genRandomUuid())
                    set("name", name)
                    set("created_at", createdAt)
                    onConflict(
                        OnConflictClause(
                            listOf("created_at"),
                            "update set created_at = excluded.created_at + interval '1 hours'"
                        )
                    )
                }
            )
        }

        // then
        val result = repo.access { a ->
            a.query(
                plainQuery(
                    "select * from insert_returning_query_test where name = ?",
                    listOf(name.toPgText())
                ) { r ->
                    assertThat(r.instant("created_at")).isEqualTo(createdAt.plus(Duration.ofHours(1)))
                }
            ).singleOrNull()
        }
        assertThat(result).isNotNull
    }

    @Test
    fun `should support on conflict do nothing`() = runTest {
        // given
        val createdAt = Instant.now().truncatedTo(MILLIS)
        val name = randomAlphabetic()

        repo.access { a ->
            a.execute(
                insertOneInto("insert_returning_query_test") {
                    set("id", genRandomUuid())
                    set("name", name)
                    set("created_at", createdAt)
                }
            )
        }

        // when
        repo.access { a ->
            a.execute(
                insertOneInto("insert_returning_query_test") {
                    set("id", genRandomUuid())
                    set("name", name)
                    set("created_at", createdAt)
                    onConflict(doNothing())
                }
            )
        }

        // then
        val result = repo.access { a ->
            a.query(
                plainQuery(
                    "select * from insert_returning_query_test where name = ?",
                    listOf(name.toPgText())
                ) { r ->
                    assertThat(r.instant("created_at")).isEqualTo(createdAt)
                }
            ).singleOrNull()
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
                a.execute(
                    plainUpdate(
                        """
                create table if not exists insert_returning_query_test (
                    id uuid null,
                    name text null,
                    created_at timestamp not null
                )
                """.trimIndent()
                    )
                )
                a.execute(
                    plainUpdate(
                        """create unique index if not exists insert_returning_query_test_created_at_idx
                    | on insert_returning_query_test (created_at)""".trimMargin()
                    )
                )
            }
        }
    }
}