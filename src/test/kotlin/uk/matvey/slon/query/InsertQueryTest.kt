package uk.matvey.slon.query

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import uk.matvey.kit.random.RandomKit.randomAlphabetic
import uk.matvey.slon.RecordReader
import uk.matvey.slon.TestContainersSetup
import uk.matvey.slon.access.AccessKit.queryAll
import uk.matvey.slon.access.AccessKit.queryOne
import uk.matvey.slon.access.AccessKit.queryOneOrNull
import uk.matvey.slon.access.AccessKit.update
import uk.matvey.slon.query.InsertOneQueryBuilder.Companion.insertOneInto
import uk.matvey.slon.query.InsertQueryBuilder.Companion.insertInto
import uk.matvey.slon.query.OnConflict.Companion.doNothing
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
    fun `should validate on creation`() {
        // when / then
        assertThatThrownBy {
            InsertQuery(
                table = "table",
                columns = listOf("col1", "col2"),
                values = listOf(listOf("value1".toPgText())),
            )
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Values count must be equal to columns count")

        assertThatThrownBy {
            InsertQuery(
                table = "table",
                columns = listOf(),
                values = listOf(),
            )
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Columns must be set")
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
            a.queryOne(
                "select * from insert_query_test where id = ?",
                listOf(id.toPgUuid()),
                InsertQueryTestRecord::from
            )
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
            a.queryAll(
                "select * from insert_query_test where name = ?",
                listOf(name.toPgText())
            ) { r ->
                assertThat(r.string("name")).isEqualTo(name)
            }
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
                        listOf("created_at"),
                        "update set created_at = excluded.created_at + interval '1 hours'"
                    )
                }
            )
        }

        // then
        val result = repo.access { a ->
            a.queryOneOrNull(
                "select * from insert_query_test where name = ?",
                listOf(name.toPgText())
            ) { r ->
                assertThat(r.instant("created_at")).isEqualTo(createdAt.plus(Duration.ofHours(1)))
            }
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
            a.queryOneOrNull(
                "select * from insert_query_test where name = ?",
                listOf(name.toPgText())
            ) { r ->
                assertThat(r.instant("created_at")).isEqualTo(createdAt)
            }
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
                a.update(
                    """
                create table if not exists insert_query_test (
                    id uuid null,
                    name text null,
                    created_at timestamp not null
                )
                """.trimIndent()
                )
                a.update(
                    """create unique index if not exists insert_query_test_created_at_idx
                    | on insert_query_test (created_at)""".trimMargin()
                )
            }
        }
    }
}