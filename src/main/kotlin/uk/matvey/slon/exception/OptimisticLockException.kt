package uk.matvey.slon.exception

class OptimisticLockException(
    table: String,
    condition: String,
) : RuntimeException("Condition was not satisfied. Table: [$table], condition: [$condition]")