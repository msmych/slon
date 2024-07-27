package uk.matvey.slon

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.configuration.FluentConfiguration
import javax.sql.DataSource

object FlywayKit {

    fun flywayMigrate(
        dataSource: DataSource,
        schema: String,
        createSchema: Boolean = true,
        location: String? = null,
        clean: Boolean = false,
    ) {
        flywayConfig(
            dataSource = dataSource,
            schema = schema,
            createSchema = createSchema,
            location = location,
            cleanDisabled = !clean,
        )
            .load()
            .apply {
                if (clean) {
                    this.clean()
                }
            }.migrate()
    }

    fun flywayConfig(
        dataSource: DataSource,
        schema: String,
        createSchema: Boolean = true,
        location: String? = null,
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