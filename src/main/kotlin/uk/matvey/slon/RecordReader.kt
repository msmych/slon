package uk.matvey.slon

import java.sql.ResultSet
import java.util.UUID

class RecordReader(private val resultSet: ResultSet) {

    fun nullableRaw(name: String) = resultSet.getObject(name)?.nullable()

    fun raw(name: String) = requireNotNull(nullableRaw(name))

    fun nullableString(name: String) = resultSet.getString(name)?.nullable()

    fun string(name: String) = requireNotNull(nullableString(name))

    fun nullableInt(name: String) = resultSet.getInt(name).nullable()

    fun int(name: String) = requireNotNull(nullableInt(name))

    fun nullableLong(name: String) = resultSet.getLong(name).nullable()

    fun long(name: String) = requireNotNull(nullableLong(name))

    fun nullableUuid(name: String) = nullableRaw(name) as UUID?

    fun uuid(name: String) = requireNotNull(nullableUuid(name))

    fun nullableInstant(name: String) = resultSet.getTimestamp(name)?.nullable()?.toInstant()

    fun instant(name: String) = requireNotNull(nullableInstant(name))

    @Suppress("UNCHECKED_CAST")
    fun nullableStringList(name: String) = (resultSet.getArray(name)?.nullable()?.array as Array<String>?)?.toList()

    fun stringList(name: String) = requireNotNull(nullableStringList(name))

    private fun <T> T?.nullable() = this?.takeIf { !resultSet.wasNull() }
}
