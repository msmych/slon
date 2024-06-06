package uk.matvey.slon.exception

import org.postgresql.util.PSQLException

class PgUniqueViolationException(cause: PSQLException) : RuntimeException(cause)