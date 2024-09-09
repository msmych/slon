package uk.matvey.slon.query

import uk.matvey.slon.value.PgValue

class DeleteQuery(
    private val table: String,
    private val condition: String,
    private val conditionParams: List<PgValue>,
) : Update {

    override fun sql(): String {
        return "delete from $table where $condition"
       }

    override fun params(): List<PgValue> {
        return conditionParams
    }
}