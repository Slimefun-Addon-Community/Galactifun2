plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.squareup:kotlinpoet:1.16.0")
}

gradlePlugin {
    plugins {
        create("uom") {
            id = "io.github.seggan.uom"
            implementationClass = "io.github.seggan.uom.UomPlugin"
        }
    }
}