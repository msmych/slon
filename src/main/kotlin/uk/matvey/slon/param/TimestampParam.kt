package uk.matvey.slon.param

import java.sql.PreparedStatement
import java.sql.Timestamp
import java.sql.Types
import java.time.Instant

class TimestampParam(private val value: Timestamp?) : Param() {

    override fun setValue(statement: PreparedStatement, index: Int): Int {
        if (value == null) {
            statement.setNull(index, Types.NULL)
        } else {
            statement.setTimestamp(index, value)
        }
        return index + 1
    }

    companion object {
        fun timestamp(value: Instant?) = TimestampParam(value?.let(Timestamp::from))
    }
}