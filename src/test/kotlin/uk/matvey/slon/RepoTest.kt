package uk.matvey.slon

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import uk.matvey.slon.InsertBuilder.Companion.insert
import uk.matvey.slon.InsertBuilder.Companion.insertInto
import uk.matvey.slon.exception.OptimisticLockException
import uk.matvey.slon.exception.PgNotNullViolationException
import uk.matvey.slon.exception.PgUniqueViolationException
import uk.matvey.slon.param.ArrayParam.Companion.textArray
import uk.matvey.slon.param.IntParam.Companion.int
import uk.matvey.slon.param.JsonbParam.Companion.jsonb
import uk.matvey.slon.param.RawParam.Companion.genRandomUuid
import uk.matvey.slon.param.TextParam.Companion.text
import uk.matvey.slon.param.TimestampParam.Companion.timestamp
import uk.matvey.slon.param.UuidParam.Companion.uuid
import uk.matvey.slon.query.RawQuery.Companion.rawQuery
import uk.matvey.slon.query.update.DeleteQuery.Builder.Companion.deleteFrom
import uk.matvey.slon.query.update.OptimisticUpdateQuery.Companion.optimistic
import uk.matvey.slon.query.update.RawUpdateQuery.Companion.rawUpdate
import uk.matvey.slon.query.update.UpdateQuery.Builder.Companion.update
import java.time.Instant
import java.time.temporal.ChronoUnit.MILLIS
import java.util.UUID
import java.util.UUID.randomUUID

class RepoTest : TestContainersSetup() {

    private val name = "name"
    private val age = 27
    private val code = 9_999_999_999_999
    private val createdAt = Instant.now().truncatedTo(MILLIS)
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
    fun `should insert record`() {
        // given
        val id = randomUUID()

        // when / then
        repo.access { a ->
            a.execute(
                insert(
                    "repo_test",
                    "id" to uuid(id),
                    "age" to int(age),
                    "code" to int(code),
                    "name" to text(name),
                    "created_at" to timestamp(createdAt),
                    "details" to jsonb(details),
                    "tags" to textArray(tags),
                )
            )
        }

        val result = repo.access { a ->
            a.execute(
                rawQuery(
                    "select * from repo_test where id = ?",
                    listOf(uuid(id)),
                    RepoTestRecord::from
                )
            )
        }

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
        repo.access { a ->
            a.execute(
                insertInto("repo_test")
                    .columns("id", "name")
                    .values(uuid(randomUUID()), text(name))
                    .values(uuid(randomUUID()), text(name))
                    .values(uuid(randomUUID()), text(name))
                    .build()
            )
        }

        val result = repo.access { a ->
            a.execute(
                rawQuery(
                    "select * from repo_test where name = ?",
                    listOf(text(name))
                ) { r ->
                    assertThat(r.string("name")).isEqualTo(name)
                }
            )
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
            repo.access { a ->
                a.execute(
                    insert(
                        "repo_test",
                        "id" to uuid(id),
                        "age" to int(age.takeUnless { k == "age" }),
                        "code" to int(code.takeUnless { k == "code" }),
                        "name" to text(name.takeUnless { k == "name" }),
                        "created_at" to timestamp(createdAt.takeUnless { k == "created_at" }),
                        "details" to jsonb(details.takeUnless { k == "details" }),
                        "tags" to textArray(tags.takeUnless { k == "tags" }),
                    )
                )
            }

            val (condition, conditionParam) = if (k == "id") {
                "name = ?" to text(name)
            } else {
                "id = ?" to uuid(id)
            }
            val result = repo.access { a ->
                a.execute(rawQuery("select * from repo_test where $condition", listOf(conditionParam)) { r ->
                    assertThat(r.nullableRaw(k)).isNull()
                })
            }
            assertThat(result).hasSize(1)
        }
    }

    @Test
    fun `should update records`() {
        // given
        val id = randomUUID()
        val newName = randomUUID().toString()
        repo.access { a ->
            a.execute(
                insert(
                    "repo_test",
                    "id" to uuid(id),
                    "age" to int(age),
                    "code" to int(code),
                    "name" to text(name),
                    "created_at" to timestamp(createdAt),
                    "details" to jsonb(details),
                    "tags" to textArray(tags),
                )
            )
        }

        // when / then
        repo.access { a ->
            a.execute(
                update("repo_test")
                    .set("name", text(newName))
                    .where("id = ?", uuid(id))
            )
        }

        val result = repo.access { a ->
            a.execute(rawQuery(
                "select * from repo_test where id = ?",
                listOf(uuid(id))
            ) { r ->
                assertThat(r.string("name")).isEqualTo(newName)
            })
        }
        assertThat(result).hasSize(1)
    }

    @Test
    fun `should delete records`() {
        // given
        val id = randomUUID()

        repo.access { a ->
            a.execute(
                insert(
                    "repo_test",
                    "id" to uuid(id),
                    "age" to int(age),
                    "code" to int(code),
                    "name" to text(name),
                    "created_at" to timestamp(createdAt),
                    "details" to jsonb(details),
                    "tags" to textArray(tags),
                )
            )
        }

        // when / then
        repo.access { a -> a.execute(deleteFrom("repo_test").where("id = ?", uuid(id))) }

        val result =
            repo.access { a -> a.execute(rawQuery("select * from repo_test where id = ?", listOf(uuid(id))) {}) }
        assertThat(result).isEmpty()
    }

    @Test
    fun `should execute multiple commands`() {
        // given
        val id1 = UUID.fromString("1ade41eb-7446-430f-9a2c-e45f7136dcf0")
        val id2 = UUID.fromString("9aca29a3-1a96-4807-b6f0-1c90d08b81a9")
        val id3 = UUID.fromString("d5ffb4cd-583b-4423-b25b-af8882aa057e")
        val newName = randomUUID().toString()

        repo.access { a ->
            a.execute(
                insertInto("repo_test")
                    .columns("id", "name")
                    .values(uuid(id1), text(name))
                    .values(uuid(id2), text(name))
                    .build()
            )
        }

        // when / then
        repo.access { a ->
            a.execute(
                insert(
                    "repo_test",
                    "id" to uuid(id3),
                    "name" to text(name)
                )
            )
            a.execute(
                update("repo_test")
                    .set("name", text(newName))
                    .where("id = ?", uuid(id1)),
            )
            a.execute(
                deleteFrom("repo_test")
                    .where("id = ?", uuid(id2))
            )
        }

        val result = repo.access { a ->
            a.execute(
                rawQuery(
                    "select * from repo_test where id in (?, ?, ?) order by id",
                    listOf(uuid(id1), uuid(id2), uuid(id3)),
                    RepoTestRecord::from
                )
            )
        }
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

        repo.access { a ->
            a.execute(
                insert(
                    "repo_test",
                    "id" to genRandomUuid(),
                    "age" to int(age),
                    "code" to int(code),
                    "name" to text(name),
                    "created_at" to timestamp(createdAt),
                    "details" to jsonb(details),
                    "tags" to textArray(tags),
                )
            )
        }

        val result = repo.access { a ->
            a.execute(rawQuery("select * from repo_test where name = ?", listOf(text(name))) { r ->
                assertThat(r.uuid("id")).isNotNull()
            })
        }
        assertThat(result).hasSize(1)
    }

    @Test
    fun `should read records updated within transaction`() {
        // given
        val name = randomUUID().toString()

        // when / then
        repo.access { a ->
            a.execute(insert("repo_test", "id" to genRandomUuid(), "name" to text(name)))
            val record =
                a.execute(rawQuery("select * from repo_test where name = ?", listOf(text(name)), RepoTestRecord::from))
            assertThat(record).hasSize(1)
            assertThat(record[0].name).isEqualTo(name)
        }
    }

    @Test
    fun `should insert returning`() {
        // when
        val result = repo.access { a ->
            a.execute(insertInto("repo_test")
                .set("id" to genRandomUuid())
                .returning(listOf("id")) { r -> r.uuid("id") }
            )
        }

        // then
        assertThat(result).hasSize(1)
    }

    @Test
    fun `should insert returning star`() {
        // when
        val result = repo.access { a ->
            a.execute(
                insertInto("repo_test")
                    .set("id" to genRandomUuid())
                    .returning(RepoTestRecord::from)
            )
        }

        // then
        assertThat(result).hasSize(1)
    }

    @Test
    fun `should throw not null violation exception`() {
        // when / then
        assertThatThrownBy {
            repo.access { a ->
                a.execute(
                    insert("repo_pk_test", "id" to uuid(null))
                )
            }
        }
            .isInstanceOf(PgNotNullViolationException::class.java)
    }

    @Test
    fun `should throw unique violation exception`() {
        // given
        val id = randomUUID()
        repo.access { a -> a.execute(insert("repo_pk_test", "id" to uuid(id))) }

        // when / then
        assertThatThrownBy { repo.access { a -> a.execute(insert("repo_pk_test", "id" to uuid(id))) } }
            .isInstanceOf(PgUniqueViolationException::class.java)
    }

    @Test
    fun `should throw optimistic lock exception`() {
        // when / then
        assertThatThrownBy {
            repo.access { a ->
                a.execute(
                    optimistic(
                        update("repo_test")
                            .set("name", text("New Name"))
                            .where("id = ?", uuid(randomUUID()))
                    )
                )
            }
        }
            .isInstanceOf(OptimisticLockException::class.java)
            .hasMessage("Condition was not satisfied")

        // when / then
        assertThatThrownBy {
            repo.access { a ->
                a.execute(
                    optimistic(
                        deleteFrom("repo_test")
                            .where("id = ?", uuid(randomUUID()))
                    )
                )
            }
        }
            .isInstanceOf(OptimisticLockException::class.java)
            .hasMessage("Condition was not satisfied")
    }

    companion object {

        private lateinit var repo: Repo

        @BeforeAll
        @JvmStatic
        fun initSetup() {
            repo = Repo(dataSource())
            repo.access { a ->
                a.execute(
                    rawUpdate(
                        """
                create table if not exists repo_test (
                    id uuid null,
                    age int null,
                    code bigint null,
                    name text null,
                    created_at timestamp null,
                    details jsonb null,
                    tags text[] null
                )
                """.trimIndent()
                    )
                )
                a.execute(
                    rawUpdate(
                        """
                create table if not exists repo_pk_test (
                    id uuid primary key not null
                )
                """.trimIndent()
                    )
                )
            }
        }
    }
}