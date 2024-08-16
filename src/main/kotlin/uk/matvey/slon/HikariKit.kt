package uk.matvey.slon

import com.zaxxer.hikari.HikariDataSource

object HikariKit {

    fun hikariDataSource(
        jdbcUrl: String,
        username: String,
        password: String,
        config: HikariDataSource.() -> Unit = {},
    ) = HikariDataSource().apply {
        this.jdbcUrl = jdbcUrl
        this.username = username
        this.password = password
        this.driverClassName = "org.postgresql.Driver"
        config()
    }
}