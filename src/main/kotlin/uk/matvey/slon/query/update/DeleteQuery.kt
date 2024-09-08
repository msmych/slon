package uk.matvey.slon.query.update

import uk.matvey.slon.value.PgValue
import uk.matvey.slon.query.update.RequireSingleUpdateQuery.Companion.requireSingleUpdate
import java.sql.Connection

class DeleteQuery(
    private val table: String,
    private val condition: String,
    private val conditionParams: List<PgValue>,
) : Update {

    override fun execute(connection: Connection): Int {
        val query = "delete from $table where $condition"
        return RawUpdateQuery(query, conditionParams).execute(connection)
    }

    fun requireSingleUpdate() = requireSingleUpdate(this)
}