package uk.matvey.slon.value

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonElement
import org.postgresql.util.PGobject
import uk.matvey.kit.json.JsonKit.JSON
import java.sql.PreparedStatement
import java.sql.Types

class PgJsonb(private val value: String?) : PgValue() {

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

        fun JsonElement?.toPgJsonb() = PgJsonb(this?.let(JSON::encodeToString))

        fun String?.toPgJsonb() = this?.let(JSON::parseToJsonElement).toPgJsonb()
    }
}