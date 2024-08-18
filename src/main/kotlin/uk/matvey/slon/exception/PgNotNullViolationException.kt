package uk.matvey.slon.exception

import org.postgresql.util.PSQLException

class PgNotNullViolationException(
    val table: String?,
    val column: String?,
    cause: PSQLException,
) : RuntimeException(cause) {

    companion object {
        fun from(e: PSQLException): PgNotNullViolationException {
            val table = e.message
                ?.substringAfter("of relation \"", missingDelimiterValue = "?")
                ?.substringBefore("\"")
            val column = e.message
                ?.substringAfter("null value in column \"", missingDelimiterValue = "?")
                ?.substringBefore("\"")
            return PgNotNullViolationException(table, column, e)
        }
    }
}