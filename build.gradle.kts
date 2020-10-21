import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
    kotlin("plugin.serialization") version "1.4.10"
    application
}
group = "com.github.forrestdp"
version = "0.0.3"

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
    implementation(group = "org.jetbrains.kotlin", name = "kotlin-reflect", version = "1.4.10")
    implementation(group = "org.jetbrains.exposed", name = "exposed-core", version = "0.28.1")
    implementation(group = "org.jetbrains.exposed", name = "exposed-jdbc", version = "0.28.1")
    implementation(group = "org.jetbrains.exposed", name = "exposed-dao", version = "0.28.1")
    implementation(group = "org.postgresql", name = "postgresql", version = "42.2.18")
    implementation(group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version = "1.0.0")
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
application {
    mainClassName = "MainKt"
}