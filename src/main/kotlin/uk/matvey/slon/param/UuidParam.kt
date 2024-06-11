package uk.matvey.slon.param

import java.sql.PreparedStatement
import java.sql.Types

class UuidParam(private val value: java.util.UUID?) : Param() {

    override fun setValue(statement: PreparedStatement, index: Int): Int {
        if (value == null) {
            statement.setNull(index, Types.NULL)
        } else {
            statement.setObject(index, value)
        }
        return index + 1
    }

    companion object {

        fun uuid(value: java.util.UUID?) = UuidParam(value)
    }
}