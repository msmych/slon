package uk.matvey.slon.query.update

import uk.matvey.slon.exception.OptimisticLockException
import uk.matvey.slon.query.Query
import java.sql.Connection

class OptimisticUpdateQuery(
    private val update: Query<Int>,
) : Query<Int> {

    override fun execute(connection: Connection): Int {
        val count = update.execute(connection)
        if (count != 1) {
            throw OptimisticLockException()
        }
        return count
    }

    companion object {

        fun optimistic(update: Query<Int>): OptimisticUpdateQuery {
            return OptimisticUpdateQuery(update)
        }
    }
}