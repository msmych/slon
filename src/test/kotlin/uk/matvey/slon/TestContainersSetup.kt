package uk.matvey.slon

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll

open class TestContainersSetup {

    companion object {

        private val postgres = PostgresTestContainer()

        fun dataSource() = postgres.dataSource()

        @BeforeAll
        @JvmStatic
        fun globalSetup() {
            postgres.start()
        }

        @AfterAll
        @JvmStatic
        fun globalTeardown() {
            postgres.stop()
        }
    }
}