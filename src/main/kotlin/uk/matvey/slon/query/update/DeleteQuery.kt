package uk.matvey.slon.query.update

import uk.matvey.slon.param.Param
import uk.matvey.slon.query.Query
import java.sql.Connection

class DeleteQuery(
    private val table: String,
    private val condition: String,
    private val conditionParams: List<Param>,
) : Query<Int> {

    override fun execute(connection: Connection): Int {
        val query = "delete from $table where $condition"
        return RawUpdateQuery(query, conditionParams).execute(connection)
    }

    class Builder(private val table: String) {

        fun where(condition: String, params: List<Param>): DeleteQuery {
            return DeleteQuery(table, condition, params.toList())
        }

        fun where(condition: String, vararg params: Param) = where(condition, params.toList())

        companion object {

            fun deleteFrom(table: String) = Builder(table)
        }
    }
}