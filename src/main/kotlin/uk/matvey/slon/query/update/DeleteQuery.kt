package uk.matvey.slon.query.update

import uk.matvey.slon.param.Param
import uk.matvey.slon.query.update.OptimisticUpdateQuery.Companion.optimistic
import java.sql.Connection

class DeleteQuery(
    private val table: String,
    private val condition: String,
    private val conditionParams: List<Param>,
) : Update {

    override fun execute(connection: Connection): Int {
        val query = "delete from $table where $condition"
        return RawUpdateQuery(query, conditionParams).execute(connection)
    }

    fun optimistic() = optimistic(this)
}