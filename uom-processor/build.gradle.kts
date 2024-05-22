plugins {
    kotlin("jvm")
}

group = "io.github.seggan"
version = "0.1-SNAPSHOT"

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":uom"))
    implementation("com.google.devtools.ksp:symbol-processing-api:2.0.0-1.0.21")
    implementation("com.squareup:kotlinpoet-ksp:1.16.0")
}

kotlin {
    jvmToolchain(17)
}