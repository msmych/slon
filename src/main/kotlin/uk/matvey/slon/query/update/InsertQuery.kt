package uk.matvey.slon.query.update

import uk.matvey.slon.param.Param
import uk.matvey.slon.query.Query
import java.sql.Connection

class InsertQuery(
    private val table: String,
    private val columns: List<String>,
    private val values: List<List<Param>>,
) : Query<Int> {

    override fun execute(connection: Connection): Int {
        val columns = columns.joinToString(prefix = "(", postfix = ")")
        val values = values.joinToString { vs ->
            vs.joinToString(prefix = "(", postfix = ")", transform = Param::stringValue)
        }
        val query = "insert into $table $columns values $values"
        val params = this.values.flatten()
        return RawUpdateQuery(query, params).execute(connection)
    }
}