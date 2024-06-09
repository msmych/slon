package uk.matvey.slon

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import javax.sql.DataSource

class DataAccess(
    private val dataSource: DataSource,
) {

    fun execute(query: String) {
        withStatement(query, PreparedStatement::executeUpdate)
    }

    fun <T> query(query: String, read: (ResultSet) -> T): T {
        return withStatement(query) { statement ->
            statement.executeQuery().use(read)
        }
    }

    fun <T> withStatement(query: String, block: (PreparedStatement) -> T): T {
        return withConnection { connection -> connection.prepareStatement(query).use(block) }
    }

    fun <T> withConnection(block: (Connection) -> T): T {
        return dataSource.connection.use(block)
    }
}
