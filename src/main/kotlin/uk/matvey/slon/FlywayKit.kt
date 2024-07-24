package uk.matvey.slon

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration
import javax.sql.DataSource

object FlywayKit {

    fun flywayConfig(
        dataSource: DataSource,
        schema: String,
        location: String,
        createSchema: Boolean = true,
        cleanDisabled: Boolean = true,
    ): FluentConfiguration {
        return Flyway.configure()
            .dataSource(dataSource)
            .schemas(schema)
            .defaultSchema(schema)
            .createSchemas(createSchema)
            .locations(location)
            .cleanDisabled(cleanDisabled)
    }
}