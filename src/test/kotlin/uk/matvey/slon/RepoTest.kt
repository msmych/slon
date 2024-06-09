package uk.matvey.slon

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import uk.matvey.slon.QueryParam.Companion.int
import uk.matvey.slon.QueryParam.Companion.jsonb
import uk.matvey.slon.QueryParam.Companion.raw
import uk.matvey.slon.QueryParam.Companion.text
import uk.matvey.slon.QueryParam.Companion.textArray
import uk.matvey.slon.QueryParam.Companion.timestamp
import uk.matvey.slon.QueryParam.Companion.uuid
import uk.matvey.slon.command.Delete.Builder.Companion.delete
import uk.matvey.slon.command.Insert.Builder.Companion.insert
import uk.matvey.slon.command.Update.Builder.Companion.update
import uk.matvey.slon.exception.PgNotNullViolationException
import uk.matvey.slon.exception.PgUniqueViolationException
import uk.matvey.slon.query.RecordReader
import java.time.Instant
import java.util.UUID
import java.util.UUID.randomUUID

class RepoTest : TestContainersSetup() {

    private val name = "name"
    private val age = 27
    private val code = 9_999_999_999_999
    private val createdAt = Instant.now()
    private val details = """{"key": "value"}"""
    private val tags = listOf("tag1", "tag2")

    private data class RepoTestRecord(
        val id: UUID?,
        val age: Int?,
        val code: Long?,
        val name: String?,
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
                    reader.nullableInstant("created_at"),
                    reader.nullableString("details"),
                    reader.nullableStringList("tags"),
                )
            }
        }
    }

    @Test
    fun `should insert records`() {
        // given
        val id = randomUUID()

        // when / then
        repo.execute(
            insert("repo_test")
                .values(
                    "id" to uuid(id),
                    "age" to int(age),
                    "code" to int(code),
                    "name" to text(name),
                    "created_at" to timestamp(createdAt),
                    "details" to jsonb(details),
                    "tags" to textArray(tags),
                )
        )

        val result = repo.query("SELECT * FROM repo_test WHERE id = ?", listOf(uuid(id)), RepoTestRecord::from)

        assertThat(result).hasSize(1)
        assertThat(result[0].id).isEqualTo(id)
        assertThat(result[0].age).isEqualTo(age.toLong())
        assertThat(result[0].code).isEqualTo(code)
        assertThat(result[0].name).isEqualTo(name)
        assertThat(result[0].createdAt).isEqualTo(createdAt)
        assertThat(result[0].details).isEqualTo(details)
        assertThat(result[0].tags).isEqualTo(tags)
    }

    @Test
    fun `should insert multiple records`() {
        // given
        val name = randomUUID().toString()

        // when / then
        repo.execute(
            insert("repo_test")
                .columns("id", "name")
                .values(uuid(randomUUID()), text(name))
                .values(uuid(randomUUID()), text(name))
                .values(uuid(randomUUID()), text(name))
        )

        val result = repo.query("SELECT * FROM repo_test WHERE name = ?", listOf(text(name))) { r ->
            assertThat(r.string("name")).isEqualTo(name)
        }
        assertThat(result).hasSize(3)
    }

    @Test
    fun `should insert null values`() {
        listOf(
            "id",
            "age",
            "code",
            "name",
            "created_at",
            "details",
            "tags",
        ).forEach { k ->
            // given
            val id = randomUUID().takeUnless { k == "id" }
            val name = randomUUID().toString()

            // when / then
            repo.execute(
                insert("repo_test")
                    .values(
                        "id" to uuid(id),
                        "age" to int(age.takeUnless { k == "age" }),
                        "code" to int(code.takeUnless { k == "code" }),
                        "name" to text(name.takeUnless { k == "name" }),
                        "created_at" to timestamp(createdAt.takeUnless { k == "created_at" }),
                        "details" to jsonb(details.takeUnless { k == "details" }),
                        "tags" to textArray(tags.takeUnless { k == "tags" }),
                    )
            )

            val (condition, conditionParam) = if (k == "id") {
                "name = ?" to text(name)
            } else {
                "id = ?" to uuid(id)
            }
            val result = repo.query("SELECT * FROM repo_test WHERE $condition", listOf(conditionParam)) { r ->
                assertThat(r.nullableRaw(k)).isNull()
            }
            assertThat(result).hasSize(1)
        }
    }

    @Test
    fun `should update records`() {
        // given
        val id = randomUUID()
        val newName = randomUUID().toString()
        repo.execute(
            insert("repo_test")
                .values(
                    "id" to uuid(id),
                    "age" to int(age),
                    "code" to int(code),
                    "name" to text(name),
                    "created_at" to timestamp(createdAt),
                    "details" to jsonb(details),
                    "tags" to textArray(tags),
                )
        )

        // when / then
        repo.execute(
            update("repo_test")
                .set("name", text(newName))
                .where("id = ?", uuid(id))
        )

        val result = repo.query("SELECT * FROM repo_test WHERE id = ?", listOf(uuid(id))) { r ->
            assertThat(r.string("name")).isEqualTo(newName)
        }
        assertThat(result).hasSize(1)
    }

    @Test
    fun `should delete records`() {
        // given
        val id = randomUUID()

        repo.execute(
            insert("repo_test")
                .values(
                    "id" to uuid(id),
                    "age" to int(age),
                    "code" to int(code),
                    "name" to text(name),
                    "created_at" to timestamp(createdAt),
                    "details" to jsonb(details),
                    "tags" to textArray(tags),
                )
        )

        // when / then
        repo.execute(delete("repo_test").where("id = ?", uuid(id)))

        val result = repo.query("SELECT * FROM repo_test WHERE id = ?", listOf(uuid(id))) {}
        assertThat(result).isEmpty()
    }

    @Test
    fun `should execute multiple commands`() {
        // given
        val id1 = UUID.fromString("1ade41eb-7446-430f-9a2c-e45f7136dcf0")
        val id2 = UUID.fromString("9aca29a3-1a96-4807-b6f0-1c90d08b81a9")
        val id3 = UUID.fromString("d5ffb4cd-583b-4423-b25b-af8882aa057e")
        val newName = randomUUID().toString()

        repo.execute(
            insert("repo_test")
                .columns("id", "name")
                .values(uuid(id1), text(name))
                .values(uuid(id2), text(name))
        )

        // when / then
        repo.execute(
            insert("repo_test")
                .values("id" to uuid(id3), "name" to text(name))
                .build(),
            update("repo_test")
                .set("name", text(newName))
                .where("id = ?", uuid(id1)),
            delete("repo_test")
                .where("id = ?", uuid(id2))
        )

        val result = repo.query(
            "SELECT * FROM repo_test WHERE id IN (?, ?, ?) ORDER BY id",
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
    fun `should support raw params`() {
        // given
        val name = randomUUID().toString()

        repo.execute(
            insert("repo_test")
                .values(
                    "id" to raw("gen_random_uuid()"),
                    "age" to int(age),
                    "code" to int(code),
                    "name" to text(name),
                    "created_at" to timestamp(createdAt),
                    "details" to jsonb(details),
                    "tags" to textArray(tags),
                )
        )

        val result = repo.query("SELECT * FROM repo_test WHERE name = ?", listOf(text(name))) { r ->
            assertThat(r.uuid("id")).isNotNull()
        }
        assertThat(result).hasSize(1)
    }

    @Test
    fun `should throw not null violation exception`() {
        // when / then
        assertThatThrownBy { repo.execute(insert("repo_pk_test").values("id" to uuid(null))) }
            .isInstanceOf(PgNotNullViolationException::class.java)
    }

    @Test
    fun `should throw unique violation exception`() {
        // given
        val id = randomUUID()
        repo.execute(insert("repo_pk_test").values("id" to uuid(id)))

        // when / then
        assertThatThrownBy { repo.execute(insert("repo_pk_test").values("id" to uuid(id))) }
            .isInstanceOf(PgUniqueViolationException::class.java)
    }

    companion object {

        private lateinit var da: DataAccess
        private lateinit var repo: Repo

        @BeforeAll
        @JvmStatic
        fun initSetup() {
            da = DataAccess(dataSource())
            da.execute(
                """
                CREATE TABLE IF NOT EXISTS repo_test (
                    id UUID NULL,
                    age INT NULL,
                    code BIGINT NULL,
                    name TEXT NULL,
                    created_at TIMESTAMP NULL,
                    details JSONB NULL,
                    tags TEXT[] NULL
                )
                """.trimIndent()
            )
            da.execute(
                """
                CREATE TABLE IF NOT EXISTS repo_pk_test (
                    id UUID PRIMARY KEY NOT NULL
                )
                """.trimIndent()
            )
            repo = Repo(da)
        }
    }
}