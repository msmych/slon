package uk.matvey.slon.access

import uk.matvey.slon.value.PgValue
import uk.matvey.slon.query.InsertQueryBuilder
import uk.matvey.slon.query.InsertReturningQuery
import uk.matvey.slon.query.update.DeleteQueryBuilder
import uk.matvey.slon.query.update.UpdateQuery
import uk.matvey.slon.query.update.UpdateQueryBuilder

object AccessKit {

    fun Access.insertInto(table: String, query: InsertQueryBuilder.() -> Unit): Int {
        val builder = InsertQueryBuilder.insertInto(table)
        builder.query()
        return execute(builder.build())
    }

    fun <T> Access.insertReturning(table: String, query: InsertQueryBuilder.() -> InsertReturningQuery<T>): List<T> {
        val builder = InsertQueryBuilder.insertInto(table)
        return execute(builder.query())
    }

    fun <T> Access.insertReturningOne(table: String, query: InsertQueryBuilder.() -> InsertReturningQuery<T>): T {
        val builder = InsertQueryBuilder.insertInto(table)
        return execute(builder.query().one())
    }

    fun <T> Access.insertReturningOneOrNull(
        table: String,
        query: InsertQueryBuilder.() -> InsertReturningQuery<T>
    ): T? {
        val builder = InsertQueryBuilder.insertInto(table)
        return execute(builder.query().oneOrNull())
    }

    fun Access.update(table: String, query: UpdateQueryBuilder.() -> UpdateQuery): Int {
        val builder = UpdateQueryBuilder.update(table)
        return execute(builder.query())
    }

    fun Access.deleteFrom(table: String, where: String, params: List<PgValue>): Int {
        return execute(DeleteQueryBuilder.deleteFrom(table).where(where, params))
    }

    fun Access.deleteFrom(table: String, where: String, vararg params: PgValue): Int {
        return this.deleteFrom(table, where, params.toList())
    }
}