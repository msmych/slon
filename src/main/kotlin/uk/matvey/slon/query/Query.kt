package uk.matvey.slon.query

import uk.matvey.slon.RecordReader
import uk.matvey.slon.value.PgValue

interface Query<T> {

    fun sql(): String

    fun params(): List<PgValue>

    fun read(reader: RecordReader): T

    companion object {

        fun <T> plainQuery(sql: String, params: List<PgValue> = listOf(), read: (RecordReader) -> T): Query<T> {
            return object : Query<T> {
                override fun sql() = sql
                override fun params() = params
                override fun read(reader: RecordReader) = read(reader)
            }
        }
    }
}