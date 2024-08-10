package uk.matvey.slon.query.update

import uk.matvey.slon.param.Param

class DeleteQueryBuilder(
    private val table: String,
) {

    private lateinit var condition: String
    private val conditionParams = mutableListOf<Param>()

    fun where(condition: String, params: List<Param>): DeleteQuery {
        this.condition = condition
        this.conditionParams += params
        return DeleteQuery(table, condition, conditionParams)
    }

    fun where(condition: String, vararg params: Param): DeleteQuery {
        return this.where(condition, params.toList())
    }

    companion object {

        fun deleteFrom(table: String): DeleteQueryBuilder {
            return DeleteQueryBuilder(table)
        }
    }
}