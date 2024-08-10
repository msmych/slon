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
import uk.matvey.kit.random.RandomKit.randomInt
import uk.matvey.kit.random.RandomKit.randomLong
import uk.matvey.slon.RecordReader
import uk.matvey.slon.TestContainersSetup
import uk.matvey.slon.access.AccessKit.insertInto
import uk.matvey.slon.access.AccessKit.update
import uk.matvey.slon.exception.PgNotNullViolationException
import uk.matvey.slon.exception.PgUniqueViolationException
import uk.matvey.slon.param.ArrayParam.Companion.textArray
import uk.matvey.slon.param.DateParam.Companion.date
import uk.matvey.slon.param.IntParam.Companion.int
import uk.matvey.slon.param.JsonbParam.Companion.jsonb
import uk.matvey.slon.param.PlainParam.Companion.genRandomUuid
import uk.matvey.slon.param.TextParam.Companion.text
import uk.matvey.slon.param.TimestampParam.Companion.timestamp
import uk.matvey.slon.param.UuidParam.Companion.uuid
import uk.matvey.slon.query.update.DeleteQuery.Builder.Companion.deleteFrom
import uk.matvey.slon.repo.RepoKit.insertInto
import uk.matvey.slon.repo.RepoKit.query
import uk.matvey.slon.repo.RepoKit.queryOne
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit.MILLIS
import java.util.UUID
import java.util.UUID.randomUUID

class RepoTest : TestContainersSetup() {

    private val name = randomAlphabetic()
    private val age = randomInt(1..99)
    private val code = randomLong(9_999_999_999_999)
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
        val birthDate: LocalDate?,
        val createdAt: Instant?,
        val details: String?,
        val tags: List<String>?,
    ) {

        companion object {

            fun from(reader: RecordReader): RepoTestRecord {
                return RepoTestRecord(
                    reader.nullableUuid("id"),
                    reader.nullableInt("age"),
                    reader.nullableLong("code"),
                    reader.nullableString("name"),
                    reader.nullableLocalDate("birth_date"),
                    reader.nullableInstant("created_at"),
                    reader.nullableString("details"),
                    reader.nullableStringList("tags"),
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
            "birth_date",
            "created_at",
            "details",
            "tags",
        ).forEach { k ->
            // given
            val id = randomUUID().takeUnless { k == "id" }
            val name = randomUUID().toString()

            // when
            repo.insertInto("repo_test") {
                set(
                    "id" to uuid(id),
                    "age" to int(age.takeUnless { k == "age" }),
                    "code" to int(code.takeUnless { k == "code" }),
                    "name" to text(name.takeUnless { k == "name" }),
                    "birth_date" to date(birthDate.takeUnless { k == "birth_date" }),
                    "created_at" to timestamp(createdAt.takeUnless { k == "created_at" }),
                    "details" to jsonb(details.takeUnless { k == "details" }),
                    "tags" to textArray(tags.takeUnless { k == "tags" }),
                )
            }

            // then
            val (condition, conditionParam) = if (k == "id") {
                "name = ?" to text(name)
            } else {
                "id = ?" to uuid(id)
            }
            repo.queryOne("select * from repo_test where $condition", listOf(conditionParam)) { r ->
                assertThat(r.nullableRaw(k)).isNull()
            }
        }
    }

    @Test
    fun `should execute multiple commands`() = runTest {
        // given
        val id1 = UUID.fromString("1ade41eb-7446-430f-9a2c-e45f7136dcf0")
        val id2 = UUID.fromString("9aca29a3-1a96-4807-b6f0-1c90d08b81a9")
        val id3 = UUID.fromString("d5ffb4cd-583b-4423-b25b-af8882aa057e")
        val newName = randomUUID().toString()

        repo.insertInto("repo_test") {
            columns("id", "name")
            values(uuid(id1), text(name))
            values(uuid(id2), text(name))
        }

        // when
        repo.access { a ->
            a.insertInto("repo_test") {
                set(
                    "id" to uuid(id3),
                    "name" to text(name)
                )
            }
            a.update("repo_test") {
                set("name", text(newName))
                where("id = ?", uuid(id1))
            }
            a.execute(
                deleteFrom("repo_test")
                    .where("id = ?", uuid(id2))
            )
        }

        // then
        val result = repo.query(
            "select * from repo_test where id in (?, ?, ?) order by id",
            listOf(uuid(id1), uuid(id2), uuid(id3)),
            RepoTestRecord::from
        )
        assertThat(result).hasSize(2)
        assertThat(result[0].id).isEqualTo(id1)
        assertThat(result[0].name).isEqualTo(newName)
        assertThat(result[1].id).isEqualTo(id3)
        assertThat(result[1].name).isEqualTo(name)
    }

    @Test
    fun `should support plain params`() = runTest {
        // given
        val name = randomUUID().toString()

        // when
        repo.insertInto("repo_test") {
            set(
                "id" to genRandomUuid(),
                "age" to int(age),
                "code" to int(code),
                "name" to text(name),
                "birth_date" to date(birthDate),
                "created_at" to timestamp(createdAt),
                "details" to jsonb(details),
                "tags" to textArray(tags),
            )
        }

        // then
        repo.queryOne("select * from repo_test where name = ?", listOf(text(name))) { r ->
            assertThat(r.uuid("id")).isNotNull
        }
    }

    @Test
    fun `should read records updated within transaction`() = runTest {
        // given
        val name = randomUUID().toString()

        // when / then
        repo.access { a ->
            a.insertInto("repo_test") {
                set(
                    "id" to genRandomUuid(),
                    "name" to text(name)
                )
            }
            val record = a.queryOne(
                "select * from repo_test where name = ?", listOf(text(name)),
                RepoTestRecord::from
            )
            assertThat(record.name).isEqualTo(name)
        }
    }

    @Test
    fun `should throw not null violation exception`() = runTest {
        // when / then
        assertThrows<PgNotNullViolationException> {
            repo.insertInto("repo_pk_test") {
                set("id" to uuid(null))
            }
        }
    }

    @Test
    fun `should throw unique violation exception`() = runTest {
        // given
        val id = randomUUID()
        repo.insertInto("repo_pk_test") {
            set("id" to uuid(id))
        }

        // when / then
        assertThrows<PgUniqueViolationException> {
            repo.insertInto("repo_pk_test") {
                set("id" to uuid(id))
            }
        }
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
                create table if not exists repo_test (
                    id uuid null,
                    age int null,
                    code bigint null,
                    name text null,
                    birth_date date null,
                    created_at timestamp null,
                    details jsonb null,
                    tags text[] null
                )
                """.trimIndent()
                )
                a.executePlain(
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