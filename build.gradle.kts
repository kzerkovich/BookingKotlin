plugins {
    kotlin("jvm") version "1.9.24"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

val exposedVersion: String by project
val ktorVersion: String by project
dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.9.24")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.2")

    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")

    testImplementation("org.testcontainers:junit-jupiter:1.19.3")
    testImplementation("org.testcontainers:postgresql:1.19.3")

    testImplementation("io.mockk:mockk:1.13.4")

    implementation("io.insert-koin:koin-core:3.5.0")
    testImplementation("io.insert-koin:koin-test:3.5.0") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-test-junit")
    }

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-server-openapi:$ktorVersion")
    implementation("io.insert-koin:koin-ktor:3.5.0")
    implementation("org.valiktor:valiktor-core:0.12.0")


    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-json:$exposedVersion")

    implementation("org.postgresql:postgresql:42.7.5")

    implementation("org.liquibase:liquibase-core:4.31.1")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}