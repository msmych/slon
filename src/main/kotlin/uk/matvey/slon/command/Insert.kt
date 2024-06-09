package uk.matvey.slon.command

import uk.matvey.slon.QueryParam

class Insert(
    private val table: String,
    private val columns: List<String>,
    private val values: List<List<QueryParam>>,
) : Command {

    override fun generateQuery(): String {
        val columns = columns.joinToString(prefix = "(", postfix = ")")
        val values = values.joinToString { vs ->
            vs.joinToString(prefix = "(", postfix = ")", transform = QueryParam::stringValue)
        }
        return "INSERT INTO $table $columns VALUES $values"
    }

    override fun collectParams(): List<QueryParam> {
        return values.flatten()
    }

    class Builder(
        private val table: String,
    ) {

        private lateinit var columns: List<String>
        private val values = mutableListOf<List<QueryParam>>()

        fun columns(vararg columns: String) = apply {
            this.columns = columns.toList()
        }

        fun values(vararg values: QueryParam) = apply {
            this.values += values.toList()
        }

        fun set(vararg values: Pair<String, QueryParam>) = apply {
            this.columns = values.map { (k, _) -> k }.toList()
            this.values += values.map { (_, v) -> v }.toList()
        }

        fun build() = Insert(table, columns, values)

        companion object {

            fun insert(table: String) = Builder(table)
        }
    }
}