package uk.matvey.slon

import uk.matvey.slon.param.Param
import uk.matvey.slon.query.InsertReturningQuery
import uk.matvey.slon.query.update.InsertQuery

class InsertBuilder(
    private val table: String,
) {

    private var onConflict: Pair<List<String>, String>? = null
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

    fun onConflict(columns: List<String>, doClause: String) = apply {
        this.onConflict = columns to doClause
    }

    fun onConflict(doClause: String) = apply {
        onConflict(listOf(), doClause)
    }

    fun onConflictDoNothing() = onConflict("nothing")

    fun build() = InsertQuery(table, columns, values, onConflict)

    fun <T> returning(returning: List<String>, read: (RecordReader) -> T): InsertReturningQuery<T> {
        return InsertReturningQuery(table, columns, values, onConflict, returning, read)
    }

    fun <T> returningOne(returning: List<String>, read: (RecordReader) -> T) = returning(returning, read).one()

    fun <T> returningOneNullable(returning: List<String>, read: (RecordReader) -> T) =
        returning(returning, read).oneNullable()

    fun <T> returning(read: (RecordReader) -> T): InsertReturningQuery<T> {
        return returning(listOf("*"), read)
    }

    fun <T> returningOne(read: (RecordReader) -> T) = returning(read).one()

    fun <T> returningOneNullable(read: (RecordReader) -> T) = returning(read).oneNullable()

    companion object {

        fun insertInto(table: String) = InsertBuilder(table)

        fun insertOne(into: String, values: List<Pair<String, Param>>) = insertInto(into).set(values).build()

        fun insertOne(into: String, vararg values: Pair<String, Param>) = insertOne(into, values.toList())
    }
}