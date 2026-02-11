plugins {
    kotlin("jvm")
}

dependencies {
    implementation(project(":mccoroutine-velocity-api"))
    compileOnly(libs.velocity.api)
    compileOnly(libs.log4j.core)
}