package uk.matvey.slon.value

import java.sql.Date
import java.sql.PreparedStatement
import java.sql.Types.NULL
import java.time.LocalDate

class PgDate(private val value: Date?) : PgValue() {

    override fun setValue(statement: PreparedStatement, index: Int): Int {
        if (value == null) {
            statement.setNull(index, NULL)
        } else {
            statement.setDate(index, value)
        }
        return index + 1
    }

    companion object {

        fun LocalDate?.toPgDate() = PgDate(this?.let(Date::valueOf))
    }
}