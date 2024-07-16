plugins {
    kotlin("jvm") version "2.0.0" apply false
    kotlin("plugin.serialization") version "1.9.23" apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}