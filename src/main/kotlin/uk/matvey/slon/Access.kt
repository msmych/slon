package uk.matvey.slon

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

    fun insertOne(into: String, vararg values: Pair<String, Param>) {
        InsertBuilder.insertOne(into, *values).execute(connection)
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
        reader: (RecordReader) -> T
    ): T? {
        return query(query, params, reader).singleOrNull()
    }

    fun <T> queryOne(
        query: String,
        params: List<Param> = listOf(),
        reader: (RecordReader) -> T
    ): T {
        return query(query, params, reader).single()
    }
}