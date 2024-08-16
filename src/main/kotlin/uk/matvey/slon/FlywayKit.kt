package uk.matvey.slon

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration
import javax.sql.DataSource

object FlywayKit {

    fun flywayMigrate(
        dataSource: DataSource,
        schema: String? = null,
        location: String? = null,
        clean: Boolean = false,
        config: FluentConfiguration.() -> Unit = {},
    ) {
        flywayConfig(
            dataSource = dataSource,
            schema = schema,
            location = location,
        ) {
            this.cleanDisabled(!clean)
            config()
        }
            .load()
            .apply {
                if (clean) {
                    this.clean()
                }
            }
            .migrate()
    }

    fun flywayConfig(
        dataSource: DataSource,
        schema: String? = null,
        location: String? = null,
        config: FluentConfiguration.() -> Unit = {},
    ): FluentConfiguration {
        return Flyway.configure()
            .dataSource(dataSource)
            .apply {
                schema?.let {
                    schemas(it)
                    defaultSchema(it)
                }
                location?.let {
                    locations(it)
                }
                config()
            }
    }
}