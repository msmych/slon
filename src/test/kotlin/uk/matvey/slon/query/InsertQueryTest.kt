package uk.matvey.slon.query

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import uk.matvey.slon.value.PgText.Companion.toPgText

class InsertQueryTest {

    @Test
    fun `should validate on creation`() {
        // when / then
        assertThatThrownBy {
            InsertQuery(
                table = "table",
                columns = listOf("col1", "col2"),
                values = listOf(listOf("value1".toPgText())),
            )
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Values count must be equal to columns count")

        assertThatThrownBy {
            InsertQuery(
                table = "table",
                columns = listOf(),
                values = listOf(),
            )
        }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Columns must be set")
    }
}