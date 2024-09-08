package uk.matvey.slon.value

import java.sql.PreparedStatement
import java.sql.Types

class PgArray(
    private val type: String,
    private val value: Collection<*>?
) : PgValue() {

    override fun setValue(statement: PreparedStatement, index: Int): Int {
        if (value == null) {
            statement.setNull(index, Types.NULL)
        } else {
            val array = statement.connection.createArrayOf(type, value.toTypedArray())
            statement.setArray(index, array)
        }
        return index + 1
    }

    companion object {

        fun Collection<String>?.toPgArray() = PgArray("text", this)
    }
}