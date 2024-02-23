plugins {
    kotlin("jvm") version "1.9.22"
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.3"
    id("xyz.jpenilla.run-paper") version "2.2.0"
}

repositories {
    mavenCentral()
    maven(url = "https://papermc.io/repo/repository/maven-public/")
    maven(url = "https://jitpack.io/")
    maven(url = "https://repo.aikar.co/content/groups/aikar/")
    maven(url = "https://hub.jeff-media.com/nexus/repository/jeff-media-public/")
}

fun DependencyHandlerScope.libraryAndTest(dependency: Any) {
    library(dependency)
    testImplementation(dependency)
}

fun DependencyHandlerScope.compileOnlyAndTest(dependency: Any) {
    compileOnly(dependency)
    testImplementation(dependency)
}

dependencies {
    library(kotlin("stdlib"))
    libraryAndTest("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0-RC2")
    libraryAndTest("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")

    libraryAndTest(kotlin("scripting-common"))
    libraryAndTest(kotlin("scripting-jvm"))
    libraryAndTest(kotlin("scripting-jvm-host"))
    libraryAndTest(kotlin("script-runtime"))

    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    compileOnlyAndTest("com.github.Slimefun:Slimefun4:206a9d6")

    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    implementation("com.jeff_media:MorePersistentDataTypes:2.4.0")

    implementation("com.github.Seggan:kfun:0.1.0")

    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-assertions-core:5.8.0")
    // Need MockBukkit for unimplemented entities
    compileOnlyAndTest("com.github.seeseemelk:MockBukkit-v1.20:3.74.0")
}

group = "io.github.addoncommunity.galactifun"
version = "MODIFIED"

kotlin {
    jvmToolchain(17)
}

tasks.compileKotlin {
    kotlinOptions.javaParameters = true
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
}

tasks.shadowJar {
    dependsOn(tasks.test)
    relocate("io.github.seggan.kfun", "io.github.addoncommunity.galactifun.kfun")
    relocate("org.bstats", "io.github.addoncommunity.galactifun.bstats")
    relocate("co.aikar.commands", "io.github.addoncommunity.galactifun.acf")
    relocate("co.aikar.locales", "io.github.addoncommunity.galactifun.acf.locales")
    relocate("com.jeff_media.morepersistentdatatypes", "io.github.addoncommunity.galactifun.pdts")

    dependencies {
        exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib"))
    }
}

bukkit {
    name = project.name
    main = "io.github.addoncommunity.galactifun.Galactifun2"
    version = project.version.toString()
    author = "Seggan"
    apiVersion = "1.20"
    softDepend = listOf("ClayTech")
    loadBefore = listOf("Multiverse-Core")
    depend = listOf("Slimefun")
}

tasks.runServer {
    downloadPlugins {
        url("https://blob.build/dl/Slimefun4/Dev/1116")
    }
    maxHeapSize = "4G"
    minecraftVersion("1.20.4")
}