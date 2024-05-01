plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.9.23"
    id("com.google.devtools.ksp") version "1.9.23-1.0.20"

    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("xyz.jpenilla.run-paper") version "2.2.0"
}

repositories {
    mavenCentral()
    maven("https://jitpack.io/")
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://hub.jeff-media.com/nexus/repository/jeff-media-public/")
    maven("https://repo.metamechanists.org/releases/")
}

dependencies {
    fun DependencyHandlerScope.libraryAndTest(dependency: Any) {
        library(dependency)
        testImplementation(dependency)
    }

    fun DependencyHandlerScope.compileOnlyAndTest(dependency: Any) {
        compileOnly(dependency)
        testImplementation(dependency)
    }

    library(kotlin("stdlib"))
    libraryAndTest("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0-RC2")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0") // For some reason libraryloader doesn't like this
    libraryAndTest(kotlin("reflect"))

    libraryAndTest(kotlin("scripting-common"))
    libraryAndTest(kotlin("scripting-jvm"))
    libraryAndTest(kotlin("scripting-jvm-host"))
    libraryAndTest(kotlin("script-runtime"))

    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    compileOnlyAndTest("com.github.Slimefun:Slimefun4:RC-37")

    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")
    implementation("com.jeff_media:MorePersistentDataTypes:2.4.0")

    implementation("org.metamechanists:DisplayModelLib:23")

    implementation("io.github.seggan:sf4k:0.3.2")

    implementation(project(":uom"))
    ksp(project(":uom-processor"))

    testImplementation(kotlin("test"))
    testImplementation("io.kotest:kotest-assertions-core:5.8.0")
    testImplementation("com.github.seeseemelk:MockBukkit-v1.20:3.80.0")
    testImplementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-test:2.14.0")
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

    fun doRelocate(from: String) {
        val last = from.split(".").last()
        relocate(from, "io.github.addoncommunity.galactifun.shadowlibs.$last")
    }

    doRelocate("io.github.seggan.kfun")
    doRelocate("org.bstats")
    doRelocate("co.aikar.commands")
    doRelocate("co.aikar.locales")
    doRelocate("com.jeff_media.morepersistentdatatypes")
    doRelocate("org.metamechanists.displaymodellib")

    dependencies {
        exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib"))
        exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib-jdk8"))
        exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib-jdk7"))
        exclude(dependency("org.jetbrains.kotlin:kotlin-stdlib-common"))
    }

    archiveBaseName = "galactifun2"
}

bukkit {
    name = rootProject.name
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