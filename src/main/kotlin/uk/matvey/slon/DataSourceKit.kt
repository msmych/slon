package uk.matvey.slon

import com.zaxxer.hikari.HikariDataSource

object DataSourceKit {

    fun hikariDataSource(
        jdbcUrl: String,
        username: String,
        password: String,
    ): HikariDataSource {
        return HikariDataSource().apply {
            this.jdbcUrl = jdbcUrl
            this.username = username
            this.password = password
            this.driverClassName = "org.postgresql.Driver"
        }
    }
}