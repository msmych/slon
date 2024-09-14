package uk.matvey.slon

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.matvey.slon.access.AccessKit.queryAll
import uk.matvey.slon.flyway.FlywayKit.flywayMigrate
import uk.matvey.slon.repo.Repo

class FlywayKitTest : TestContainersSetup() {

    @Test
    fun `should migrate in public schema`() = runTest {
        // given
        val repo = Repo(dataSource())

        // when
        flywayMigrate(dataSource = dataSource())

        // then
        repo.access { a ->
            a.queryAll("select count(*) from migration_test") {
                assertThat(it.int(1)).isEqualTo(1)
            }
        }
    }
}