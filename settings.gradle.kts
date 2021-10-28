pluginManagement {
    plugins {
        id("com.google.devtools.ksp") version "1.5.31-1.0.0"
    }
    repositories {
        gradlePluginPortal()
    }
}

include(":lib", ":compiler")
