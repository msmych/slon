package uk.matvey.slon.query

import uk.matvey.slon.value.PgValue

class InsertQuery(
    private val table: String,
    private val columns: List<String>,
    private val values: List<List<PgValue>>,
    private val onConflict: OnConflict? = null,
) : Update {

    init {
        require(values.all { it.size == columns.size }) {
            "Values count must be equal to columns count"
        }
        require(columns.isNotEmpty()) {
            "Columns must be set"
        }
        require(values.isNotEmpty()) {
            "Values must be set"
        }
    }

    override fun sql(): String {
        val columns = columns.joinToString(prefix = "(", postfix = ")")
        val values = values.joinToString(", ") { vals ->
            vals.joinToString(prefix = "(", postfix = ")", transform = { it.placeholder })
        }
        return listOfNotNull(
            "insert into $table $columns values $values",
            onConflict?.sql(),
        ).joinToString(" ")
    }

    override fun params(): List<PgValue> {
        return this.values.flatten()
    }
}