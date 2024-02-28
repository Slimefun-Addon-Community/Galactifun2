plugins {
    kotlin("jvm")
}

group = "io.github.seggan"
version = "0.1-SNAPSHOT"

dependencies {
    implementation(kotlin("stdlib"))
}

kotlin {
    jvmToolchain(17)
}