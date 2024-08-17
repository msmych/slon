package uk.matvey.slon.query

import uk.matvey.slon.RecordReader
import uk.matvey.slon.param.Param
import uk.matvey.slon.query.RawQuery.Companion.rawQuery
import java.sql.Connection

class InsertReturningQuery<T>(
    private val table: String,
    private val columns: List<String>,
    private val values: List<List<Param>>,
    private val onConflict: Pair<List<String>, String>?,
    private val returning: List<String>,
    private val read: (RecordReader) -> T,
) : Query<List<T>> {

    override fun execute(connection: Connection): List<T> {
        val columns = columns.joinToString(prefix = "(", postfix = ")")
        val values = values.joinToString { vs ->
            vs.joinToString(prefix = "(", postfix = ")", transform = Param::stringValue)
        }
        val returning = returning.joinToString()
        val onConflictClause = onConflict?.let { (k, v) ->
            val conflictColumns = k.takeIf { it.isNotEmpty() }
                ?.let { " " + it.joinToString(prefix = "(", postfix = ")") }
                ?: ""
            " on conflict" + conflictColumns + " do $v"
        } ?: ""
        val query = "insert into $table $columns values $values" + onConflictClause + " returning $returning"
        val params = this.values.flatten()
        return rawQuery(query, params, read).execute(connection)
    }

    fun one(): OneQuery<T> {
        return OneQuery(this)
    }

    fun oneOrNull(): OneOrNullQuery<T> {
        return OneOrNullQuery(this)
    }
}