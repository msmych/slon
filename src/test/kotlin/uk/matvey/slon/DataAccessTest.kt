package uk.matvey.slon

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DataAccessTest : TestContainersSetup() {

    @Test
    fun `should execute updates and queries`() {
        // given
        val da = DataAccess(dataSource())

        // when / then
        da.execute("CREATE TABLE data_access_test (id INT)")
        da.execute("INSERT INTO data_access_test VALUES (1), (2)")
        da.query("SELECT * FROM data_access_test ORDER BY id DESC") { rs ->
            assertThat(rs.next()).isTrue()
            assertThat(rs.getInt("id")).isEqualTo(2)
            assertThat(rs.next()).isTrue()
            assertThat(rs.getInt("id")).isEqualTo(1)
            assertThat(rs.next()).isFalse()
        }
    }
}