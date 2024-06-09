package uk.matvey.slon.command

import uk.matvey.slon.QueryParam
import uk.matvey.slon.exception.OptimisticLockException

class Update(
    private val table: String,
    private val values: List<Pair<String, QueryParam>>,
    private var condition: String,
    private var conditionParams: List<QueryParam>,
    private val optimistic: Boolean,
) : Command {

    override fun generateQuery(): String {
        val sets = values.joinToString { (k, v) -> "$k = ${v.stringValue()}" }
        return "UPDATE $table SET $sets WHERE $condition"
    }

    override fun collectParams(): List<QueryParam> {
        return values.map { (_, v) -> v } + conditionParams
    }

    override fun onResult(count: Int) {
        if (optimistic && count != 1) {
            throw OptimisticLockException(table, condition)
        }
    }

    class Builder(
        private val table: String,
        private val optimistic: Boolean,
    ) {

        private val values = mutableListOf<Pair<String, QueryParam>>()

        fun set(column: String, value: QueryParam) = apply {
            values += column to value
        }

        fun set(vararg values: Pair<String, QueryParam>) = apply {
            this.values += values
        }

        fun where(condition: String, vararg params: QueryParam): Update {
            return Update(table, values, condition, params.toList(), optimistic)
        }

        companion object {

            fun update(table: String) = Builder(table, false)

            fun optimisticUpdate(table: String) = Builder(table, true)
        }
    }
}