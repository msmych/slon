package uk.matvey.slon

import uk.matvey.slon.command.Insert
import uk.matvey.slon.param.Param
import uk.matvey.slon.query.InsertReturning

class InsertBuilder(
    private val table: String,
) {

    private lateinit var columns: List<String>
    private val values = mutableListOf<List<Param>>()

    fun columns(vararg columns: String) = apply {
        this.columns = columns.toList()
    }

    fun values(vararg values: Param) = apply {
        this.values += values.toList()
    }

    fun set(vararg values: Pair<String, Param>) = apply {
        this.columns = values.map { (k, _) -> k }.toList()
        this.values += values.map { (_, v) -> v }.toList()
    }

    fun build() = Insert(table, columns, values)

    fun <T> returning(returning: List<String>, read: (RecordReader) -> T) =
        InsertReturning(table, columns.toList(), values, returning, read)

    companion object {

        fun insert(into: String) = InsertBuilder(into)
    }
}