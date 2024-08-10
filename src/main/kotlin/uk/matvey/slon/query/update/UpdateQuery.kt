package uk.matvey.slon.query.update

import uk.matvey.slon.param.Param
import uk.matvey.slon.query.update.OptimisticUpdateQuery.Companion.optimistic
import java.sql.Connection

class UpdateQuery(
    private val table: String,
    private val values: List<Pair<String, Param>>,
    private var condition: String,
    private var conditionParams: List<Param>,
) : Update {

    override fun execute(connection: Connection): Int {
        val sets = values.joinToString { (k, v) -> "$k = ${v.stringValue}" }
        val query = "update $table set $sets where $condition"
        val params = values.map { it.second } + conditionParams
        return RawUpdateQuery(query, params).execute(connection)
    }

    fun optimistic() = optimistic(this)
}