plugins {
    kotlin("multiplatform")
}

kotlin {
    js(IR) {
        browser()
    }
    jvm()
    ios()
    iosSimulatorArm64()
    tvos()
    tvosSimulatorArm64()
    watchos()
    watchosSimulatorArm64()

    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
            languageSettings.optIn("kotlin.js.ExperimentalJsExport")
        }
        val commonMain by getting {
            dependencies {
                implementation(project(":lib"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
            }
        }
        val tvosMain by getting
        val tvosSimulatorArm64Main by getting { dependsOn(tvosMain)}
        val watchosMain by getting
        val watchosSimulatorArm64Main by getting { dependsOn(watchosMain)}
    }

    targets.all {
        compilations.all {
            // Cannot enable rn due to native issue (stdlib included more than once)
            // may be related to https://youtrack.jetbrains.com/issue/KT-46636
            kotlinOptions.allWarningsAsErrors = false
        }
    }
}
