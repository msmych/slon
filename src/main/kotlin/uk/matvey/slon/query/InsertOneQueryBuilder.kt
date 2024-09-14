package uk.matvey.slon.query

import kotlinx.serialization.json.JsonElement
import uk.matvey.slon.value.PgArray.Companion.toPgArray
import uk.matvey.slon.value.PgBool.Companion.toPgBool
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

class InsertOneQueryBuilder(
    private val table: String,
) {

    private val columns = mutableListOf<String>()
    private val values = mutableListOf<PgValue>()
    private var onConflict: OnConflict? = null

    fun columns(columns: List<String>) = apply {
        this.columns.clear()
        this.columns += columns
    }

    fun columns(vararg columns: String) = apply {
        this.columns(columns.toList())
    }

    fun values(values: List<PgValue>) = apply {
        this.values.clear()
        this.values += values
    }

    fun values(vararg values: PgValue) = apply {
        this.values(values.toList())
    }

    fun set(values: List<Pair<String, PgValue>>) = apply {
        this.columns(values.map { it.first })
        this.values(values.map { it.second })
    }

    fun set(vararg values: Pair<String, PgValue>) = apply {
        this.set(values.toList())
    }

    fun set(column: String, value: PgValue) = apply {
        columns += column
        values += value
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

    fun set(column: String, value: Boolean?) = apply {
        set(column, value.toPgBool())
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

    fun onConflict(onConflict: OnConflict) = apply {
        this.onConflict = onConflict
    }

    fun onConflict(columns: List<String>, action: String) = apply {
        this.onConflict = OnConflict(columns, action)
    }

    fun build() = InsertQuery(table, columns, listOf(values), onConflict)

    companion object {

        fun insertOneInto(table: String): InsertOneQueryBuilder {
            return InsertOneQueryBuilder(table)
        }

        fun insertOneInto(table: String, block: InsertOneQueryBuilder.() -> InsertOneQueryBuilder): InsertQuery {
            return insertOneInto(table).block().build()
        }
    }
}