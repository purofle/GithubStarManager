plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

group = "tech.archlinux"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-client-cio-jvm:3.0.3")
    testImplementation(kotlin("test"))

    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.auth)
    implementation(libs.ktor.client.logging)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)

    implementation(libs.apache.logging.log4j.core)
    implementation(libs.apache.logging.log4j.slf4j2.impl)

    implementation(libs.pgvector.pgvector)
    implementation(libs.postgresql.r2dbc)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}