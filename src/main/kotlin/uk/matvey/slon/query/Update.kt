package uk.matvey.slon.query

import uk.matvey.slon.value.PgValue

interface Update {

    fun sql(): String

    fun params(): List<PgValue>

    companion object {

        fun plainUpdate(sql: String, params: List<PgValue> = listOf()) = object : Update {
            override fun sql() = sql
            override fun params() = params
        }
    }
}