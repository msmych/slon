package uk.matvey.slon

import uk.matvey.slon.param.Param
import uk.matvey.slon.query.InsertReturningQuery
import uk.matvey.slon.query.update.InsertQuery

class InsertBuilder(
    private val table: String,
) {

    private lateinit var columns: List<String>
    private val values = mutableListOf<List<Param>>()

    fun columns(columns: List<String>) = apply {
        this.columns = columns
    }

    fun columns(vararg columns: String) = columns(columns.toList())

    fun values(values: List<Param>) = apply {
        this.values += values
    }

    fun values(vararg values: Param) = values(values.toList())

    fun set(values: List<Pair<String, Param>>) = apply {
        this.columns = values.map { it.first }
        this.values += values.map { it.second }
    }

    fun set(vararg values: Pair<String, Param>) = set(values.toList())

    fun build() = InsertQuery(table, columns, values)

    fun <T> returning(returning: List<String>, read: (RecordReader) -> T): InsertReturningQuery<T> {
        return InsertReturningQuery(table, columns, values, returning, read)
    }

    fun <T> returning(read: (RecordReader) -> T): InsertReturningQuery<T> {
        return returning(listOf("*"), read)
    }

    companion object {

        fun insertInto(table: String) = InsertBuilder(table)

        fun insert(into: String, values: List<Pair<String, Param>>) = insertInto(into).set(values).build()

        fun insert(into: String, vararg values: Pair<String, Param>) = insert(into, values.toList())
    }
}