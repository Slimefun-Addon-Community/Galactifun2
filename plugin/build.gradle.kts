import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")

    id("com.gradleup.shadow") version "8.3.2"
    id("net.minecrell.plugin-yml.paper") version "0.6.0"
    id("xyz.jpenilla.run-paper") version "2.3.0"

    id("io.github.seggan.uom")
}

repositories {
    mavenCentral()
    maven("https://jitpack.io/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://repo.metamechanists.org/releases/")
}

dependencies {
    fun DependencyHandlerScope.libraryAndTest(dependency: Any) {
        paperLibrary(dependency)
        testImplementation(dependency)
    }

    fun DependencyHandlerScope.compileOnlyAndTest(dependency: Any) {
        compileOnly(dependency)
        testImplementation(dependency)
    }

    paperLibrary(kotlin("stdlib"))
    libraryAndTest("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0-RC2")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0") // For some reason libraryloader doesn't like this
    libraryAndTest(kotlin("reflect"))

    libraryAndTest(kotlin("scripting-common"))
    libraryAndTest(kotlin("scripting-jvm"))
    libraryAndTest(kotlin("scripting-jvm-host"))
    libraryAndTest(kotlin("script-runtime"))

    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")
    compileOnlyAndTest("com.github.Slimefun:Slimefun4:e02a0f61d1")

    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation("co.aikar:acf-paper:0.5.1-SNAPSHOT")

    implementation("org.metamechanists:DisplayModelLib:34")

    implementation("io.github.seggan:sf4k:0.6.0")

    testImplementation(kotlin("test"))
    testImplementation("io.strikt:strikt-core:0.34.0")
    implementation("com.github.seeseemelk:MockBukkit-v1.20:3.93.2")
    testImplementation("com.github.shynixn.mccoroutine:mccoroutine-bukkit-test:2.19.0")
}

group = "io.github.addoncommunity.galactifun"
version = "MODIFIED"

kotlin {
    jvmToolchain(21)
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
    manifest {
        attributes["paperweight-mappings-namespace"] = "mojang"
    }

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
        doRelocate("org.metamechanists.displaymodellib")
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

paper {
    name = rootProject.name
    main = "io.github.addoncommunity.galactifun.Galactifun2"
    loader = "io.github.addoncommunity.galactifun.Galactifun2Loader"
    bootstrapper = "io.github.addoncommunity.galactifun.Galactifun2Bootstrapper"
    version = project.version.toString()
    author = "Seggan"
    apiVersion = "1.20"
    generateLibrariesJson = true

    serverDependencies {
        register("Multiverse-Core") {
            required = false
            load = PaperPluginDescription.RelativeLoadOrder.AFTER
        }

        register("Slimefun") {
            required = true
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
            joinClasspath = true
        }
    }
}

tasks.runServer {
    javaLauncher = javaToolchains.launcherFor {
        @Suppress("UnstableApiUsage")
        vendor = JvmVendorSpec.JETBRAINS
        languageVersion = JavaLanguageVersion.of(21)
    }
    jvmArgs("-XX:+AllowEnhancedClassRedefinition", "-XX:+AllowRedefinitionToAddDeleteMethods")
    downloadPlugins {
        url("https://blob.build/dl/Slimefun4/Dev/1154")
    }
    maxHeapSize = "4G"
    minecraftVersion("1.20.6")
}

uom {
    val kmPerLy = 9.461e12
    val kmPerAu = 1.495978707e8

    allowKoltinxSerialization = true
    pkg = "io.github.addoncommunity.galactifun.units"
    val time = existingMeasure("kotlin.time.Duration", "doubleSeconds") {
        scalarToUnit("seconds", true)
    }
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
    val pressure = measure("Pressure", "pascals") {
        unit("atmospheres", 101325.0)
    }
    val area = measure("Area", "squareMeters")
    val volume = measure("Volume", "cubicMeters") {
        unit("liters", 0.001)
    }
    val density = measure("Density", "kilogramsPerCubicMeter") {
        unit("kilogramsPerLiter", 1000.0)
    }

    distance times time resultsIn velocity
    acceleration times time resultsIn velocity
    density times volume resultsIn mass
    acceleration times mass resultsIn force
    distance times distance resultsIn area
    distance times area resultsIn volume
    pressure times area resultsIn force

    measure("Angle", "radians") {
        unit("degrees", Math.PI / 180)
    }
}