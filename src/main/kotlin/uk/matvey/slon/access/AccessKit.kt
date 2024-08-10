package uk.matvey.slon.access

import uk.matvey.slon.query.InsertQueryBuilder
import uk.matvey.slon.query.InsertReturningQuery
import uk.matvey.slon.query.UpdateQueryBuilder
import uk.matvey.slon.query.update.UpdateQuery

object AccessKit {

    fun Access.insertInto(table: String, query: InsertQueryBuilder.() -> Unit) {
        val builder = InsertQueryBuilder.insert(table)
        builder.query()
        execute(builder.build())
    }

    fun <T> Access.insertReturning(table: String, query: InsertQueryBuilder.() -> InsertReturningQuery<T>): List<T> {
        val builder = InsertQueryBuilder.insert(table)
        return execute(builder.query())
    }

    fun <T> Access.insertReturningOne(table: String, query: InsertQueryBuilder.() -> InsertReturningQuery<T>): T {
        val builder = InsertQueryBuilder.insert(table)
        return execute(builder.query().one())
    }

    fun <T> Access.insertReturningOneNullable(
        table: String,
        query: InsertQueryBuilder.() -> InsertReturningQuery<T>
    ): T? {
        val builder = InsertQueryBuilder.insert(table)
        return execute(builder.query().oneNullable())
    }

    fun Access.update(table: String, query: UpdateQueryBuilder.() -> UpdateQuery) {
        val builder = UpdateQueryBuilder.update(table)
        execute(builder.query())
    }
}