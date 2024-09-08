package uk.matvey.slon.value

import java.sql.PreparedStatement

class Pg(private val value: String) : PgValue() {

    override val placeholder = this.value

    override fun setValue(statement: PreparedStatement, index: Int): Int {
        return index
    }

    companion object {

        fun plain(value: String) = Pg(value)

        fun genRandomUuid() = plain("gen_random_uuid()")

        fun now() = plain("now()")
    }
}