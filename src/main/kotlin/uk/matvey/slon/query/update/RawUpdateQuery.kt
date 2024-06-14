package uk.matvey.slon.query.update

import uk.matvey.slon.param.Param
import uk.matvey.slon.query.Query
import java.sql.Connection

class RawUpdateQuery(
    private val query: String,
    private val params: List<Param>
) : Query<Int> {

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

        fun rawUpdate(query: String, params: List<Param>) = RawUpdateQuery(query, params)

        fun rawUpdate(query: String, vararg params: Param) = rawUpdate(query, params.toList())
    }
}