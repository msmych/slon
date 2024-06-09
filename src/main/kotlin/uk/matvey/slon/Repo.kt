package uk.matvey.slon

import org.postgresql.util.PSQLException
import org.postgresql.util.PSQLState.NOT_NULL_VIOLATION
import org.postgresql.util.PSQLState.UNIQUE_VIOLATION
import uk.matvey.slon.command.Command
import uk.matvey.slon.command.Insert
import uk.matvey.slon.exception.PgNotNullViolationException
import uk.matvey.slon.exception.PgUniqueViolationException

class Repo(private val dataAccess: DataAccess) {

    fun execute(commands: List<Command>) {
        dataAccess.withConnection { connection ->
            connection.autoCommit = false
            commands.forEach { command ->
                connection.prepareStatement(command.generateQuery()).use { statement ->
                    command.setValues(statement, 1)
                    try {
                        val count = statement.executeUpdate()
                        command.onResult(count)
                    } catch (e: Exception) {
                        connection.rollback()
                        throw when (e) {
                            is PSQLException -> when (e.sqlState) {
                                NOT_NULL_VIOLATION.state -> PgNotNullViolationException(e)
                                UNIQUE_VIOLATION.state -> PgUniqueViolationException(e)
                                else -> e
                            }
                            else -> e
                        }
                    }
                }
            }
            connection.commit()
        }
    }

    fun <T> query(query: String, params: List<QueryParam>, read: (RecordReader) -> T): List<T> {
        return dataAccess.withStatement(query) { statement ->
            var index = 1
            params.forEach { param ->
                index = param.setValue(statement, index)
            }
            statement.executeQuery().use { resultSet ->
                val list = mutableListOf<T>()
                while (resultSet.next()) {
                    list += read(RecordReader(resultSet))
                }
                list
            }
        }
    }

    fun <T> query(query: String, read: (RecordReader) -> T): List<T> {
        return query(query, listOf(), read)
    }

    fun execute(vararg commands: Command) {
        return execute(commands.toList())
    }

    fun execute(builder: Insert.Builder) = execute(builder.build())
}