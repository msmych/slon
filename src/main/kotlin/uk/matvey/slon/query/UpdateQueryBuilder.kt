package uk.matvey.slon.query

import kotlinx.serialization.json.JsonElement
import uk.matvey.slon.RecordReader
import uk.matvey.slon.value.PgArray.Companion.toPgArray
import uk.matvey.slon.value.PgDate.Companion.toPgDate
import uk.matvey.slon.value.PgInt.Companion.toPgInt
import uk.matvey.slon.value.PgJsonb.Companion.toPgJsonb
import uk.matvey.slon.value.PgText.Companion.toPgText
import uk.matvey.slon.value.PgTimestamp.Companion.toPgTimestamp
import uk.matvey.slon.value.PgUuid.Companion.toPgUuid
import uk.matvey.slon.value.PgValue
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class UpdateQueryBuilder(
    private val table: String,
) {

    private val values = mutableListOf<Pair<String, PgValue>>()
    private lateinit var condition: String
    private val conditionParams = mutableListOf<PgValue>()

    fun set(values: List<Pair<String, PgValue>>) = apply {
        this.values += values
    }

    fun set(vararg values: Pair<String, PgValue>) = apply {
        this.set(values.toList())
    }

    fun set(column: String, value: PgValue) = apply {
        this.set(column to value)
    }

    fun set(column: String, value: String?) = apply {
        set(column, value.toPgText())
    }

    fun <T : Enum<T>> set(column: String, value: Enum<T>?) = apply {
        set(column, value.toPgText())
    }

    fun set(column: String, value: Long?) = apply {
        set(column, value.toPgInt())
    }

    fun set(column: String, value: Int?) = apply {
        set(column, value.toPgInt())
    }

    fun set(column: String, value: UUID?) = apply {
        set(column, value.toPgUuid())
    }

    fun set(column: String, value: Instant?) = apply {
        set(column, value.toPgTimestamp())
    }

    fun set(column: String, value: LocalDateTime?) = apply {
        set(column, value.toPgTimestamp())
    }

    fun set(column: String, value: LocalDate?) = apply {
        set(column, value.toPgDate())
    }

    fun set(column: String, value: JsonElement?) = apply {
        set(column, value.toPgJsonb())
    }

    fun set(column: String, value: Collection<String>?) = apply {
        set(column, value.toPgArray())
    }

    fun where(condition: String, params: List<PgValue>) = apply {
        this.condition = condition
        this.conditionParams += params
    }

    fun where(condition: String, vararg params: PgValue) = apply {
        where(condition, params.toList())
    }

    fun build() = UpdateQuery(table, values, condition, conditionParams)

    fun <T> returning(
        returning: ReturningClause = ReturningClause.all(),
        read: (RecordReader) -> T
    ): UpdateReturningQuery<T> {
        return object : UpdateReturningQuery<T>(build(), returning) {
            override fun read(reader: RecordReader): T {
                return read(reader)
            }
        }
    }

    fun <T> returning(returning: List<String>, read: (RecordReader) -> T): UpdateReturningQuery<T> {
        return returning(ReturningClause(returning), read)
    }

    companion object {

        fun update(table: String): UpdateQueryBuilder {
            return UpdateQueryBuilder(table)
        }

        fun update(table: String, block: UpdateQueryBuilder.() -> UpdateQueryBuilder): UpdateQuery {
            return update(table).block().build()
        }
    }
}