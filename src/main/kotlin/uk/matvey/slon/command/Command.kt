package uk.matvey.slon.command

import java.sql.Connection

sealed interface Command {

    fun execute(connection: Connection)
}
