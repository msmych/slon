package uk.matvey.slon.query

import uk.matvey.slon.RecordReader
import uk.matvey.slon.value.PgValue
import uk.matvey.slon.query.update.InsertQuery

class InsertQueryBuilder(
    private val table: String,
) {
    private val columns = mutableListOf<String>()
    private val values = mutableListOf<List<PgValue>>()
    private var onConflict: Pair<List<String>, String>? = null

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

    fun onConflict(columns: List<String>, doClause: String) = apply {
        this.onConflict = columns to doClause
    }

    fun onConflict(doClause: String) = apply {
        this.onConflict(listOf(), doClause)
    }

    fun onConflictDoNothing() = apply {
        this.onConflict("nothing")
    }

    fun build() = InsertQuery(table, columns, values, onConflict)

    fun <T> returning(returning: List<String>, read: (RecordReader) -> T): InsertReturningQuery<T> {
        return InsertReturningQuery(table, columns, values, onConflict, returning, read)
    }

    fun <T> returning(read: (RecordReader) -> T): InsertReturningQuery<T> {
        return this.returning(listOf("*"), read)
    }

    companion object {

        fun insertInto(table: String): InsertQueryBuilder {
            return InsertQueryBuilder(table)
        }
    }
}