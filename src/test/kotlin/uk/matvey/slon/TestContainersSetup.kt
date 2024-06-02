package uk.matvey.slon

import com.zaxxer.hikari.HikariDataSource
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.testcontainers.containers.PostgreSQLContainer
import javax.sql.DataSource

open class TestContainersSetup {

    fun dataSource(): DataSource = dataSource

    companion object {

        private val postgres = PostgreSQLContainer("postgres")
        private lateinit var dataSource: DataSource

        @BeforeAll
        @JvmStatic
        fun globalSetup() {
            postgres.start()
            dataSource = HikariDataSource().apply {
                jdbcUrl = postgres.jdbcUrl
                username = postgres.username
                password = postgres.password
            }
        }

        @AfterAll
        @JvmStatic
        fun globalTeardown() {
            postgres.stop()
        }
    }
}