buildscript {
    val kotlinVersion: String by project
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    }
}

val localProperties = java.util.Properties().apply {
    val propPath = "local.properties"
    if (File(propPath).exists()) {
        load(java.io.FileInputStream(File(rootProject.rootDir, propPath)))
    }
}

plugins {
    id("maven-publish")
    id("org.jlleitschuh.gradle.ktlint") version "10.2.0"
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        google()
    }
}

subprojects {
    apply(plugin = "maven-publish")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    ktlint {
        debug.set(true)
        filter {
            exclude { element -> element.file.path.contains("build/") }
        }
    }

    group = "com.deezer.kustom"

    if (localProperties.getProperty("REPOSITORY_URL") != null) {
        publishing {
            version = "0.0.1-SNAPSHOT"

            repositories {
                maven {
                    url = uri(localProperties.getProperty("REPOSITORY_URL"))
                    credentials {
                        username = localProperties.getProperty("REPOSITORY_USERNAME")
                        password = localProperties.getProperty("REPOSITORY_PASSWORD")
                    }
                }
            }
        }
    }
}
