package uk.matvey.slon.query

import java.sql.Connection

interface Query<T> {

    fun execute(connection: Connection): T
}