# Slon

Postgres utils

## Gradle plugin

Adds `generateJooq` Gradle task that generate jOOQ classes based on Flyway migrations by applying those migrations on an ad-hoc Postgres Docker container using Testcontainers and then running jOOQ code generator on it

A simple config:
```kotlin
plugins {
    id("uk.matvey.slon") version "0.1.0"
}

slon {
    flywayDir.set(file("$projectDir/src/main/resources/db/migration"))
    jooqDir.set(file("$projectDir/src/main/jooq"))
    packageName.set("uk.matvey.generated.jooq")
}
```