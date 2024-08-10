package uk.matvey.slon.access

import uk.matvey.slon.RecordReader
import uk.matvey.slon.param.Param
import uk.matvey.slon.query.Query
import uk.matvey.slon.query.RawQuery
import uk.matvey.slon.query.update.RawUpdateQuery.Companion.rawUpdate
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

    fun executePlain(query: String) {
        execute(rawUpdate(query))
    }

    fun <T> query(
        query: String,
        params: List<Param> = listOf(),
        read: (RecordReader) -> T
    ): List<T> {
        return execute(RawQuery(query, params, read))
    }

    fun <T> queryOneNullable(
        query: String,
        params: List<Param> = listOf(),
        read: (RecordReader) -> T
    ): T? {
        return execute(RawQuery(query, params, read).oneNullable())
    }

    fun <T> queryOne(
        query: String,
        params: List<Param> = listOf(),
        read: (RecordReader) -> T
    ): T {
        return execute(RawQuery(query, params, read).one())
    }
}