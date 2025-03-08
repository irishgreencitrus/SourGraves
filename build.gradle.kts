plugins {
    id("com.gradleup.shadow") version "8.3.6"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10"
    kotlin("jvm") version "2.1.10"
    application
}
val pluginName = "SourGraves"
val minecraftVersion = "1.21.4"
val pluginVersion = "0.1.0"
group = "io.github.irishgreencitrus"
version = "$minecraftVersion-$pluginVersion"
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

tasks.processResources {
    filesMatching("plugin.yml") {
        expand(
            "pluginName" to pluginName,
            "pluginVersion" to pluginVersion,
            "minecraftVersion" to minecraftVersion,
        )
    }
}

tasks.shadowJar {
    archiveFileName = "$pluginName-$version.jar"
}

tasks.jar {
    archiveFileName = "$pluginName-$version-slim.jar"
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