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

    // TODO: All platform required even if not really different from main?

    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
            languageSettings.optIn("kotlin.js.ExperimentalJsExport")
        }
    }

    targets.all {
        compilations.all {
            // Cannot enable rn due to native issue (stdlib included more than once)
            // may be related to https://youtrack.jetbrains.com/issue/KT-46636
            kotlinOptions.allWarningsAsErrors = false
        }
    }
}
