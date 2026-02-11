import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension

plugins {
    kotlin("jvm") version "2.3.10" apply false
    `java-library`
    `kotlin-dsl`
    `maven-publish`
}

allprojects {
    group = "dev.slne.forks.mccoroutine"
    version = findProperty("version") as String
}

subprojects {
    apply(plugin = "java-library")

    dependencies {
        compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    }

    extensions.findByType<KotlinJvmExtension>()?.apply {
        jvmToolchain(25)
    }

    apply(plugin = "maven-publish")

    publishing {
        repositories {
            maven("https://repo.slne.dev/repository/maven-releases/") {
                name = "maven-releases"
                credentials {
                    username = System.getenv("SLNE_RELEASES_REPO_USERNAME")
                    password = System.getenv("SLNE_RELEASES_REPO_PASSWORD")
                }
            }
        }

        publications.create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}