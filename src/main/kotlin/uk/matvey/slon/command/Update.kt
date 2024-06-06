package uk.matvey.slon.command

import uk.matvey.slon.QueryParam

class Update(
    private val table: String,
    private val values: List<Pair<String, QueryParam>>,
    private var condition: String,
    private var conditionParams: List<QueryParam>
) : Command {

    override fun generateQuery(): String {
        val sets = values.joinToString { "${it.first} = ${it.second.stringValue()}" }
        return "UPDATE $table SET $sets WHERE $condition"
    }

    override fun collectParams(): List<QueryParam> {
        return values.map { it.second } + conditionParams
    }

    class Builder(
        private val table: String,
    ) {
        private val values = mutableListOf<Pair<String, QueryParam>>()

        fun set(column: String, value: QueryParam) = apply {
            values += column to value
        }

        fun set(vararg values: Pair<String, QueryParam>) = apply {
            this.values += values
        }

        fun where(condition: String, vararg params: QueryParam) = Update(table, values, condition, params.toList())

        companion object {

            fun update(table: String) = Builder(table)
        }
    }
}