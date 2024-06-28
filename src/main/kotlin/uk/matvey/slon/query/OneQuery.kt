package uk.matvey.slon.query

import java.sql.Connection

class OneQuery<T>(
    private val query: Query<List<T>>,
) : Query<T> {

    override fun execute(connection: Connection): T {
        return query.execute(connection).single()
    }
}