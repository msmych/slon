package uk.matvey.slon.param

import java.sql.PreparedStatement
import java.sql.Types

class ArrayParam(
    private val type: String,
    private val value: Collection<*>?
) : Param() {

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
        fun textArray(value: Collection<String>?) = ArrayParam("text", value)
    }
}