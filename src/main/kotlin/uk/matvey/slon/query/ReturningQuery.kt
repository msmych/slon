package uk.matvey.slon.query

import uk.matvey.slon.RecordReader
import uk.matvey.slon.value.PgValue

abstract class ReturningQuery<T>(
    private val update: Update,
    private val returning: Returning,
) : Query<T> {

    override fun sql(): String {
        return listOf(update.sql(), returning.sql()).joinToString(" ")
    }

    override fun params(): List<PgValue> {
        return update.params()
    }

    companion object {

        fun <T> Update.returning(returning: Returning, read: (RecordReader) -> T): ReturningQuery<T> {
            return object : ReturningQuery<T>(this@returning, returning) {
                override fun read(reader: RecordReader): T {
                    return read(reader)
                }
            }
        }

        fun <T> Update.returning(returning: List<String>, read: (RecordReader) -> T): ReturningQuery<T> {
            return this.returning(Returning(returning), read)
        }

        fun <T> Update.returning(read: (RecordReader) -> T): ReturningQuery<T> {
            return this.returning(Returning.all(), read)
        }
    }
}