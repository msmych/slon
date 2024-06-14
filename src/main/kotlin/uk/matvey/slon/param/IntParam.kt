package uk.matvey.slon.param

import java.sql.PreparedStatement
import java.sql.Types

class IntParam(private val value: Long?) : Param() {

    override fun setValue(statement: PreparedStatement, index: Int): Int {
        if (value == null) {
            statement.setNull(index, Types.NULL)
        } else {
            statement.setLong(index, value)
        }
        return index + 1
    }

    companion object {

        fun int(value: Long?) = IntParam(value)

        fun int(value: Int?) = int(value?.toLong())
    }
}