plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    kotlin("jvm") version "1.9.20"
    application
}

group = "io.github.irishgreencitrus"
version = "dev1"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    testImplementation(kotlin("test"))
    compileOnly("io.papermc.paper:paper-api:1.20.2-R0.1-SNAPSHOT")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

application {
    mainClass.set("MainKt")
}