plugins {
    `java-gradle-plugin`
    `maven-publish`
    kotlin("jvm") version "2.2.20"
    id("com.gradle.plugin-publish") version "1.2.1"
    signing
}

group = "uk.matvey"
version = "0.1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.testcontainers:postgresql:1.21.3")
    implementation("org.flywaydb:flyway-core:11.14.0")
    implementation("org.jooq:jooq-codegen:3.20.8")

    runtimeOnly("org.flywaydb:flyway-database-postgresql:11.14.0")
    runtimeOnly("org.postgresql:postgresql:42.7.8")

    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testImplementation("org.assertj:assertj-core:3.27.6")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

gradlePlugin {
    website = "https://matvey.uk/tech"
    vcsUrl = "https://github.com/msmych/slon.git"
    plugins {
        create("slon") {
            id = "uk.matvey.slon"
            displayName = "Slon"
            description = "Generate jOOQ classes based on Flyway migrations"
            tags = listOf("postgres", "flyway", "jooq", "testcontainers")
            implementationClass = "uk.matvey.slon.plugin.SlonPlugin"
        }
    }
}

kotlin {
    jvmToolchain(24)
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    repositories {
        maven {
            name = "localRepo"
            url = layout.buildDirectory.dir("local-repo").get().asFile.toURI()
        }
    }
}

signing {
    useGpgCmd()
    sign(publishing.publications)
}
