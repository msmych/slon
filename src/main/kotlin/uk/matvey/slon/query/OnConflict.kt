package uk.matvey.slon.query

class OnConflict(
    private val columns: List<String>,
    private val action: String,
) {

    fun sql(): String {
        val conflictColumns = columns.takeIf { it.isNotEmpty() }
            ?.joinToString(prefix = "(", postfix = ")")
        return listOfNotNull(
            "on conflict",
            conflictColumns,
            "do $action",
        ).joinToString(" ")
    }

    companion object {

        fun doNothing(): OnConflict {
            return OnConflict(listOf(), "nothing")
        }
    }
}