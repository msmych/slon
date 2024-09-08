package uk.matvey.slon.value

import java.sql.PreparedStatement
import java.sql.Types

class PgInt(private val value: Long?) : PgValue() {

    override fun setValue(statement: PreparedStatement, index: Int): Int {
        if (value == null) {
            statement.setNull(index, Types.NULL)
        } else {
            statement.setLong(index, value)
        }
        return index + 1
    }

    companion object {

        fun Long?.toPgInt() = PgInt(this)

        fun Int?.toPgInt() = this?.toLong().toPgInt()
    }
}