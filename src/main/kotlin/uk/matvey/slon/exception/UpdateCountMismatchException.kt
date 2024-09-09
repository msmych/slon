package uk.matvey.slon.exception

class UpdateCountMismatchException(
    expected: Int,
    actual: Int,
) : RuntimeException("Expected $expected updates but got $actual")