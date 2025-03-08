plugins {
    id("com.gradleup.shadow") version "8.3.6"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10"
    kotlin("jvm") version "2.1.10"
    application
}

val minecraftVersion = "1.21.4"
group = "io.github.irishgreencitrus"
version = "${minecraftVersion}-dev1"
val paperApiVersion = "$minecraftVersion-R0.1-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("net.peanuuutz.tomlkt:tomlkt:0.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    compileOnly("io.papermc.paper:paper-api:$paperApiVersion")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(8)
}
java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

application {
    mainClass.set("MainKt")
}