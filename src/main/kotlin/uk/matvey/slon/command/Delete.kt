package uk.matvey.slon.command

import uk.matvey.slon.QueryParam

class Delete(
    private val table: String,
    private val condition: String,
    private val conditionParams: List<QueryParam>
) : Command {

    override fun generateQuery(): String {
        return "DELETE FROM $table WHERE $condition"
    }

    override fun collectParams(): List<QueryParam> {
        return conditionParams
    }

    class Builder(private val table: String) {

        fun where(condition: String, vararg params: QueryParam): Delete {
            return Delete(table, condition, params.toList())
        }

        companion object {

            fun delete(table: String) = Builder(table)
        }
    }
}