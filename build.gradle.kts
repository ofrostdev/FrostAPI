plugins {
    kotlin("jvm") version "2.0.0"
    id("com.gradleup.shadow") version "8.3.0"
    `maven-publish`
}

group = "com.github.ofrostdev"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") {
        name = "spigotmc-repo"
    }
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    maven("https://jitpack.io")
}

dependencies {

    compileOnly(files("libs/spigot-1.8.8.jar"))
    compileOnly("me.clip:placeholderapi:2.11.6")

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.0")

    implementation("commons-lang:commons-lang:2.6")

    implementation("com.github.ofrostdev:command-framework:main-SNAPSHOT")
    implementation("com.github.iDimaBR:sql-provider:main-SNAPSHOT")
    implementation("com.github.LMS5413:inventory-api:main-SNAPSHOT")
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        mergeServiceFiles()

        relocate("me.saiintbrisson", "com.github.ofrostdev.api.libs.saiintbrisson")
        relocate("com.henryfabio", "com.github.ofrostdev.api.libs.henryfabio")
        relocate("com.cryptomorin", "com.github.ofrostdev.api.libs.cryptomorim")
        relocate("com.zaxxer", "com.github.ofrostdev.api.libs.zaxxer")
        relocate("net.kyori", "com.github.ofrostdev.api.libs.kyori")

        relocate("org.apache", "com.github.ofrostdev.api.libs.apache")

        relocate("org", "com.github.ofrostdev.api.libs") {
            exclude("org/bukkit/**")
            exclude("org/spigotmc/**")
        }
    }

    jar {
        enabled = false
    }

    build {
        dependsOn(shadowJar)
    }

    processResources {
        val props = mapOf("version" to version)
        inputs.properties(props)
        filteringCharset = "UTF-8"
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}

val targetJavaVersion = 8
kotlin {
    jvmToolchain(targetJavaVersion)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifact(tasks.shadowJar) {
                builtBy(tasks.shadowJar)
            }
            groupId = project.group.toString()
            artifactId = "FrostAPI"
            version = project.version.toString()
        }
    }
}
