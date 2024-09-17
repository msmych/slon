package uk.matvey.slon.repo

import uk.matvey.slon.RecordReader
import uk.matvey.slon.access.AccessKit.delete
import uk.matvey.slon.access.AccessKit.insertInto
import uk.matvey.slon.access.AccessKit.insertOneInto
import uk.matvey.slon.access.AccessKit.plainUpdate
import uk.matvey.slon.access.AccessKit.queryAll
import uk.matvey.slon.access.AccessKit.queryOne
import uk.matvey.slon.access.AccessKit.queryOneOrNull
import uk.matvey.slon.access.AccessKit.update
import uk.matvey.slon.query.InsertOneQueryBuilder
import uk.matvey.slon.query.InsertQueryBuilder
import uk.matvey.slon.query.UpdateQueryBuilder
import uk.matvey.slon.value.PgValue

object RepoKit {

    fun <T> Repo.queryAll(query: String, params: List<PgValue>, read: (RecordReader) -> T): List<T> {
        return access { a -> a.queryAll(query, params, read) }
    }

    fun <T> Repo.queryOneOrNull(query: String, params: List<PgValue>, read: (RecordReader) -> T): T? {
        return access { a -> a.queryOneOrNull(query, params, read) }
    }

    fun <T> Repo.queryOne(query: String, params: List<PgValue>, read: (RecordReader) -> T): T {
        return access { a -> a.queryOne(query, params, read) }
    }

    fun <T> Repo.queryAll(query: String, read: (RecordReader) -> T): List<T> {
        return access { a -> a.queryAll(query, read) }
    }

    fun <T> Repo.queryOneOrNull(query: String, read: (RecordReader) -> T): T? {
        return access { a -> a.queryOneOrNull(query, read) }
    }

    fun <T> Repo.queryOne(query: String, read: (RecordReader) -> T): T {
        return access { a -> a.queryOne(query, read) }
    }

    fun Repo.plainUpdate(update: String, params: List<PgValue>): Int {
        return access { a -> a.plainUpdate(update, params) }
    }

    fun Repo.plainUpdate(update: String, vararg params: PgValue): Int {
        return plainUpdate(update, params.toList())
    }

    fun Repo.insertInto(table: String, block: InsertQueryBuilder.() -> InsertQueryBuilder): Int {
        return access { a -> a.insertInto(table, block) }
    }

    fun Repo.insertOneInto(table: String): Int {
        return access { a -> a.insertOneInto(table) }
    }

    fun Repo.insertOneInto(table: String, block: InsertOneQueryBuilder.() -> InsertOneQueryBuilder): Int {
        return access { a -> a.insertOneInto(table, block) }
    }

    fun Repo.update(table: String, block: UpdateQueryBuilder.() -> UpdateQueryBuilder): Int {
        return access { a -> a.update(table, block) }
    }

    fun Repo.delete(from: String, where: String, params: List<PgValue>): Int {
        return access { a -> a.delete(from, where, params) }
    }

    fun Repo.delete(from: String, where: String, vararg params: PgValue): Int {
        return delete(from, where, params.toList())
    }
}