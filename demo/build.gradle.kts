plugins {
    application
    id("uk.matvey.slon") version "0.1.0"
}

slon {
    flywayDir.set(file("$projectDir/src/main/resources/db/migration"))
    jooqDir.set(file("$projectDir/src/main/jooq"))
    packageName.set("uk.matvey.generated.jooq")
}