package uk.matvey.slon.param

import java.sql.PreparedStatement

sealed class Param {

    open val stringValue = "?"

    abstract fun setValue(statement: PreparedStatement, index: Int): Int

    companion object {


    }
}
