package uk.matvey.slon.query

import uk.matvey.slon.RecordReader
import uk.matvey.slon.param.Param
import uk.matvey.slon.query.update.InsertQuery

class InsertQueryBuilder(
    private val table: String,
) {
    private lateinit var columns: List<String>
    private val values = mutableListOf<List<Param>>()
    private var onConflict: Pair<List<String>, String>? = null

    fun columns(columns: List<String>) {
        this.columns = columns
    }

    fun values(values: List<List<Param>>) {
        this.values += values
    }

    fun values(vararg values: List<Param>) {
        this.values(values.toList())
    }

    fun set(vararg values: Pair<String, Param>) {
        this.columns(values.map { it.first })
        this.values(values.map { it.second })
    }

    fun onConflict(columns: List<String>, doClause: String) {
        this.onConflict = columns to doClause
    }

    fun onConflict(doClause: String) {
        onConflict(listOf(), doClause)
    }

    fun <T> returning(returning: List<String>, read: (RecordReader) -> T): InsertReturningQuery<T> {
        return InsertReturningQuery(table, columns, values, onConflict, returning, read)
    }

    fun build() = InsertQuery(table, columns, values, onConflict)

    companion object {

        fun insert(table: String): InsertQueryBuilder {
            return InsertQueryBuilder(table)
        }
    }
}