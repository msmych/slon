package uk.matvey.slon.param

import java.sql.PreparedStatement

class PlainParam(private val value: String) : Param() {

    override val stringValue = this.value

    override fun setValue(statement: PreparedStatement, index: Int): Int {
        return index
    }

    companion object {

        fun plainParam(value: String) = PlainParam(value)

        val currentDate = plainParam("current_date")
        val currentTimestamp = plainParam("current_timestamp")

        fun genRandomUuid() = plainParam("gen_random_uuid()")

        fun now() = plainParam("now()")
    }
}