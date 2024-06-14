package uk.matvey.slon.query.update

import uk.matvey.slon.param.Param
import uk.matvey.slon.query.Query
import java.sql.Connection

class UpdateQuery(
    private val table: String,
    private val values: List<Pair<String, Param>>,
    private var condition: String,
    private var conditionParams: List<Param>,
) : Query<Int> {

    override fun execute(connection: Connection): Int {
        val sets = values.joinToString { (k, v) -> "$k = ${v.stringValue}" }
        val query = "update $table set $sets where $condition"
        val params = values.map { it.second } + conditionParams
        return RawUpdateQuery(query, params).execute(connection)
    }

    class Builder(
        private val table: String,
    ) {

        private val values = mutableListOf<Pair<String, Param>>()

        fun set(values: List<Pair<String, Param>>) = apply {
            this.values += values
        }

        fun set(vararg values: Pair<String, Param>) = set(values.toList())

        fun set(column: String, value: Param) = set(column to value)

        fun where(condition: String, params: List<Param>): UpdateQuery {
            return UpdateQuery(table, values, condition, params)
        }

        fun where(condition: String, vararg params: Param) = where(condition, params.toList())

        companion object {

            fun update(table: String) = Builder(table)
        }
    }
}