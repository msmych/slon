package uk.matvey.slon.query

import uk.matvey.slon.value.PgValue

abstract class InsertReturningQuery<T>(
    private val insertQuery: InsertQuery,
    private val returning: ReturningClause,
) : Query<T> {

    override fun sql(): String {
        return listOf(insertQuery.sql(), returning.sql()).joinToString(" ")
    }

    override fun params(): List<PgValue> {
        return insertQuery.params()
    }
}