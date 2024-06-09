package uk.matvey.slon.exception

class OptimisticLockException(
    table: String,
    condition: String,
) : RuntimeException("Update condition was not satisfied. Table: [$table], condition: [$condition]")