package uk.matvey.slon.repo

import uk.matvey.slon.RecordReader
import uk.matvey.slon.param.Param
import uk.matvey.slon.query.Query

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

    suspend fun Repo.insertOne(into: String, vararg values: Pair<String, Param>) {
        access { a -> a.insertOne(into, *values) }
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
}