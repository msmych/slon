package uk.matvey.slon.param

import java.sql.Date
import java.sql.PreparedStatement
import java.sql.Types.NULL
import java.time.LocalDate

class DateParam(private val value: Date?) : Param() {

    override fun setValue(statement: PreparedStatement, index: Int): Int {
        if (value == null) {
            statement.setNull(index, NULL)
        } else {
            statement.setDate(index, value)
        }
        return index + 1
    }

    companion object {
        fun date(value: LocalDate?) = DateParam(value?.let(Date::valueOf))
    }
}