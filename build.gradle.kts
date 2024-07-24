plugins {
    kotlin("jvm") version "2.0.0"
    `java-library`
    `java-test-fixtures`
    `maven-publish`
    id("org.jreleaser") version "1.12.0"
}

repositories {
    mavenCentral()
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/msmych/kit")
        credentials {
            username = "GitHubPackages-RO"
            password = project.findProperty("ghPackagesRoToken") as? String ?: System.getenv("GH_PACKAGES_RO_TOKEN")
        }
    }
}

val assertjVersion: String by project
val flywayVersion: String by project
val hikariCpVersion: String by project
val junitVersion: String by project
val kitVersion: String by project
val kotlinLoggingJvmVersion: String by project
val logbackClassicVersion: String by project
val postgresqlVersion: String by project
val testcontainersVersion: String by project

dependencies {
    api("com.zaxxer:HikariCP:$hikariCpVersion")
    api("org.flywaydb:flyway-core:$flywayVersion")
    api("org.postgresql:postgresql:$postgresqlVersion")

    implementation("ch.qos.logback:logback-classic:$logbackClassicVersion")
    implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingJvmVersion")
    implementation("uk.matvey:kit:$kitVersion")

    testApi("org.assertj:assertj-core:$assertjVersion")
    testFixturesApi(platform("org.junit:junit-bom:$junitVersion"))
    testFixturesApi("org.junit.jupiter:junit-jupiter")
    testFixturesApi("org.junit.platform:junit-platform-launcher")
    testFixturesApi(platform("org.testcontainers:testcontainers-bom:$testcontainersVersion"))
    testFixturesApi("org.testcontainers:postgresql")
}

tasks.test {
    useJUnitPlatform()
}

java {
    withJavadocJar()
    withSourcesJar()
}

val slonVersion = project.findProperty("releaseVersion") as? String ?: "0.1.0-SNAPSHOT"

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "uk.matvey"
            artifactId = "slon"
            version = slonVersion

            from(components["java"])

            pom {
                name = "Slon"
                description = "Lightweight Kotlin library to work with Postgres"
                url = "https://github.com/msmych/slon"
                licenses {
                    license {
                        name = "Apache-2.0"
                        url = "https://spdx.org/licenses/Apache-2.0.html"
                    }
                }
                developers {
                    developer {
                        id = "msmych"
                        name = "Matvey Smychkov"
                        email = "realsmych@gmail.com"
                    }
                }
                scm {
                    connection = "scm:git:https://github.com/msmych/slon.git"
                    developerConnection = "scm:git:ssh://github.com/msmych/slon.git"
                    url = "https://github.com/msmych/slon"
                }
            }
        }

        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/msmych/kit")
                credentials {
                    username = System.getenv("GITHUB_ACTOR")
                    password = System.getenv("GH_TOKEN")
                }
            }
//            maven {
//                url = uri(layout.buildDirectory.dir("staging-deploy"))
//            }
        }
    }
}

jreleaser {
    signing {
        setActive("ALWAYS")
        armored = true
    }
    deploy {
        maven {
            mavenCentral {
                create("sonatype") {
                    setActive("ALWAYS")
                    namespace = "uk.matvey"
                    deploymentId = "slon"
                    url = "https://central.sonatype.com/api/v1/publisher"
                    stagingRepository("build/staging-deploy")
                }
            }
        }
        group = "uk.matvey"
        version = slonVersion
    }
}