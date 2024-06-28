package uk.matvey.slon

import java.sql.ResultSet
import java.util.UUID

class RecordReader(private val resultSet: ResultSet) {

    fun nullableRaw(name: String) = resultSet.getObject(name)?.nullable()

    fun nullableRaw(index: Int) = resultSet.getObject(index)?.nullable()

    fun raw(name: String) = requireNotNull(nullableRaw(name))

    fun raw(index: Int) = requireNotNull(nullableRaw(index))

    fun nullableString(name: String) = resultSet.getString(name)?.nullable()

    fun nullableString(index: Int) = resultSet.getString(index)?.nullable()

    fun string(name: String) = requireNotNull(nullableString(name))

    fun string(index: Int) = requireNotNull(nullableString(index))

    fun nullableInt(name: String) = resultSet.getInt(name).nullable()

    fun nullableInt(index: Int) = resultSet.getInt(index).nullable()

    fun int(name: String) = requireNotNull(nullableInt(name))

    fun int(index: Int) = requireNotNull(nullableInt(index))

    fun nullableLong(name: String) = resultSet.getLong(name).nullable()

    fun nullableLong(index: Int) = resultSet.getLong(index).nullable()

    fun long(name: String) = requireNotNull(nullableLong(name))

    fun long(index: Int) = requireNotNull(nullableLong(index))

    fun nullableUuid(name: String) = nullableRaw(name) as UUID?

    fun nullableUuid(index: Int) = nullableRaw(index) as UUID?

    fun uuid(name: String) = requireNotNull(nullableUuid(name))

    fun uuid(index: Int) = requireNotNull(nullableUuid(index))

    fun nullableInstant(name: String) = resultSet.getTimestamp(name)?.nullable()?.toInstant()

    fun nullableInstant(index: Int) = resultSet.getTimestamp(index)?.nullable()?.toInstant()

    fun instant(name: String) = requireNotNull(nullableInstant(name))

    fun instant(index: Int) = requireNotNull(nullableInstant(index))

    fun nullableLocalDate(name: String) = resultSet.getDate(name)?.nullable()?.toLocalDate()

    fun nullableLocalDate(index: Int) = resultSet.getDate(index)?.nullable()?.toLocalDate()

    fun localDate(name: String) = requireNotNull(nullableLocalDate(name))

    fun localDate(index: Int) = requireNotNull(nullableLocalDate(index))

    @Suppress("UNCHECKED_CAST")
    fun nullableStringList(name: String) = (resultSet.getArray(name)?.nullable()?.array as Array<String>?)?.toList()

    @Suppress("UNCHECKED_CAST")
    fun nullableStringList(index: Int) = (resultSet.getArray(index)?.nullable()?.array as Array<String>?)?.toList()

    fun stringList(name: String) = requireNotNull(nullableStringList(name))

    fun stringList(index: Int) = requireNotNull(nullableStringList(index))

    private fun <T> T?.nullable() = this?.takeIf { !resultSet.wasNull() }
}
