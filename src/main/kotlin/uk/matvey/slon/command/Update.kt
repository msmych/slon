package uk.matvey.slon.command

import uk.matvey.slon.exception.OptimisticLockException
import uk.matvey.slon.param.Param
import java.sql.Connection

class Update(
    private val table: String,
    private val values: List<Pair<String, Param>>,
    private var condition: String,
    private var conditionParams: List<Param>,
    private val optimistic: Boolean,
) : Command {

    override fun execute(connection: Connection) {
        val sets = values.joinToString { (k, v) -> "$k = ${v.stringValue}" }
        val query = "update $table set $sets where $condition"
        connection.prepareStatement(query).use { statement ->
            val params = values.map { (_, v) -> v } + conditionParams
            var index = 1
            params.forEach { param ->
                index = param.setValue(statement, index)
            }
            val count = statement.executeUpdate()
            if (optimistic && count != 1) {
                throw OptimisticLockException(table, condition)
            }
        }
    }

    class Builder(
        private val table: String,
        private val optimistic: Boolean,
    ) {

        private val values = mutableListOf<Pair<String, Param>>()

        fun set(column: String, value: Param) = apply {
            values += column to value
        }

        fun set(vararg values: Pair<String, Param>) = apply {
            this.values += values
        }

        fun where(condition: String, vararg params: Param): Update {
            return Update(table, values, condition, params.toList(), optimistic)
        }

        companion object {

            fun update(table: String) = Builder(table, false)

            fun optimisticUpdate(table: String) = Builder(table, true)
        }
    }
}