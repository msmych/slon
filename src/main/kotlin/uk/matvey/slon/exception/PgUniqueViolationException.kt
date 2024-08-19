package uk.matvey.slon.exception

import org.postgresql.util.PSQLException

class PgUniqueViolationException(
    val constraint: String?,
    cause: PSQLException,
) : RuntimeException(cause) {

    companion object {
        fun from(e: PSQLException): PgUniqueViolationException {
            val constraint = e.serverErrorMessage?.constraint
            return PgUniqueViolationException(constraint, e)
        }
    }
}