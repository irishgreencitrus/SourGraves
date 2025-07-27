plugins {
    id("com.gradleup.shadow") version "8.3.6"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10"
    kotlin("jvm") version "2.1.10"
    application
}
val pluginName = "SourGraves"
val minecraftVersion = properties.getOrDefault("minecraftVersion", "1.21")
val pluginVersion = properties.getOrDefault("pluginVersion", "2.1.0")
group = "io.github.irishgreencitrus"
version = "$minecraftVersion-$pluginVersion"
val paperApiVersion = "$minecraftVersion-R0.1-SNAPSHOT"

val exposedVersion: String by project

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.org/repository/maven-public/")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("net.peanuuutz.tomlkt:tomlkt:0.4.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.8.0")
    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation("com.zaxxer:HikariCP:6.3.0")
    compileOnly("io.papermc.paper:paper-api:$paperApiVersion")
    implementation("com.mysql:mysql-connector-j:9.3.0") {
        exclude(group = "com.google.protobuf", module = "protobuf-java")
    }
    implementation("org.postgresql:postgresql:42.7.7")
    compileOnly("net.milkbowl.vault:VaultUnlockedAPI:2.11")
}

tasks.test {
    useJUnitPlatform()
}

tasks.processResources {
    outputs.upToDateWhen { false }
    filesMatching("plugin.yml") {
        expand(
            "pluginName" to pluginName,
            "pluginVersion" to pluginVersion,
            "minecraftVersion" to minecraftVersion,
        )
    }
}

tasks.shadowJar {
    relocate("org.bstats", "io.github.irishgreencitrus.stats")
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