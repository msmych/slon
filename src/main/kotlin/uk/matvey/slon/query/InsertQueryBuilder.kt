package uk.matvey.slon.query

import uk.matvey.slon.value.PgValue

class InsertQueryBuilder(
    private val table: String,
) {
    private val columns = mutableListOf<String>()
    private val values = mutableListOf<List<PgValue>>()
    private var onConflict: OnConflict? = null

    fun columns(columns: List<String>) = apply {
        this.columns.clear()
        this.columns += columns
    }

    fun columns(vararg columns: String) = apply {
        this.columns(columns.toList())
    }

    fun values(values: List<List<PgValue>>) = apply {
        this.values += values
    }

    fun values(vararg values: PgValue) = apply {
        this.values(listOf(values.toList()))
    }

    fun values(vararg values: Pair<String, PgValue>) = apply {
        this.columns(values.map { it.first })
        this.values(listOf(values.map { it.second }))
    }

    fun onConflict(onConflict: OnConflict) = apply {
        this.onConflict = onConflict
    }

    fun build() = InsertQuery(table, columns, values, onConflict)

    companion object {

        fun insertInto(table: String): InsertQueryBuilder {
            return InsertQueryBuilder(table)
        }

        fun insertInto(table: String, block: InsertQueryBuilder.() -> InsertQueryBuilder): InsertQuery {
            return insertInto(table).block().build()
        }
    }
}