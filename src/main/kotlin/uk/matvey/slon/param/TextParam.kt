package uk.matvey.slon.param

import java.sql.PreparedStatement
import java.sql.Types

class TextParam(private val value: String?) : Param() {

    override fun setValue(statement: PreparedStatement, index: Int): Int {
        if (value == null) {
            statement.setNull(index, Types.NULL)
        } else {
            statement.setString(index, value)
        }
        return index + 1
    }

    companion object {

        fun text(value: String?) = TextParam(value)
    }
}