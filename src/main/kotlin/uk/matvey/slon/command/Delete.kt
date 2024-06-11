package uk.matvey.slon.command

import uk.matvey.slon.param.Param
import uk.matvey.slon.exception.OptimisticLockException
import java.sql.Connection

class Delete(
    private val table: String,
    private val condition: String,
    private val conditionParams: List<Param>,
    private val optimistic: Boolean,
) : Command {

    override fun execute(connection: Connection) {
        val query = "delete from $table where $condition"
        connection.prepareStatement(query).use { statement ->
            var index = 1
            conditionParams.forEach { param ->
                index = param.setValue(statement, index)
            }
            val count = statement.executeUpdate()
            if (optimistic && count == 0) {
                throw OptimisticLockException(table, condition)
            }
        }
    }

    class Builder(private val table: String, private val optimistic: Boolean) {

        fun where(condition: String, vararg params: Param): Delete {
            return Delete(table, condition, params.toList(), optimistic)
        }

        companion object {

            fun delete(from: String) = Builder(from, false)

            fun optimisticDelete(from: String) = Builder(from, true)
        }
    }
}