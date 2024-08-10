package uk.matvey.slon.param

import kotlinx.serialization.json.JsonElement
import org.postgresql.util.PGobject
import uk.matvey.kit.json.JsonKit.jsonSerialize
import java.sql.PreparedStatement
import java.sql.Types

class JsonbParam(private val value: String?) : Param() {

    override fun setValue(statement: PreparedStatement, index: Int): Int {
        if (value == null) {
            statement.setNull(index, Types.NULL)
        } else {
            val pgObj = PGobject()
            pgObj.type = "jsonb"
            pgObj.value = value
            statement.setObject(index, pgObj)
        }
        return index + 1
    }

    companion object {

        fun jsonb(value: String?) = JsonbParam(value)

        fun jsonb(value: JsonElement?) = JsonbParam(value?.let { jsonSerialize(it) })
    }
}