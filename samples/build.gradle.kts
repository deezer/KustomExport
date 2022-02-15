plugins {
    kotlin("multiplatform")
    id("com.google.devtools.ksp")
}

kotlin {
    js(IR) {
        moduleName = "@kustom/Samples"
        compilations["main"].packageJson {
            customField("main", "kotlin/Samples.js")
            customField("types", "kotlin/Samples.d.ts")
        }
        browser()
        binaries.library()
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
            languageSettings.optIn("kotlin.js.ExperimentalJsExport")
        }

        val commonMain by getting {
            // KSP issue is limiting JS generation at the moment
            // https://github.com/google/ksp/issues/728
            kotlin.srcDir("build/generated/ksp/commonMain/kotlin")

            dependencies {
                implementation(project(":lib"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
            }
        }
        val jsMain by getting {
            // KSP issue is limiting JS generation at the moment
            // https://github.com/google/ksp/issues/728
            kotlin.srcDir("build/generated/ksp/commonMain/kotlin")
            // Eventually we should go with:
            // kotlin.srcDir("build/generated/ksp/jsMain/kotlin")
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-js:1.6.0")
            }
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

dependencies {
    // Enable KSP generation only when the flag is passed,
    // so we can generate and build in 2 different Gradle run, and avoid
    // KotlinJsIr crash
    val enableKsp: String? by project
    if (enableKsp == "true") {
        add("kspJs", project(":compiler"))
    }
}

ksp {
    arg("erasePackage", "false")
}
