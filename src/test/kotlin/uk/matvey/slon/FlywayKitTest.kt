package uk.matvey.slon

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.matvey.slon.flyway.FlywayKit.flywayMigrate
import uk.matvey.slon.repo.Repo
import uk.matvey.slon.repo.RepoKit.queryAll

class FlywayKitTest : TestContainersSetup() {

    @Test
    fun `should migrate in public schema`() {
        // given
        val repo = Repo(dataSource())

        // when
        flywayMigrate(dataSource = dataSource())

        // then
        repo.queryAll("select count(*) from migration_test") {
            assertThat(it.int(1)).isEqualTo(1)
        }
    }
}