package uk.matvey.slon

import org.postgresql.util.PSQLException
import org.postgresql.util.PSQLState.NOT_NULL_VIOLATION
import org.postgresql.util.PSQLState.UNIQUE_VIOLATION
import uk.matvey.slon.command.Command
import uk.matvey.slon.exception.PgNotNullViolationException
import uk.matvey.slon.exception.PgUniqueViolationException
import uk.matvey.slon.param.Param
import uk.matvey.slon.query.Query
import uk.matvey.slon.query.Select
import javax.sql.DataSource

class Repo(private val dataSource: DataSource) {

    fun <T> access(block: (DataAccess) -> T): T {
        return dataSource.connection.use { connection ->
            val dataAccess = DataAccess(connection)
            try {
                val result = block(dataAccess)
                connection.commit()
                result
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

    fun execute(vararg commands: Command) {
        return access { dataAccess ->
            commands.forEach(dataAccess::execute)
        }
    }

    fun <T> query(query: Query<T>): T {
        return access { dataAccess ->
            dataAccess.execute(query)
        }
    }

    fun <T> query(query: String, params: List<Param>, read: (RecordReader) -> T): List<T> {
        return query(Select(query, params, read))
    }
}