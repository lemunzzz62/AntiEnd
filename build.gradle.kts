plugins {
    id("java-library")
    id("xyz.jpenilla.run-paper") version "3.1.0"
    id("com.gradleup.shadow") version "9.6.1"
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.3.build.+")
    implementation("fr.mrmicky:fastboard:2.2.2")
}

java {
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

tasks {
    // Only ship the shaded jar; the plain one lacks fastboard and would crash on load.
    jar {
        enabled = false
    }

    shadowJar {
        relocate("fr.mrmicky.fastboard", "org.bl4nk.fastboard")
        archiveFileName.set("AntiEnd.jar")
    }

    runServer {
        // Configure the Minecraft version for our task.
        // This is the only required configuration besides applying the plugin.
        // Your plugin's jar (or shadowJar if present) will be used automatically.
        minecraftVersion("26.3")
        jvmArgs("-Xms2G", "-Xmx2G")
    }

    processResources {
        val props = mapOf("version" to version)
        // Track props as inputs so a version bump actually re-expands plugin.yml.
        inputs.properties(props)
        filesMatching("plugin.yml") {
            expand(props)
        }
    }
}
