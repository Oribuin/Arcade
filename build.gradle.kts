plugins {
    `java-library`
    `maven-publish`
    id("com.gradleup.shadow") version "9.4.1"
    id("de.eldoria.plugin-yml.bukkit") version "0.9.0"
}

group = "dev.oribuin"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21

    disableAutoTargetJvm()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()
    mavenLocal()

    maven("https://repo.rosewooddev.io/repository/public/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://repo.codemc.org/repository/maven-public")
    maven("https://nexus.neetgames.com/repository/maven-snapshots/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("org.jetbrains:annotations:23.0.0")

    // Commands, Configs & Database
    api("org.spongepowered:configurate-yaml:4.2.0")
    api("org.incendo:cloud-core:2.0.0")
    api("org.incendo:cloud-annotations:2.0.0")
    api("org.incendo:cloud-paper:2.0.0")
    api("com.zaxxer:HikariCP:4.0.3")
    api("dev.triumphteam:triumph-gui:3.1.13") {
        exclude(group = "com.google.code.gson", module = "gson")
        exclude(group = "net.kyori", module = "*")
    }
    // Spigot
    compileOnly("io.papermc.paper:paper-api:26.2.build.+")

    // External Plugins
    compileOnly("me.clip:placeholderapi:2.12.3")
    compileOnly("com.arcaniax:HeadDatabase-API:1.3.2")
}

tasks {
    compileJava {
        this.options.compilerArgs.add("-parameters")
        this.options.isFork = true
        this.options.encoding = "UTF-8"
    }

    shadowJar {
        this.archiveClassifier.set("")
        this.relocate("com.zaxxer.hikari", "${project.group}.arcade.libs.hikari")
        this.relocate("dev.triumphteam.gui", "${project.group}.arcade.libs.triumphgui")
        this.relocate("io.leangen", "${project.group}.arcade.libs.leangen")
        this.relocate("org.slf4j", "${project.group}.arcade.libs.sl4fj")
        this.relocate("org.incendo", "${project.group}.arcade.libs.incendo")
        this.relocate("org.spongepowered", "${project.group}.arcade.libs.spongepowered")
        this.minimize()
    }

    bukkit {
        this.main = "dev.oribuin.arcade.ArcadePlugin"
        this.version = "${project.version}"
        this.author = "Oribuin"
        this.description = "hello"
        this.apiVersion = "26.1.2"
        this.foliaSupported = true
        this.softDepend = listOf("HeadDatabase", "PlaceholderAPI")
    }

    build {
        this.dependsOn(shadowJar)
    }
}