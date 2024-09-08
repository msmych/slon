package uk.matvey.slon

import java.sql.ResultSet
import java.util.UUID

class RecordReader(private val resultSet: ResultSet) {

    fun rawOrNull(name: String) = resultSet.getObject(name)?.orNull()

    fun rawOrNull(index: Int) = resultSet.getObject(index)?.orNull()

    fun raw(name: String) = requireNotNull(rawOrNull(name))

    fun raw(index: Int) = requireNotNull(rawOrNull(index))

    fun stringOrNull(name: String) = resultSet.getString(name)?.orNull()

    fun stringOrNull(index: Int) = resultSet.getString(index)?.orNull()

    fun string(name: String) = requireNotNull(stringOrNull(name))

    fun string(index: Int) = requireNotNull(stringOrNull(index))

    inline fun <reified T : Enum<T>> enumOrNull(name: String) = stringOrNull(name)?.let { enumValueOf<T>(it) }

    inline fun <reified T : Enum<T>> enumOrNull(index: Int) = stringOrNull(index)?.let { enumValueOf<T>(it) }

    inline fun <reified T : Enum<T>> enum(name: String) = enumValueOf<T>(string(name))

    inline fun <reified T : Enum<T>> enum(index: Int) = enumValueOf<T>(string(index))

    fun intOrNull(name: String) = resultSet.getInt(name).orNull()

    fun intOrNull(index: Int) = resultSet.getInt(index).orNull()

    fun int(name: String) = requireNotNull(intOrNull(name))

    fun int(index: Int) = requireNotNull(intOrNull(index))

    fun longOrNull(name: String) = resultSet.getLong(name).orNull()

    fun longOrNull(index: Int) = resultSet.getLong(index).orNull()

    fun long(name: String) = requireNotNull(longOrNull(name))

    fun long(index: Int) = requireNotNull(longOrNull(index))

    fun uuidOrNull(name: String) = rawOrNull(name) as UUID?

    fun uuidOrNull(index: Int) = rawOrNull(index) as UUID?

    fun uuid(name: String) = requireNotNull(uuidOrNull(name))

    fun uuid(index: Int) = requireNotNull(uuidOrNull(index))

    fun instantOrNull(name: String) = resultSet.getTimestamp(name)?.orNull()?.toInstant()

    fun instantOrNull(index: Int) = resultSet.getTimestamp(index)?.orNull()?.toInstant()

    fun instant(name: String) = requireNotNull(instantOrNull(name))

    fun instant(index: Int) = requireNotNull(instantOrNull(index))

    fun localDateTimeOrNull(name: String) = resultSet.getTimestamp(name)?.orNull()?.toLocalDateTime()

    fun localDateTimeOrNull(index: Int) = resultSet.getTimestamp(index)?.orNull()?.toLocalDateTime()

    fun localDateTime(name: String) = requireNotNull(localDateTimeOrNull(name))

    fun localDateTime(index: Int) = requireNotNull(localDateOrNull(index))

    fun localDateOrNull(name: String) = resultSet.getDate(name)?.orNull()?.toLocalDate()

    fun localDateOrNull(index: Int) = resultSet.getDate(index)?.orNull()?.toLocalDate()

    fun localDate(name: String) = requireNotNull(localDateOrNull(name))

    fun localDate(index: Int) = requireNotNull(localDateOrNull(index))

    @Suppress("UNCHECKED_CAST")
    fun stringListOrNull(name: String) = (resultSet.getArray(name)?.orNull()?.array as Array<String>?)?.toList()

    @Suppress("UNCHECKED_CAST")
    fun stringListOrNull(index: Int) = (resultSet.getArray(index)?.orNull()?.array as Array<String>?)?.toList()

    fun stringList(name: String) = requireNotNull(stringListOrNull(name))

    fun stringList(index: Int) = requireNotNull(stringListOrNull(index))

    private fun <T> T?.orNull() = this?.takeIf { !resultSet.wasNull() }
}
