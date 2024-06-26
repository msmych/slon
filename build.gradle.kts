plugins {
    kotlin("jvm") version "2.0.0"
    `java-library`
    `maven-publish`
    id("org.jreleaser") version "1.12.0"
}

repositories {
    mavenCentral()
}

val assertjVersion: String by project
val hikariCpVersion: String by project
val junitVersion: String by project
val kotlinLoggingJvmVersion: String by project
val logbackClassicVersion: String by project
val postgresqlVersion: String by project
val testcontainersVersion: String by project

dependencies {
    implementation("ch.qos.logback:logback-classic:$logbackClassicVersion")
    implementation("com.zaxxer:HikariCP:$hikariCpVersion")
    implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingJvmVersion")
    implementation("org.postgresql:postgresql:$postgresqlVersion")

    testImplementation(platform("org.junit:junit-bom:$junitVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("org.assertj:assertj-core:$assertjVersion")

    testImplementation(platform("org.testcontainers:testcontainers-bom:$testcontainersVersion"))
    testImplementation("org.testcontainers:postgresql")
}

tasks.test {
    useJUnitPlatform()
}

java {
    withJavadocJar()
    withSourcesJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "uk.matvey"
            artifactId = "slon"

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
                url = uri(layout.buildDirectory.dir("staging-deploy"))
            }
        }
    }
}

val releaseVersion = project.findProperty("releaseVersion") ?: "0.1.0-SNAPSHOT"

jreleaser {
    signing {
        setActive("ALWAYS")
        armored = true
    }
    deploy {
        maven {
            github {
                enabled = false
            }
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
        version = releaseVersion
    }
}