plugins {
    kotlin("jvm")
    id("maven-publish")
}

repositories {
    mavenCentral()
}

val kotlinVersion: String by project
val kspVersion: String by project

dependencies {

    implementation(project(":lib"))
    implementation(project(":lib-coroutines"))
    implementation("com.squareup:kotlinpoet:1.10.2") {
        exclude(module = "kotlin-reflect")
    }
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("com.squareup:kotlinpoet-ksp:1.10.2")
    implementation("com.google.devtools.ksp:symbol-processing:$kspVersion")
    implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")

    testImplementation("com.github.tschuchortdev:kotlin-compile-testing-ksp:1.4.7")
    testImplementation("junit:junit:4.13.2")
    testImplementation(kotlin("test"))
}

sourceSets.main {
    java.srcDirs("src/main/kotlin")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "deezer.kustomexport"
            artifactId = "compiler"

            from(components["java"])
        }
    }
}
