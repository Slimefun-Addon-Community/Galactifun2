plugins {
    kotlin("jvm")
}

group = "io.github.seggan"
version = "0.1-SNAPSHOT"

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.google.devtools.ksp:symbol-processing-api:1.9.22-1.0.17") {
        isTransitive = false
    }
}

kotlin {
    jvmToolchain(17)
}