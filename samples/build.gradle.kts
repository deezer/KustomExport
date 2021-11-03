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
            dependencies {
                implementation(project(":lib"))
            }
        }
        val jsMain by getting {
            dependencies {
                kotlin.srcDir("build/generated/ksp/jsMain/kotlin")
            }
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
