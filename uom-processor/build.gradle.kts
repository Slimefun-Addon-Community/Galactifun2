plugins {
    kotlin("jvm")
}

group = "io.github.seggan"
version = "0.1-SNAPSHOT"

dependencies {
    implementation(kotlin("stdlib"))
    implementation(project(":uom"))
    implementation("com.google.devtools.ksp:symbol-processing-api:1.9.22-1.0.17")
    implementation("com.squareup:kotlinpoet-ksp:1.16.0")
}

kotlin {
    jvmToolchain(17)
}