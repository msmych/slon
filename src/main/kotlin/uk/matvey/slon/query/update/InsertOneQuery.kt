package uk.matvey.slon.query.update

import kotlinx.serialization.json.JsonElement
import uk.matvey.slon.value.PgArray.Companion.toPgArray
import uk.matvey.slon.value.PgDate.Companion.toPgDate
import uk.matvey.slon.value.PgInt.Companion.toPgInt
import uk.matvey.slon.value.PgJsonb.Companion.toPgJsonb
import uk.matvey.slon.value.PgText.Companion.toPgText
import uk.matvey.slon.value.PgTimestamp.Companion.toPgTimestamp
import uk.matvey.slon.value.PgUuid.Companion.toPgUuid
import uk.matvey.slon.value.PgValue
import java.sql.Connection
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class InsertOneQuery(
    private val table: String,
    private val columns: List<String>,
    private val values: List<PgValue>,
): Update {

    override fun execute(connection: Connection): Int {
        val columns = columns.joinToString(prefix = "(", postfix = ")")
        val values = values.joinToString(prefix = "(", postfix = ")", transform = PgValue::placeholder)
        val query = "insert into $table $columns values $values"
        val params = this.values
        return RawUpdateQuery(query, params).execute(connection)
    }

    class Builder(
        private val table: String,
    ) {

        private val columns = mutableListOf<String>()
        private val values = mutableListOf<PgValue>()

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

        fun set(values: List<Pair<String, PgValue>>) {
            this.columns(values.map { it.first })
            this.values(values.map { it.second })
        }

        fun set(vararg values: Pair<String, PgValue>) {
            this.set(values.toList())
        }

        fun set(column: String, value: PgValue) = apply {
            columns += column
            values += value
        }

        fun set(column: String, value: String?) = apply {
            columns += column
            values += value.toPgText()
        }

        fun <T : Enum<T>> set(column: String, value: Enum<T>?) = apply {
            columns += column
            values += value.toPgText()
        }

        fun set(column: String, value: Long?) = apply {
            columns += column
            values += value.toPgInt()
        }

        fun set(column: String, value: Int?) = apply {
            columns += column
            values += value.toPgInt()
        }

        fun set(column: String, value: UUID?) = apply {
            columns += column
            values += value.toPgUuid()
        }

        fun set(column: String, value: Instant?) = apply {
            columns += column
            values += value.toPgTimestamp()
        }

        fun set(column: String, value: LocalDateTime?) = apply {
            columns += column
            values += value.toPgTimestamp()
        }

        fun set(column: String, value: LocalDate?) = apply {
            columns += column
            values += value.toPgDate()
        }

        fun set(column: String, value: JsonElement?) = apply {
            columns += column
            values += value.toPgJsonb()
        }

        fun set(column: String, value: Collection<String>?) = apply {
            columns += column
            values += value.toPgArray()
        }

        fun build(): InsertOneQuery {
            require(values.size == columns.size) {
                "Values count must be equal to columns count"
            }
            require(columns.isNotEmpty()) {
                "Columns must be set"
            }
            require(values.isNotEmpty()) {
                "Values must be set"
            }
            return InsertOneQuery(table, columns, values)
        }

        companion object {

            fun insertOneInto(table: String): Builder {
                return Builder(table)
            }

            fun insertOneInto(table: String, block: Builder.() -> Builder): InsertOneQuery {
                return insertOneInto(table).block().build()
            }
        }
    }
}