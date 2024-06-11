package uk.matvey.slon.command

import uk.matvey.slon.param.Param
import java.sql.Connection

class Insert(
    private val table: String,
    private val columns: List<String>,
    private val values: List<List<Param>>,
) : Command {

    override fun execute(connection: Connection) {
        val columns = columns.joinToString(prefix = "(", postfix = ")")
        val values = values.joinToString { vs ->
            vs.joinToString(prefix = "(", postfix = ")", transform = Param::stringValue)
        }
        val query = "insert into $table $columns values $values"
        connection.prepareStatement(query).use { statement ->
            val params = this.values.flatten()
            var index = 1
            params.forEach { param ->
                index = param.setValue(statement, index)
            }
            statement.executeUpdate()
        }
    }
}