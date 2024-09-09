package uk.matvey.slon.query

import uk.matvey.slon.value.PgValue

abstract class InsertReturningQuery<T>(
    private val insertQuery: InsertQuery,
    private val returning: List<String>,
) : Query<T> {

    override fun sql(): String {
        return listOf(
            insertQuery.sql(),
            "returning",
            returning.takeIf { it.isNotEmpty() }?.joinToString(", ") ?: "*",
        ).joinToString(" ")
    }

    override fun params(): List<PgValue> {
        return insertQuery.params()
    }
}