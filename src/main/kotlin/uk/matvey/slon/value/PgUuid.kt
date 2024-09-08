package uk.matvey.slon.value

import java.sql.PreparedStatement
import java.sql.Types
import java.util.UUID

class PgUuid(private val value: java.util.UUID?) : PgValue() {

    override fun setValue(statement: PreparedStatement, index: Int): Int {
        if (value == null) {
            statement.setNull(index, Types.NULL)
        } else {
            statement.setObject(index, value)
        }
        return index + 1
    }

    companion object {

        fun UUID?.toPgUuid() = PgUuid(this)
    }
}