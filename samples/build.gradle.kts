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
        val commonMain by getting {
            // KSP issue is limiting JS generation at the moment
            // https://github.com/google/ksp/issues/728
            kotlin.srcDir("build/generated/ksp/commonMain/kotlin")

            dependencies {
                implementation(project(":lib"))
            }
        }
        val jsMain by getting {
            // KSP issue is limiting JS generation at the moment
            // https://github.com/google/ksp/issues/728
            kotlin.srcDir("build/generated/ksp/commonMain/kotlin")
            // Eventually we should go with:
            // kotlin.srcDir("build/generated/ksp/jsMain/kotlin")
        }
    }
}

dependencies {
    // Enable KSP generation only when the flag is passed,
    // so we can generate and build in 2 different Gradle run, and avoid
    // KotlinJsIr crash
    val enableKsp: String? by project
    if (enableKsp == "true") {
        // KSP issue is limiting JS generation at the moment
        // https://github.com/google/ksp/issues/728
        // As a hack, we generate JS facade for KotlinMetadata instead, so we define this expect/actual pattern

        //add("kspJs", project(":compiler"))
        add("kspMetadata", project(":compiler"))
    }
}

ksp {
    arg("erasePackage", "false")
}
