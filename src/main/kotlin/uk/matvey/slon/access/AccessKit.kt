package uk.matvey.slon.access

import uk.matvey.slon.RecordReader
import uk.matvey.slon.exception.UpdateCountMismatchException
import uk.matvey.slon.query.Query.Companion.plainQuery
import uk.matvey.slon.query.Update
import uk.matvey.slon.query.Update.Companion.plainUpdate
import uk.matvey.slon.value.PgValue

object AccessKit {

    fun <T> Access.query(query: String, params: List<PgValue>, read: (RecordReader) -> T): List<T> {
        return query(plainQuery(query, params, read))
    }

    fun <T> Access.queryOneOrNull(query: String, params: List<PgValue>, read: (RecordReader) -> T): T? {
        return query(query, params, read).singleOrNull()
    }

    fun <T> Access.queryOne(query: String, params: List<PgValue>, read: (RecordReader) -> T): T {
        return query(query, params, read).single()
    }

    fun <T> Access.query(query: String, read: (RecordReader) -> T): List<T> {
        return query(query, listOf(), read)
    }

    fun <T> Access.queryOneOrNull(query: String, read: (RecordReader) -> T): T? {
        return query(query, read).singleOrNull()
    }

    fun <T> Access.queryOne(query: String, read: (RecordReader) -> T): T {
        return query(query, read).single()
    }

    fun Access.update(update: String, params: List<PgValue>): Int {
        return execute(plainUpdate(update, params))
    }

    fun Access.update(update: String, vararg params: PgValue): Int {
        return update(update, params.toList())
    }

    fun Access.updateSingle(update: Update) {
        val updates = execute(update)
        if (updates != 1) {
            throw UpdateCountMismatchException(1, updates)
        }
    }
}