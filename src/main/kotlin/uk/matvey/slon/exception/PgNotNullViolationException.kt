package uk.matvey.slon.exception

import org.postgresql.util.PSQLException

class PgNotNullViolationException(
    val table: String?,
    val column: String?,
    cause: PSQLException,
) : RuntimeException(cause) {

    companion object {
        fun from(e: PSQLException): PgNotNullViolationException {
            val table = e.serverErrorMessage?.table
            val column = e.serverErrorMessage?.column
            return PgNotNullViolationException(table, column, e)
        }
    }
}