package uk.matvey.slon

import org.postgresql.util.PSQLException
import org.postgresql.util.PSQLState.NOT_NULL_VIOLATION
import org.postgresql.util.PSQLState.UNIQUE_VIOLATION
import uk.matvey.slon.command.Command
import uk.matvey.slon.command.Insert
import uk.matvey.slon.exception.PgNotNullViolationException
import uk.matvey.slon.exception.PgUniqueViolationException

class Repo(val dataAccess: DataAccess) {

    fun execute(commands: List<Command>) {
        dataAccess.withConnection { conn ->
            conn.autoCommit = false
            commands.forEach { command ->
                conn.prepareStatement(command.generateQuery()).use { statement ->
                    command.setValues(statement, 1)
                    try {
                        statement.executeUpdate()
                    } catch (e: Exception) {
                        conn.rollback()
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
            conn.commit()
        }
    }

    fun execute(vararg commands: Command) {
        return execute(commands.toList())
    }

    fun execute(insert: Insert.Builder) = execute(insert.build())
}