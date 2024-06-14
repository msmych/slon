package uk.matvey.slon.query

import uk.matvey.slon.RecordReader
import uk.matvey.slon.param.Param
import java.sql.Connection

class InsertReturningQuery<T>(
    private val table: String,
    private val columns: List<String>,
    private val values: List<List<Param>>,
    private val returning: List<String>,
    private val read: (RecordReader) -> T,
) : Query<List<T>> {

    override fun execute(connection: Connection): List<T> {
        val columns = columns.joinToString(prefix = "(", postfix = ")")
        val values = values.joinToString { vs ->
            vs.joinToString(prefix = "(", postfix = ")", transform = Param::stringValue)
        }
        val returning = returning.joinToString()
        val query = "insert into $table $columns values $values returning $returning"
        val params = this.values.flatten()
        return RawQuery(query, params, read).execute(connection)
    }
}