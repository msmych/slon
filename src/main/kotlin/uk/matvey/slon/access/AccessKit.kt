package uk.matvey.slon.access

import uk.matvey.slon.exception.UpdateCountMismatchException
import uk.matvey.slon.query.Update

object AccessKit {

    fun Access.updateSingle(update: Update) {
        val updates = execute(update)
        if (updates != 1) {
            throw UpdateCountMismatchException(1, updates)
        }
    }
}