package uk.matvey.slon.query.update

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import uk.matvey.kit.random.RandomKit.randomAlphabetic
import uk.matvey.slon.RecordReader
import uk.matvey.slon.TestContainersSetup
import uk.matvey.slon.query.InsertOneBuilder.Companion.insertOneInto
import uk.matvey.slon.query.InsertQueryBuilder.Companion.insertInto
import uk.matvey.slon.query.OnConflictClause
import uk.matvey.slon.query.OnConflictClause.Companion.doNothing
import uk.matvey.slon.query.Query.Companion.plainQuery
import uk.matvey.slon.query.Update.Companion.plainUpdate
import uk.matvey.slon.repo.Repo
import uk.matvey.slon.value.Pg
import uk.matvey.slon.value.Pg.Companion.genRandomUuid
import uk.matvey.slon.value.PgText.Companion.toPgText
import uk.matvey.slon.value.PgUuid.Companion.toPgUuid
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
        val name = randomAlphabetic()
        val createdAt = Instant.now().truncatedTo(MILLIS)

        // when / then
        repo.access { a ->
            a.execute(
                insertOneInto("insert_query_test") {
                    set("id", id)
                    set("name", name)
                    set("created_at", createdAt)
                }
            )
        }

        val result = repo.access { a ->
            a.query(
                plainQuery(
                    "select * from insert_query_test where id = ?",
                    listOf(id.toPgUuid()),
                    InsertQueryTestRecord::from
                )
            ).single()
        }

        assertThat(result.id).isEqualTo(id)
        assertThat(result.name).isEqualTo(name)
        assertThat(result.createdAt).isEqualTo(createdAt)
    }

    @Test
    fun `should insert multiple records`() = runTest {
        // given
        val name = randomAlphabetic()

        // when
        repo.access { a ->
            a.execute(
                insertInto("insert_query_test")
                    .columns("id", "name", "created_at")
                    .values(randomUUID().toPgUuid(), name.toPgText(), Pg.now())
                    .values(randomUUID().toPgUuid(), name.toPgText(), Pg.plain("now() + interval '1 hours'"))
                    .values(randomUUID().toPgUuid(), name.toPgText(), Pg.plain("now() + interval '2 hours'"))
                    .build()
            )
        }

        // then
        val result = repo.access { a ->
            a.query(
                plainQuery(
                    "select * from insert_query_test where name = ?",
                    listOf(name.toPgText())
                ) { r ->
                    assertThat(r.string("name")).isEqualTo(name)
                }
            )
        }
        assertThat(result).hasSize(3)
    }

    @Test
    fun `should support on conflict clause`() = runTest {
        // given
        val createdAt = Instant.now().truncatedTo(MILLIS)
        val name = randomAlphabetic()

        repo.access { a ->
            a.execute(
                insertOneInto("insert_query_test") {
                    set("id", genRandomUuid())
                    set("name", name)
                    set("created_at", createdAt)
                }
            )
        }

        // when
        repo.access { a ->
            a.execute(
                insertOneInto("insert_query_test") {
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
                    "select * from insert_query_test where name = ?",
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
                insertOneInto("insert_query_test") {
                    set("id", genRandomUuid())
                    set("name", name)
                    set("created_at", createdAt)
                }
            )
        }

        // when
        repo.access { a ->
            a.execute(
                insertOneInto("insert_query_test") {
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
                    "select * from insert_query_test where name = ?",
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
                create table if not exists insert_query_test (
                    id uuid null,
                    name text null,
                    created_at timestamp not null
                )
                """.trimIndent()
                    )
                )
                a.execute(
                    plainUpdate(
                        """create unique index if not exists insert_query_test_created_at_idx
                    | on insert_query_test (created_at)""".trimMargin()
                    )
                )
            }
        }
    }
}