package uk.matvey.slon.query.update

import uk.matvey.slon.value.PgValue

class DeleteQueryBuilder(
    private val table: String,
) {

    private lateinit var condition: String
    private val conditionParams = mutableListOf<PgValue>()

    fun where(condition: String, params: List<PgValue>): DeleteQuery {
        this.condition = condition
        this.conditionParams += params
        return DeleteQuery(table, condition, conditionParams)
    }

    fun where(condition: String, vararg params: PgValue): DeleteQuery {
        return this.where(condition, params.toList())
    }

    companion object {

        fun deleteFrom(table: String): DeleteQueryBuilder {
            return DeleteQueryBuilder(table)
        }
    }
}