package uk.matvey.slon

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.matvey.slon.flyway.FlywayKit.flywayMigrate
import uk.matvey.slon.repo.Repo
import uk.matvey.slon.repo.RepoKit.queryOne

class FlywayKitTest : TestContainersSetup() {

    @Test
    fun `should migrate in public schema`() = runTest {
        // given
        val repo = Repo(dataSource())

        // when
        flywayMigrate(dataSource = dataSource())

        // then
        val count = repo.queryOne("select count(*) from migration_test") { it.int(1) }
        assertThat(count).isEqualTo(1)
    }
}