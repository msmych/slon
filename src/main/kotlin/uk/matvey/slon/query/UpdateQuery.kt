package uk.matvey.slon.query

import uk.matvey.slon.value.PgValue

class UpdateQuery(
    private val table: String,
    private val values: List<Pair<String, PgValue>>,
    private val condition: String,
    private val conditionParams: List<PgValue>,
) : Update {

    override fun sql(): String {
        val sets = values.joinToString { (k, v) -> "$k = ${v.placeholder}" }
        return "update $table set $sets where $condition"
    }

    override fun params(): List<PgValue> {
        return values.map { it.second } + conditionParams
    }
}