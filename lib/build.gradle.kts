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
}
