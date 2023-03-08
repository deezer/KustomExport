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
    implementation("com.squareup:kotlinpoet:1.12.0") {
        exclude(module = "kotlin-reflect")
    }
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    implementation("com.squareup:kotlinpoet-ksp:1.12.0")
    implementation("com.google.devtools.ksp:symbol-processing:$kspVersion")
    implementation("com.google.devtools.ksp:symbol-processing-api:$kspVersion")
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
