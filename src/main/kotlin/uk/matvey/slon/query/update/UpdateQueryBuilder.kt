package uk.matvey.slon.query.update

import uk.matvey.slon.value.PgValue

class UpdateQueryBuilder(
    private val table: String,
) {

    private val values = mutableListOf<Pair<String, PgValue>>()
    private lateinit var condition: String
    private val conditionParams = mutableListOf<PgValue>()

    fun set(values: List<Pair<String, PgValue>>) = apply {
        this.values += values
    }

    fun set(vararg values: Pair<String, PgValue>) = apply {
        this.set(values.toList())
    }

    fun set(column: String, value: PgValue) = apply {
        this.set(column to value)
    }

    fun where(condition: String, params: List<PgValue>): UpdateQuery {
        this.condition = condition
        this.conditionParams += params
        return UpdateQuery(table, values, condition, conditionParams)
    }

    fun where(condition: String, vararg params: PgValue): UpdateQuery {
        return this.where(condition, params.toList())
    }

    companion object {

        fun update(table: String): UpdateQueryBuilder {
            return UpdateQueryBuilder(table)
        }
    }
}