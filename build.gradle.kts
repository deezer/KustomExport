buildscript {
    val kotlinVersion: String by project
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    }
}

val localProperties = java.util.Properties().apply {
    val propPath = "local.properties"
    if (File(propPath).exists()) {
        load(java.io.FileInputStream(File(rootProject.rootDir, propPath)))
    }
}

plugins {
    id("maven-publish")
    id("org.ajoberstar.git-publish") version "3.0.0"
    id("org.ajoberstar.grgit") version "4.1.1"
    id("org.jlleitschuh.gradle.ktlint") version "10.2.0"
}

allprojects {
    group = "deezer.kustomexport"
    version = "0.0.3-SNAPSHOT"

    repositories {
        mavenLocal()
        mavenCentral()
        google()
    }
}

// Required by M1 for now (no node build for v14 on M1)
rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin> {
    rootProject.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>().nodeVersion = "16.0.0"
}

subprojects {
    apply(plugin = "maven-publish")

    publishing {
        repositories {
            maven {
                url = uri(localProperties.getProperty("REPOSITORY_URL"))
                credentials {
                    username = localProperties.getProperty("REPOSITORY_USERNAME")
                    password = localProperties.getProperty("REPOSITORY_PASSWORD")
                }
            }
        }
    }
}

val gitUser = System.getenv("GIT_USER")
val gitPassword = System.getenv("GIT_PASSWORD")
if (gitUser != null && gitPassword != null) {
    System.setProperty("org.ajoberstar.grgit.auth.username", gitUser)
    System.setProperty("org.ajoberstar.grgit.auth.password", gitPassword)
}

tasks.create<Sync>("copyMavenLocalArtifacts") {
    group = "publishing"
    dependsOn(":compiler:publishToMavenLocal", ":lib:publishToMavenLocal")

    val userHome = System.getProperty("user.home")
    val groupDir = project.group.toString().replace('.', '/')
    val localRepository = "$userHome/.m2/repository/$groupDir/"

    from(localRepository) {
        include("*/${project.version}/**")
    }

    into("$buildDir/mvn-repo/$groupDir/")
}

gitPublish {
    repoUri.set("git@github.com:deezer/KustomExport.git")
    branch.set("mvn-repo")
    contents.from("$buildDir/mvn-repo")
    preserve { include("**") }
    val head = grgit.head()
    commitMessage.set("${head.abbreviatedId}: ${project.version} : ${head.fullMessage}")
}
tasks["gitPublishCopy"].dependsOn("copyMavenLocalArtifacts")
