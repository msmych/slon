package uk.matvey.slon.query

class OnConflictClause(
    private val columns: List<String>,
    private val action: String,
) {

    fun toSql(): String {
        val conflictColumns = columns.takeIf { it.isNotEmpty() }
            ?.joinToString(prefix = "(", postfix = ")")
        return listOfNotNull(
            "on conflict",
            conflictColumns,
            "do $action",
        ).joinToString(" ")
    }

    companion object {

        fun doNothing(): OnConflictClause {
            return OnConflictClause(listOf(), "nothing")
        }
    }
}