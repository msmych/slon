package uk.matvey.slon.query

class Returning(
    private val columns: List<String>,
) {

    fun sql(): String {
        return listOf(
            "returning",
            columns.takeIf { it.isNotEmpty() }?.joinToString(", ") ?: "*"
        ).joinToString(" ")
    }

    companion object {

        fun all() = Returning(listOf())
    }
}