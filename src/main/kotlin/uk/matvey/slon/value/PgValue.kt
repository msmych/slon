package uk.matvey.slon.value

import java.sql.PreparedStatement

sealed class PgValue {

    open val placeholder = "?"

    abstract fun setValue(statement: PreparedStatement, index: Int): Int
}
