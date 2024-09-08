package uk.matvey.slon.value

import java.sql.PreparedStatement
import java.sql.Types

class PgText(private val value: String?) : PgValue() {

    override fun setValue(statement: PreparedStatement, index: Int): Int {
        if (value == null) {
            statement.setNull(index, Types.NULL)
        } else {
            statement.setString(index, value)
        }
        return index + 1
    }

    companion object {

        fun String?.toPgText() = PgText(this)

        fun <T : Enum<T>> Enum<T>?.toPgText() = this?.name.toPgText()
    }
}