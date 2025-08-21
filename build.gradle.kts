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
    maven("https://jitpack.io")
}

dependencies {
    compileOnly(files("libs/spigot-1.8.8.jar"))

    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.0")

    implementation("com.github.ofrostdev:command-framework:main-SNAPSHOT")
    implementation("com.github.iDimaBR:sql-provider:main-SNAPSHOT")
    implementation("com.github.LMS5413:inventory-api:main-SNAPSHOT")
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

tasks {


    shadowJar {
      archiveClassifier.set("")
      mergeServiceFiles()

      exclude("org/spigotmc/**")
      exclude("org/bukkit/**")

      relocate("kotlin", "com.github.ofrostdev.api.libs.kotlin")
      relocate("me.saiintbrisson", "com.github.ofrostdev.api.libs.saiintbrisson")
      relocate("com.henryfabio", "com.github.ofrostdev.api.libs.henryfabio")
      relocate("com.cryptomorin", "com.github.ofrostdev.api.libs.cryptomorim")
      relocate("com.zaxxer", "com.github.ofrostdev.api.libs.zaxxer")
      relocate("org", "com.github.ofrostdev.api.libs")
  }
    jar{
        enabled = false
    }
}

val targetJavaVersion = 8
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.processResources {
    val props = mapOf("version" to version)
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }
}
