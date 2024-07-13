package uk.matvey.slon

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.testcontainers.containers.PostgreSQLContainer
import uk.matvey.slon.DataSourceKit.hikariDataSource
import javax.sql.DataSource

open class TestContainersSetup {

    companion object {

        private val postgres = PostgreSQLContainer("postgres")
        private lateinit var dataSource: DataSource

        fun dataSource(): DataSource = dataSource

        @BeforeAll
        @JvmStatic
        fun globalSetup() {
            postgres.start()
            dataSource = hikariDataSource(postgres.jdbcUrl, postgres.username, postgres.password)
        }

        @AfterAll
        @JvmStatic
        fun globalTeardown() {
            postgres.stop()
        }
    }
}