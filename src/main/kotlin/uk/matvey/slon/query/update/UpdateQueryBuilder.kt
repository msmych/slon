package uk.matvey.slon.query.update

import uk.matvey.slon.param.Param

class UpdateQueryBuilder(
    private val table: String,
) {

    private val values = mutableListOf<Pair<String, Param>>()
    private lateinit var condition: String
    private val conditionParams = mutableListOf<Param>()

    fun set(values: List<Pair<String, Param>>) = apply {
        this.values += values
    }

    fun set(vararg values: Pair<String, Param>) = apply {
        this.set(values.toList())
    }

    fun set(column: String, value: Param) = apply {
        this.set(column to value)
    }

    fun where(condition: String, params: List<Param>): UpdateQuery {
        this.condition = condition
        this.conditionParams += params
        return UpdateQuery(table, values, condition, conditionParams)
    }

    fun where(condition: String, vararg params: Param): UpdateQuery {
        return this.where(condition, params.toList())
    }

    companion object {

        fun update(table: String): UpdateQueryBuilder {
            return UpdateQueryBuilder(table)
        }
    }
}