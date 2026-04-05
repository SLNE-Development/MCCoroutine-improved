import org.gradle.jvm.tasks.Jar
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmExtension

plugins {
    kotlin("jvm") version "2.3.20" apply false
    id("org.jetbrains.dokka-javadoc") version "2.2.0" apply false
    `java-library`
    `maven-publish`
}

allprojects {
    group = "dev.slne.forks.mccoroutine"
    version = findProperty("version") as String
}

subprojects {
    val isApiModule = name.endsWith("-api")

    apply(plugin = "java-library")

    val dokkaJavadocJar = if (isApiModule) {
        apply(plugin = "org.jetbrains.dokka-javadoc")

        tasks.register<Jar>("dokkaJavadocJar") {
            group = JavaBasePlugin.DOCUMENTATION_GROUP
            description = "Assembles a JAR containing Dokka Javadoc output."
            archiveClassifier.set("javadoc")
            dependsOn(tasks.named("dokkaGeneratePublicationJavadoc"))
            from(tasks.named("dokkaGeneratePublicationJavadoc"))
        }
    } else {
        null
    }

    dependencies {
        compileOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    }

    extensions.findByType<KotlinJvmExtension>()?.apply {
        jvmToolchain(25)
    }

    java {
        withSourcesJar()
        if (!isApiModule) {
            withJavadocJar()
        }
    }

    if (dokkaJavadocJar != null) {
        tasks.named("assemble") {
            dependsOn(dokkaJavadocJar)
        }
    }

    apply(plugin = "maven-publish")

    publishing {
        repositories {
            maven("https://reposilite.slne.dev/releases") {
                name = "slne-repository-releases"
                credentials {
                    username = System.getenv("SLNE_RELEASES_REPO_USERNAME")
                    password = System.getenv("SLNE_RELEASES_REPO_PASSWORD")
                }
            }
        }

        publications.create<MavenPublication>("maven") {
            from(components["java"])

            dokkaJavadocJar?.let {
                artifact(it)
            }
        }
    }
}