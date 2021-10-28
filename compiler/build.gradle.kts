plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":lib"))
    implementation("com.squareup:kotlinpoet:1.10.2") {
        exclude(module = "kotlin-reflect")
    }
    implementation("com.squareup:kotlinpoet-ksp:1.10.2")
    implementation("com.google.devtools.ksp:symbol-processing-api:1.5.31-1.0.0")//TODO: reuse version

    testImplementation("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.4.4")
    testImplementation("junit:junit:4.12")
    testImplementation(kotlin("test"))
}


sourceSets.main {
    java.srcDirs("src/main/kotlin")
}
