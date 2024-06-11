package uk.matvey.slon

import uk.matvey.slon.command.Command
import uk.matvey.slon.param.Param
import uk.matvey.slon.query.Query
import uk.matvey.slon.query.Select
import java.sql.Connection
import java.sql.Connection.TRANSACTION_READ_COMMITTED

class DataAccess(private val connection: Connection) {

    init {
        connection.transactionIsolation = TRANSACTION_READ_COMMITTED
        connection.autoCommit = false
    }

    fun execute(command: Command) {
        command.execute(connection)
    }

    fun <T> execute(query: Query<T>): T {
        return query.execute(connection)
    }

    fun execute(query: String) {
        connection.prepareStatement(query).executeUpdate()
    }

    fun <T> query(query: String, params: List<Param>, read: (RecordReader) -> T): List<T> {
        return execute(Select(query, params, read))
    }
}
