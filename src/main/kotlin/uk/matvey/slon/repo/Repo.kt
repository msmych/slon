package uk.matvey.slon.repo

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.postgresql.util.PSQLException
import org.postgresql.util.PSQLState.NOT_NULL_VIOLATION
import org.postgresql.util.PSQLState.UNIQUE_VIOLATION
import uk.matvey.slon.access.Access
import uk.matvey.slon.exception.PgNotNullViolationException
import uk.matvey.slon.exception.PgUniqueViolationException
import javax.sql.DataSource

class Repo(
    private val dataSource: DataSource,
    private val coroutineDispatcher: CoroutineDispatcher = Dispatchers.IO,
) {

    suspend fun <T> access(block: (Access) -> T): T {
        return withContext(coroutineDispatcher) {
            dataSource.connection.use { connection ->
                val access = Access(connection)
                try {
                    val result = block(access)
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
    }
}