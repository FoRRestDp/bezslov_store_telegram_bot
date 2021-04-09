import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    kotlin("jvm") version "1.4.32"
    kotlin("plugin.serialization") version "1.4.32"
    id("io.gitlab.arturbosch.detekt") version "1.16.0"
    application
}
group = "com.github.forrestdp"
version = "0.0.5"

repositories {
    mavenCentral()
    jcenter()
    maven("https://jitpack.io")
}
dependencies {
    testImplementation(kotlin("test-junit"))
    implementation(group = "com.github.kotlin-telegram-bot.kotlin-telegram-bot", name = "telegram", version = "6.0.4")
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-reflect", version = "1.4.32")
    implementation(group = "org.jetbrains.exposed", name = "exposed-core", version = "0.30.1")
    implementation(group = "org.jetbrains.exposed", name = "exposed-jdbc", version = "0.30.1")
    implementation(group = "org.jetbrains.exposed", name = "exposed-dao", version = "0.30.1")
    implementation(group = "org.postgresql", name = "postgresql", version = "42.2.19")
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version = "1.1.0")

    detektPlugins(group = "io.gitlab.arturbosch.detekt", name = "detekt-formatting", version = "1.14.32")
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.useIR = true
}
application {
    mainClass.set("MainKt")
}