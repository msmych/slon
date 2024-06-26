package uk.matvey.slon

import org.postgresql.util.PSQLException
import org.postgresql.util.PSQLState.NOT_NULL_VIOLATION
import org.postgresql.util.PSQLState.UNIQUE_VIOLATION
import uk.matvey.slon.exception.PgNotNullViolationException
import uk.matvey.slon.exception.PgUniqueViolationException
import uk.matvey.slon.param.Param
import uk.matvey.slon.query.Query
import javax.sql.DataSource

class Repo(private val dataSource: DataSource) {

    fun <T> access(block: (Access) -> T): T {
        return dataSource.connection.use { connection ->
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

    fun <T> execute(query: Query<T>): T {
        return access { a -> a.execute(query) }
    }

    fun executePlain(query: String) {
        access { a -> a.executePlain(query) }
    }

    fun insertOne(into: String, vararg values: Pair<String, Param>) {
        access { a -> a.insertOne(into, *values) }
    }

    fun <T> query(
        query: String,
        params: List<Param> = listOf(),
        read: (RecordReader) -> T
    ): List<T> {
        return access { a -> a.query(query, params, read) }
    }

    fun <T> queryOne(
        query: String,
        params: List<Param> = listOf(),
        reader: (RecordReader) -> T
    ): T {
        return access { a -> a.queryOne(query, params, reader) }
    }

    fun <T> queryOneNullable(
        query: String,
        params: List<Param> = listOf(),
        reader: (RecordReader) -> T
    ): T? {
        return access { a -> a.queryOneNullable(query, params, reader) }
    }
}