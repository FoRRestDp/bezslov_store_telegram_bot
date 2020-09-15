import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
    application
}
group = "com.github.forrestdp"
version = "0.0.1"

repositories {
    mavenCentral()
    maven { 
        url = uri("https://jitpack.io")
    }
}
dependencies {
    testImplementation(kotlin("test-junit"))
    implementation(group = "com.github.kotlin-telegram-bot", name = "kotlin-telegram-bot", version = "5.0.0")
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "13"
}
application {
    mainClassName = "MainKt"
}