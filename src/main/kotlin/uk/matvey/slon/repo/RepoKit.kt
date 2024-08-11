package uk.matvey.slon.repo

import uk.matvey.slon.RecordReader
import uk.matvey.slon.access.AccessKit.deleteFrom
import uk.matvey.slon.access.AccessKit.insertInto
import uk.matvey.slon.access.AccessKit.insertReturning
import uk.matvey.slon.access.AccessKit.update
import uk.matvey.slon.param.Param
import uk.matvey.slon.query.InsertQueryBuilder
import uk.matvey.slon.query.InsertReturningQuery
import uk.matvey.slon.query.Query
import uk.matvey.slon.query.update.UpdateQuery
import uk.matvey.slon.query.update.UpdateQueryBuilder

object RepoKit {

    suspend fun <T> Repo.execute(query: Query<T>): T {
        return access { a -> a.execute(query) }
    }

    suspend fun Repo.executePlain(query: String) {
        access { a -> a.executePlain(query) }
    }

    suspend fun <T> Repo.query(
        query: String,
        params: List<Param> = listOf(),
        read: (RecordReader) -> T
    ): List<T> {
        return access { a -> a.query(query, params, read) }
    }

    suspend fun Repo.insertInto(table: String, query: InsertQueryBuilder.() -> Unit): Int {
        return access { a -> a.insertInto(table, query) }
    }

    suspend fun <T> Repo.insertReturning(
        table: String,
        query: InsertQueryBuilder.() -> InsertReturningQuery<T>
    ): List<T> {
        return access { a -> a.insertReturning(table, query) }
    }

    suspend fun Repo.update(table: String, query: UpdateQueryBuilder.() -> UpdateQuery): Int {
        return access { a -> a.update(table, query) }
    }

    suspend fun <T> Repo.queryOne(
        query: String,
        params: List<Param> = listOf(),
        reader: (RecordReader) -> T
    ): T {
        return access { a -> a.queryOne(query, params, reader) }
    }

    suspend fun <T> Repo.queryOneNullable(
        query: String,
        params: List<Param> = listOf(),
        reader: (RecordReader) -> T
    ): T? {
        return access { a -> a.queryOneNullable(query, params, reader) }
    }

    suspend fun Repo.deleteFrom(table: String, where: String, params: List<Param>): Int {
        return access { a -> a.deleteFrom(table, where, params) }
    }

    suspend fun Repo.deleteFrom(table: String, where: String, vararg params: Param): Int {
        return this.deleteFrom(table, where, params.toList())
    }
}