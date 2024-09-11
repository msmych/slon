package uk.matvey.slon.value

import java.sql.PreparedStatement
import java.sql.Types.NULL

class PgBool(private val value: Boolean?) : PgValue() {

    override fun setValue(statement: PreparedStatement, index: Int): Int {
        if (value == null) {
            statement.setNull(index, NULL)
        } else {
            statement.setBoolean(index, value)
        }
        return index + 1
    }

    companion object {

        fun Boolean?.toPgBool() = PgBool(this)
    }
}