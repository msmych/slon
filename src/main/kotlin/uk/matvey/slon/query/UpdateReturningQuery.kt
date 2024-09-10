package uk.matvey.slon.query

import uk.matvey.slon.value.PgValue

abstract class UpdateReturningQuery<T>(
    private val updateQuery: UpdateQuery,
    private val returning: ReturningClause,
) : Query<T> {

    override fun sql(): String {
        return listOf(updateQuery.sql(), returning.sql()).joinToString(" ")
    }

    override fun params(): List<PgValue> {
        return updateQuery.params()
    }
}