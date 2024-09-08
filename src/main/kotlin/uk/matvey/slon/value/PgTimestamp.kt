package uk.matvey.slon.value

import java.sql.PreparedStatement
import java.sql.Timestamp
import java.sql.Types
import java.time.Instant
import java.time.LocalDateTime

class PgTimestamp(private val value: Timestamp?) : PgValue() {

    override fun setValue(statement: PreparedStatement, index: Int): Int {
        if (value == null) {
            statement.setNull(index, Types.NULL)
        } else {
            statement.setTimestamp(index, value)
        }
        return index + 1
    }

    companion object {

        fun Instant?.toPgTimestamp() = PgTimestamp(this?.let(Timestamp::from))

        fun LocalDateTime?.toPgTimestamp() = PgTimestamp(this?.let(Timestamp::valueOf))
    }
}