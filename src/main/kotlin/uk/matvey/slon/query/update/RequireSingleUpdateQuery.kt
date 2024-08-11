package uk.matvey.slon.query.update

import uk.matvey.slon.exception.OptimisticLockException
import java.sql.Connection

class RequireSingleUpdateQuery(
    private val update: Update,
) : Update {

    override fun execute(connection: Connection): Int {
        val count = update.execute(connection)
        if (count != 1) {
            throw OptimisticLockException()
        }
        return count
    }

    companion object {

        fun requireSingleUpdate(update: Update): RequireSingleUpdateQuery {
            return RequireSingleUpdateQuery(update)
        }
    }
}