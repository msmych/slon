package uk.matvey.slon.command

import uk.matvey.slon.QueryParam
import java.sql.PreparedStatement

sealed interface Command {

    fun generateQuery(): String

    fun collectParams(): List<QueryParam>

    @Suppress("NAME_SHADOWING")
    fun setValues(statement: PreparedStatement, index: Int): Int {
        var index = index
        collectParams().forEach { param ->
            index = param.setValue(statement, index)
        }
        return index
    }
}
