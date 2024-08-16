package uk.matvey.slon

import org.testcontainers.containers.PostgreSQLContainer
import uk.matvey.slon.HikariKit.hikariDataSource

class PostgresTestContainer {

    val postgres = PostgreSQLContainer("postgres")

    fun start() {
        postgres.start()
    }

    fun dataSource() = hikariDataSource(postgres.jdbcUrl, postgres.username, postgres.password)

    fun stop() {
        postgres.stop()
    }
}