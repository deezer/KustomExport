pluginManagement {
    plugins {
        id("com.google.devtools.ksp") version "1.6.0-RC-1.0.1-RC"
    }
    repositories {
        gradlePluginPortal()
    }
}

include(":lib", ":compiler", ":samples")
