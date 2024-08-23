plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.google.devtools.ksp") version "2.0.0-1.0.21"

    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("xyz.jpenilla.run-paper") version "2.3.0"

    id("io.github.seggan.uom")
}

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io/")
    maven(url = "https://repo.aikar.co/content/groups/aikar/")
    maven(url = "https://hub.jeff-media.com/nexus/repository/jeff-media-public/")
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

    implementation("io.github.seggan:sf4k:0.4.1")

    testImplementation(kotlin("test"))
    testImplementation("io.strikt:strikt-core:0.34.0")
    testImplementation("com.github.seeseemelk:MockBukkit-v1.20:3.80.0")
    testImplementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-test:2.14.0")
}

group = "io.github.addoncommunity.galactifun"
version = "MODIFIED"

kotlin {
    jvmToolchain(17)
}

tasks.compileKotlin {
    compilerOptions.javaParameters = true
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

    mergeServiceFiles()
    // Relocate if true or not set, always relocate bstats
    doRelocate("org.bstats")
    if (System.getenv("RELOCATE") != "false") {
        doRelocate("io.github.seggan.kfun")
        doRelocate("co.aikar.commands")
        doRelocate("co.aikar.locales")
    } else {
        archiveClassifier = "unrelocated"
    }

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
    javaLauncher = javaToolchains.launcherFor {
        @Suppress("UnstableApiUsage")
        vendor = JvmVendorSpec.JETBRAINS
        languageVersion = JavaLanguageVersion.of(17)
    }
    jvmArgs("-XX:+AllowEnhancedClassRedefinition", "-XX:+AllowRedefinitionToAddDeleteMethods")
    downloadPlugins {
        url("https://blob.build/dl/Slimefun4/Dev/1116")
    }
    maxHeapSize = "4G"
    minecraftVersion("1.20.4")
}

uom {
    val kmPerLy = 9.461e12
    val kmPerAu = 1.495978707e8

    allowKoltinxSerialization = true
    pkg = "io.github.addoncommunity.galactifun.units"
    val time = existingMeasure("kotlin.time.Duration", "doubleSeconds")
    val distance = measure("Distance", "meters") {
        unit("lightYears", kmPerLy * 1000)
        unit("kilometers", 1000.0)
        unit("au", kmPerAu * 1000)
    }
    val mass = measure("Mass", "kilograms") {
        unit("tons", 1000.0)
    }
    val velocity = measure("Velocity", "metersPerSecond")
    val acceleration = measure("Acceleration", "metersPerSecondSquared")
    val force = measure("Force", "newtons") {
        unit("kilonewtons", 1000.0)
        unit("meganewtons", 1_000_000.0)
    }
    val volume = measure("Volume", "liters") {
        unit("cubicMeters", 1000.0)
    }
    val density = measure("Density", "kilogramsPerLiter")

    distance times time resultsIn velocity
    velocity dividedBy time resultsIn acceleration
    acceleration times time resultsIn velocity
    mass dividedBy volume resultsIn density
    density times volume resultsIn mass
    acceleration times mass resultsIn force
    force dividedBy acceleration resultsIn mass
}