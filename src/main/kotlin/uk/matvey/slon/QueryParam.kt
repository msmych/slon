package uk.matvey.slon

import org.postgresql.util.PGobject
import uk.matvey.slon.QueryParam.Type.INT
import uk.matvey.slon.QueryParam.Type.JSONB
import uk.matvey.slon.QueryParam.Type.RAW
import uk.matvey.slon.QueryParam.Type.TEXT
import uk.matvey.slon.QueryParam.Type.TEXT_ARRAY
import uk.matvey.slon.QueryParam.Type.TIMESTAMP
import uk.matvey.slon.QueryParam.Type.UUID
import java.sql.PreparedStatement
import java.sql.Timestamp
import java.sql.Types.NULL
import java.time.Instant

class QueryParam(
    private val type: Type,
    private val value: Any?,
) {

    enum class Type {
        RAW,
        TEXT,
        INT,
        UUID,
        TIMESTAMP,
        JSONB,
        TEXT_ARRAY,
    }

    fun stringValue() = when (this.type) {
        RAW -> value as String
        else -> "?"
    }

    fun setValue(statement: PreparedStatement, index: Int): Int {
        if (value == null) {
            statement.setNull(index, NULL)
        } else {
            when (type) {
                RAW -> {}
                TEXT -> statement.setString(index, value as String)
                INT -> statement.setLong(index, value as Long)
                UUID -> statement.setObject(index, value)
                TIMESTAMP -> statement.setTimestamp(index, value as Timestamp)
                JSONB -> {
                    val pgObj = PGobject()
                    pgObj.type = "jsonb"
                    pgObj.value = value as String
                    statement.setObject(index, pgObj)
                }
                TEXT_ARRAY -> {
                    val array = statement.connection.createArrayOf("text", (value as Collection<*>).toTypedArray())
                    statement.setArray(index, array)
                }
            }
        }
        return if (type == RAW) index else index + 1
    }

    companion object {

        fun raw(value: String) = QueryParam(RAW, value)

        fun text(value: String?) = QueryParam(TEXT, value)

        fun uuid(value: java.util.UUID?) = QueryParam(UUID, value)

        fun int(value: Int?) = int(value?.toLong())

        fun int(value: Long?) = QueryParam(INT, value)

        fun timestamp(value: Instant?) = QueryParam(TIMESTAMP, value?.let(Timestamp::from))

        fun jsonb(value: String?) = QueryParam(JSONB, value)

        fun textArray(value: Collection<String>?) = QueryParam(TEXT_ARRAY, value)
    }
}
