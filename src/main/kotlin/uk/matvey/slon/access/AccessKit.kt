package uk.matvey.slon.access

import uk.matvey.slon.RecordReader
import uk.matvey.slon.exception.UpdateCountMismatchException
import uk.matvey.slon.query.DeleteQueryBuilder.Companion.deleteFrom
import uk.matvey.slon.query.InsertOneQueryBuilder
import uk.matvey.slon.query.InsertQueryBuilder
import uk.matvey.slon.query.Query.Companion.plainQuery
import uk.matvey.slon.query.Update
import uk.matvey.slon.query.UpdateQueryBuilder
import uk.matvey.slon.value.PgValue

object AccessKit {

    fun <T> Access.queryAll(query: String, params: List<PgValue>, read: (RecordReader) -> T): List<T> {
        return query(plainQuery(query, params, read))
    }

    fun <T> Access.queryOneOrNull(query: String, params: List<PgValue>, read: (RecordReader) -> T): T? {
        return queryAll(query, params, read).singleOrNull()
    }

    fun <T> Access.queryOne(query: String, params: List<PgValue>, read: (RecordReader) -> T): T {
        return queryAll(query, params, read).single()
    }

    fun <T> Access.queryAll(query: String, read: (RecordReader) -> T): List<T> {
        return queryAll(query, listOf(), read)
    }

    fun <T> Access.queryOneOrNull(query: String, read: (RecordReader) -> T): T? {
        return queryAll(query, read).singleOrNull()
    }

    fun <T> Access.queryOne(query: String, read: (RecordReader) -> T): T {
        return queryAll(query, read).single()
    }

    fun Access.plainUpdate(update: String, params: List<PgValue>): Int {
        return execute(Update.plainUpdate(update, params))
    }

    fun Access.plainUpdate(update: String, vararg params: PgValue): Int {
        return plainUpdate(update, params.toList())
    }

    fun Access.insertInto(table: String, block: InsertQueryBuilder.() -> InsertQueryBuilder): Int {
        return execute(InsertQueryBuilder.insertInto(table).block().build())
    }

    fun Access.insertOneInto(table: String): Int {
        return execute(InsertOneQueryBuilder.insertOneInto(table).build())
    }

    fun Access.insertOneInto(table: String, block: InsertOneQueryBuilder.() -> InsertOneQueryBuilder): Int {
        return execute(InsertOneQueryBuilder.insertOneInto(table).block().build())
    }

    fun Access.update(table: String, block: UpdateQueryBuilder.() -> UpdateQueryBuilder): Int {
        return execute(UpdateQueryBuilder.update(table).block().build())
    }

    fun Access.delete(from: String, where: String, params: List<PgValue>): Int {
        return execute(deleteFrom(from).where(where, params))
    }

    fun Access.delete(from: String, where: String, vararg params: PgValue): Int {
        return delete(from, where, params.toList())
    }

    fun Access.updateSingle(update: Update) {
        val updates = execute(update)
        if (updates != 1) {
            throw UpdateCountMismatchException(1, updates)
        }
    }
}