package uk.matvey.slon.query.update

import uk.matvey.slon.value.PgValue
import java.sql.Connection

class RawUpdateQuery(
    private val query: String,
    private val params: List<PgValue>
) : Update {

    override fun execute(connection: Connection): Int {
        return connection.prepareStatement(query).use { statement ->
            var index = 1
            this.params.forEach { param ->
                index = param.setValue(statement, index)
            }
            statement.executeUpdate()
        }
    }

    companion object {

        fun rawUpdate(query: String, params: List<PgValue>) = RawUpdateQuery(query, params)

        fun rawUpdate(query: String, vararg params: PgValue) = rawUpdate(query, params.toList())
    }
}