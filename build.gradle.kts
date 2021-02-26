import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.30"
    kotlin("plugin.serialization") version "1.4.30"
    id("io.gitlab.arturbosch.detekt") version "1.14.2"
    application
}
group = "com.github.forrestdp"
version = "0.0.4"

repositories {
    mavenCentral()
    jcenter()
    maven {
        url = uri("https://jitpack.io")
    }
}
dependencies {
    testImplementation(kotlin("test-junit"))
    implementation(group = "com.github.kotlin-telegram-bot.kotlin-telegram-bot", name = "telegram", version = "5.0.0")
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-reflect", version = "1.4.30")
    implementation(group = "org.jetbrains.exposed", name = "exposed-core", version = "0.29.1")
    implementation(group = "org.jetbrains.exposed", name = "exposed-jdbc", version = "0.29.1")
    implementation(group = "org.jetbrains.exposed", name = "exposed-dao", version = "0.29.1")
    implementation(group = "org.postgresql", name = "postgresql", version = "42.2.19")
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version = "1.1.0")

    detektPlugins(group = "io.gitlab.arturbosch.detekt", name = "detekt-formatting", version = "1.14.2")
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.useIR = true
}
application {
    mainClassName = "MainKt"
}