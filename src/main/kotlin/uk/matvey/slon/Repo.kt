package uk.matvey.slon

import org.postgresql.util.PSQLException
import org.postgresql.util.PSQLState.NOT_NULL_VIOLATION
import org.postgresql.util.PSQLState.UNIQUE_VIOLATION
import uk.matvey.slon.command.Command
import uk.matvey.slon.command.Insert
import uk.matvey.slon.exception.PgNotNullViolationException
import uk.matvey.slon.exception.PgUniqueViolationException

class Repo(val dataAccess: DataAccess) {

    fun execute(command: Command) {
        val query = command.generateQuery()
        dataAccess.withStatement(query) { statement ->
            statement.connection.autoCommit = false
            command.setValues(statement, 1)
            try {
                statement.executeUpdate()
            } catch (e: Exception) {
                statement.connection.rollback()
                throw when (e) {
                    is PSQLException -> when (e.sqlState) {
                        NOT_NULL_VIOLATION.state -> PgNotNullViolationException(e)
                        UNIQUE_VIOLATION.state -> PgUniqueViolationException(e)
                        else -> e
                    }
                    else -> e
                }
            }
            statement.connection.commit()
        }
    }

    fun execute(insert: Insert.Builder) = execute(insert.build())
}