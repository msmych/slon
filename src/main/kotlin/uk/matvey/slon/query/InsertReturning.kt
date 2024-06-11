package uk.matvey.slon.query

import uk.matvey.slon.param.Param
import uk.matvey.slon.RecordReader
import java.sql.Connection

class InsertReturning<T>(
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
        val returning = returning.joinToString(prefix = "(", postfix = ")")
        val query = "insert into $table $columns values $values returning $returning"
        return connection.prepareStatement(query).use { statement ->
            val params = this.values.flatten()
            var index = 1
            params.forEach { param ->
                index = param.setValue(statement, index)
            }
            val resultSet = statement.executeQuery()
            val list = mutableListOf<T>()
            while (resultSet.next()) {
                list += read(RecordReader(resultSet))
            }
            list
        }
    }
}