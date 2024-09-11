package uk.matvey.slon.repo

import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.matvey.kit.random.RandomKit.randomAlphabetic
import uk.matvey.kit.random.RandomKit.randomAlphanumeric
import uk.matvey.kit.random.RandomKit.randomBool
import uk.matvey.kit.random.RandomKit.randomInt
import uk.matvey.kit.random.RandomKit.randomLong
import uk.matvey.kit.string.StringKit.toUuid
import uk.matvey.slon.RecordReader
import uk.matvey.slon.TestContainersSetup
import uk.matvey.slon.access.AccessKit.query
import uk.matvey.slon.access.AccessKit.queryOne
import uk.matvey.slon.access.AccessKit.update
import uk.matvey.slon.exception.PgNotNullViolationException
import uk.matvey.slon.exception.PgUniqueViolationException
import uk.matvey.slon.query.DeleteQueryBuilder.Companion.deleteFrom
import uk.matvey.slon.query.InsertOneQueryBuilder.Companion.insertOneInto
import uk.matvey.slon.query.InsertQueryBuilder.Companion.insertInto
import uk.matvey.slon.query.UpdateQueryBuilder.Companion.update
import uk.matvey.slon.value.Pg.Companion.genRandomUuid
import uk.matvey.slon.value.PgText.Companion.toPgText
import uk.matvey.slon.value.PgUuid
import uk.matvey.slon.value.PgUuid.Companion.toPgUuid
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit.MILLIS
import java.util.UUID
import java.util.UUID.randomUUID

class RepoTest : TestContainersSetup() {

    private val name = randomAlphabetic()
    private val age = randomInt(1..99)
    private val code = randomLong(9_999_999_999_999)
    private val isBot = randomBool()
    private val birthDate = LocalDate.now()
    private val createdAt = Instant.now().truncatedTo(MILLIS)
    private val details = buildJsonObject {
        put(randomAlphanumeric(), randomAlphanumeric())
    }
    private val tags = listOf(randomAlphanumeric(), randomAlphanumeric())

    private data class RepoTestRecord(
        val id: UUID?,
        val age: Int?,
        val code: Long?,
        val name: String?,
        val isBot: Boolean?,
        val birthDate: LocalDate?,
        val createdAt: Instant?,
        val details: String?,
        val tags: List<String>?,
    ) {

        companion object {

            fun from(reader: RecordReader): RepoTestRecord {
                return RepoTestRecord(
                    reader.uuidOrNull("id"),
                    reader.intOrNull("age"),
                    reader.longOrNull("code"),
                    reader.stringOrNull("name"),
                    reader.boolOrNull("is_bot"),
                    reader.localDateOrNull("birth_date"),
                    reader.instantOrNull("created_at"),
                    reader.stringOrNull("details"),
                    reader.stringListOrNull("tags"),
                )
            }
        }
    }

    @Test
    fun `should insert null values`() = runTest {
        listOf(
            "id",
            "age",
            "code",
            "name",
            "is_bot",
            "birth_date",
            "created_at",
            "details",
            "tags",
        ).forEach { k ->
            // given
            val id = randomUUID().takeUnless { k == "id" }
            val name = randomAlphabetic()

            // when
            repo.access { a ->
                a.execute(
                    insertOneInto("repo_test") {
                        set("id", id)
                        set("age", age.takeUnless { k == "age" })
                        set("code", code.takeUnless { k == "code" })
                        set("name", name.takeUnless { k == "name" })
                        set("is_bot", isBot.takeUnless { k == "is_bot" })
                        set("birth_date", birthDate.takeUnless { k == "birth_date" })
                        set("created_at", createdAt.takeUnless { k == "created_at" })
                        set("details", details.takeUnless { k == "details" })
                        set("tags", tags.takeUnless { k == "tags" })
                    }
                )
            }

            // then
            val (condition, conditionParam) = if (k == "id") {
                "name = ?" to name.toPgText()
            } else {
                "id = ?" to id.toPgUuid()
            }
            repo.access { a ->
                a.query(
                    "select * from repo_test where $condition",
                    listOf(conditionParam)
                ) { r ->
                    assertThat(r.rawOrNull(k)).isNull()
                }
            }
        }
    }

    @Test
    fun `should execute multiple commands`() = runTest {
        // given
        val id1 = "1ade41eb-7446-430f-9a2c-e45f7136dcf0".toUuid()
        val id2 = "9aca29a3-1a96-4807-b6f0-1c90d08b81a9".toUuid()
        val id3 = "d5ffb4cd-583b-4423-b25b-af8882aa057e".toUuid()
        val newName = randomAlphabetic()

        repo.access { a ->
            a.execute(
                insertInto("repo_test")
                    .columns("id", "name")
                    .values(id1.toPgUuid(), name.toPgText())
                    .values(id2.toPgUuid(), name.toPgText())
                    .build()
            )
        }

        // when
        repo.access { a ->
            a.execute(insertOneInto("repo_test") {
                set("id", id3.toPgUuid())
                set("name", name.toPgText())
            })
            a.execute(
                update("repo_test") {
                    set("name", newName)
                    where("id = ?", id1.toPgUuid())
                }
            )
            a.execute(
                deleteFrom("repo_test")
                    .where("id = ?", id2.toPgUuid())
            )
        }

        // then
        val result = repo.access { a ->
            a.query(
                "select * from repo_test where id in (?, ?, ?) order by id",
                listOf(id1, id2, id3).map { it.toPgUuid() },
                RepoTestRecord::from
            )
        }
        assertThat(result).hasSize(2)
        assertThat(result[0].id).isEqualTo(id1)
        assertThat(result[0].name).isEqualTo(newName)
        assertThat(result[1].id).isEqualTo(id3)
        assertThat(result[1].name).isEqualTo(name)
    }

    @Test
    fun `should support plain params`() = runTest {
        // given
        val name = randomAlphabetic()

        // when
        repo.access { a ->
            a.execute(
                insertOneInto("repo_test") {
                    set("id", genRandomUuid())
                    set("age", age)
                    set("code", code)
                    set("name", name)
                    set("is_bot", isBot)
                    set("birth_date", birthDate)
                    set("created_at", createdAt)
                    set("details", details)
                    set("tags", tags)
                }
            )
        }

        // then
        repo.access { a ->
            a.query(
                "select * from repo_test where name = ?",
                listOf(name.toPgText()),
            ) { r ->
                assertThat(r.uuid("id")).isNotNull
            }
        }
    }

    @Test
    fun `should read records updated within transaction`() = runTest {
        // given
        val name = randomAlphabetic()

        // when / then
        repo.access { a ->
            a.execute(
                insertOneInto("repo_test") {
                    set("id", genRandomUuid())
                    set("name", name)
                }
            )
            val record = a.queryOne(
                "select * from repo_test where name = ?",
                listOf(name.toPgText()),
                RepoTestRecord::from
            )
            assertThat(record.name).isEqualTo(name)
        }
    }

    @Test
    fun `should throw not null violation exception`() = runTest {
        // when / then
        val e = assertThrows<PgNotNullViolationException> {
            repo.access { a ->
                a.execute(
                    insertOneInto("repo_pk_test") {
                        set("id", PgUuid(null))
                    }
                )
            }
        }
        assertThat(e.table).isEqualTo("repo_pk_test")
        assertThat(e.column).isEqualTo("id")
    }

    @Test
    fun `should throw unique violation exception`() = runTest {
        // given
        val id = randomUUID()
        repo.access { a ->
            a.execute(
                insertOneInto("repo_pk_test") {
                    set("id", id)
                }
            )
        }

        // when / then
        val e = assertThrows<PgUniqueViolationException> {
            repo.access { a ->
                a.execute(
                    insertOneInto("repo_pk_test") {
                        set("id", id)
                    }
                )
            }
        }
        assertThat(e.constraint).isEqualTo("repo_pk_test_pkey")
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
                create table if not exists repo_test (
                    id uuid null,
                    age int null,
                    code bigint null,
                    name text null,
                    is_bot bool null,
                    birth_date date null,
                    created_at timestamp null,
                    details jsonb null,
                    tags text[] null
                )
                """.trimIndent()
                )
                a.update(
                    """
                create table if not exists repo_pk_test (
                    id uuid primary key not null
                )
                """.trimIndent()
                )
            }
        }
    }
}