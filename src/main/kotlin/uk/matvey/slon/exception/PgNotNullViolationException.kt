package uk.matvey.slon.exception

import org.postgresql.util.PSQLException

class PgNotNullViolationException(cause: PSQLException) : RuntimeException(cause)