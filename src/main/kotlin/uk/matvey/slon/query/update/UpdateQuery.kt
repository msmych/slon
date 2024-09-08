package uk.matvey.slon.query.update

import uk.matvey.slon.value.PgValue
import uk.matvey.slon.query.update.RequireSingleUpdateQuery.Companion.requireSingleUpdate
import java.sql.Connection

class UpdateQuery(
    private val table: String,
    private val values: List<Pair<String, PgValue>>,
    private var condition: String,
    private var conditionParams: List<PgValue>,
) : Update {

    override fun execute(connection: Connection): Int {
        val sets = values.joinToString { (k, v) -> "$k = ${v.placeholder}" }
        val query = "update $table set $sets where $condition"
        val params = values.map { it.second } + conditionParams
        return RawUpdateQuery(query, params).execute(connection)
    }

    fun requireSingleUpdate() = requireSingleUpdate(this)
}