import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        // For now we cannot upgrade the version of BuildKonfig because we'll need to build with Java 11
        classpath("com.codingfeline.buildkonfig:buildkonfig-gradle-plugin:0.10.0")
    }
}

val kmpGroup by extra("com.deezer.kmp")
val versionBase by extra("0.15.0")

plugins {
    id("maven-publish")
    id("org.jlleitschuh.gradle.ktlint") version "10.2.0"
    //kotlin("multiplatform") apply false
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

    publishing {
        // TODO
    }
}
