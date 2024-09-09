package uk.matvey.slon.access

import uk.matvey.slon.RecordReader
import uk.matvey.slon.query.Query
import uk.matvey.slon.query.Update
import java.sql.Connection
import java.sql.Connection.TRANSACTION_READ_COMMITTED

class Access(private val connection: Connection) {

    init {
        connection.transactionIsolation = TRANSACTION_READ_COMMITTED
        connection.autoCommit = false
    }

    fun execute(update: Update): Int {
        return connection.prepareStatement(update.sql()).use { statement ->
            var index = 1
            update.params().forEach { param ->
                index = param.setValue(statement, index)
            }
            statement.executeUpdate()
        }
    }

    fun <T> query(query: Query<T>): List<T> {
        return connection.prepareStatement(query.sql()).use { statement ->
            var index = 1
            query.params().forEach { param ->
                index = param.setValue(statement, index)
            }
            statement.executeQuery().use { resultSet ->
                val list = mutableListOf<T>()
                while (resultSet.next()) {
                    list += query.read(RecordReader(resultSet))
                }
                list
            }
        }
    }
}