package uk.matvey.slon

import uk.matvey.slon.query.Query
import java.sql.Connection
import java.sql.Connection.TRANSACTION_READ_COMMITTED

class Access(private val connection: Connection) {

    init {
        connection.transactionIsolation = TRANSACTION_READ_COMMITTED
        connection.autoCommit = false
    }

    fun <T> execute(query: Query<T>): T {
        return query.execute(connection)
    }
}