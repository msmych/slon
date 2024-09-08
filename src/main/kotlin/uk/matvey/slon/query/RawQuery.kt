package uk.matvey.slon.query

import uk.matvey.slon.RecordReader
import uk.matvey.slon.value.PgValue
import java.sql.Connection

class RawQuery<T>(
    private val query: String,
    private val params: List<PgValue>,
    private val read: (RecordReader) -> T,
) : Query<List<T>> {

    override fun execute(connection: Connection): List<T> {
        return connection.prepareStatement(query).use { statement ->
            var index = 1
            params.forEach { param ->
                index = param.setValue(statement, index)
            }
            statement.executeQuery().use { resultSet ->
                val list = mutableListOf<T>()
                while (resultSet.next()) {
                    list += read(RecordReader(resultSet))
                }
                list
            }
        }
    }

    fun one(): OneQuery<T> {
        return OneQuery(this)
    }

    fun oneOrNull(): OneOrNullQuery<T> {
        return OneOrNullQuery(this)
    }

    companion object {

        fun <T> rawQuery(query: String, params: List<PgValue>, read: (RecordReader) -> T): RawQuery<T> {
            return RawQuery(query, params, read)
        }
    }
}