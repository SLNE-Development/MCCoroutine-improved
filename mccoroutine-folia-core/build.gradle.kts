plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":mccoroutine-folia-api"))
    compileOnly(libs.paper.api)
}