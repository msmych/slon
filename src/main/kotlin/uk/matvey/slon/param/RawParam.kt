package uk.matvey.slon.param

import java.sql.PreparedStatement

class RawParam(private val value: String) : Param() {

    override val stringValue = this.value

    override fun setValue(statement: PreparedStatement, index: Int): Int {
        return index
    }

    companion object {

        fun raw(value: String) = RawParam(value)
    }
}