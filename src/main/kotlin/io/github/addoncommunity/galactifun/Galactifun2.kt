package io.github.addoncommunity.galactifun

import co.aikar.commands.PaperCommandManager
import io.github.addoncommunity.galactifun.api.objects.planet.PlanetaryObject
import io.github.addoncommunity.galactifun.api.objects.planet.PlanetaryWorld
import io.github.addoncommunity.galactifun.api.objects.properties.Distance.Companion.au
import io.github.addoncommunity.galactifun.api.objects.properties.atmosphere.Gas
import io.github.addoncommunity.galactifun.api.objects.properties.atmosphere.composition
import io.github.addoncommunity.galactifun.base.BaseUniverse
import io.github.addoncommunity.galactifun.core.Gf2Command
import io.github.addoncommunity.galactifun.core.managers.PlanetManager
import io.github.addoncommunity.galactifun.scripting.PlanetScript
import io.github.addoncommunity.galactifun.scripting.dsl.*
import io.github.addoncommunity.galactifun.scripting.dsl.gen.*
import io.github.addoncommunity.galactifun.scripting.evalScript
import io.github.addoncommunity.galactifun.util.years
import io.github.seggan.kfun.AbstractAddon
import io.github.thebusybiscuit.slimefun4.api.MinecraftVersion
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun
import io.github.thebusybiscuit.slimefun4.libraries.paperlib.PaperLib
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Biome
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.valueOrThrow
import kotlin.script.experimental.host.toScriptSource
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

class Galactifun2 : AbstractAddon() {

    lateinit var manager: PaperCommandManager
        private set

    override fun onLoad() {
        Bukkit.spigot().config["world-settings.default.verbose"] = false
    }

    override fun onEnable() {
        instance = this

        var shouldDisable = false
        if (!PaperLib.isPaper()) {
            log(Level.SEVERE, "Galactifun2 only supports Paper and its forks (e.x. Airplane and Purpur)")
            log(Level.SEVERE, "Please use Paper or a fork of Paper")
            shouldDisable = true
        }
        if (Slimefun.getMinecraftVersion().isBefore(MinecraftVersion.MINECRAFT_1_19)) {
            log(Level.SEVERE, "Galactifun2 only supports Minecraft 1.19 and above")
            log(Level.SEVERE, "Please use Minecraft 1.19 or above")
            shouldDisable = true
        }
        if (Bukkit.getPluginManager().isPluginEnabled("ClayTech")) {
            log(Level.SEVERE, "Galactifun2 will not work properly with ClayTech")
            log(Level.SEVERE, "Please disable ClayTech")
            shouldDisable = true
        }
        if (Bukkit.getPluginManager().isPluginEnabled("Galactifun")) {
            log(Level.SEVERE, "Galactifun2 will not work properly with Galactifun")
            log(Level.SEVERE, "Please remove Galactifun")
            shouldDisable = true
        }

        if (shouldDisable) {
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }

        Metrics(this, 11613)

        manager = PaperCommandManager(this)
        manager.enableUnstableAPI("help")
        manager.commandCompletions.registerAsyncCompletion("worlds") { _ ->
            PlanetManager.allPlanetaryWorlds.map { it.name }.sorted()
        }
        manager.commandCompletions.registerAsyncCompletion("planets") { _ ->
            PlanetManager.allPlanets.map { it.name }.sorted()
        }
        manager.commandContexts.registerContext(PlanetaryWorld::class.java) { context ->
            val arg = context.popFirstArg()
            PlanetManager.allPlanetaryWorlds.find { it.name == arg }
        }
        manager.commandContexts.registerContext(PlanetaryObject::class.java) { context ->
            val arg = context.popFirstArg()
            PlanetManager.allPlanets.find { it.name == arg }
        }
        manager.registerCommand(Gf2Command)

        BaseUniverse.init()

        val scriptsFolder = dataFolder.resolve("planets")
        if (!scriptsFolder.exists()) {
            scriptsFolder.mkdirs()
        }
        for (script in scriptsFolder.listFiles()!!) {
            if (script.isFile && script.name.endsWith(".planet.kts")) {
                log("Loading planet script: ${script.name}")
                val result = evalScript(script.toScriptSource())
                for (diagnostic in result.reports) {
                    log(
                        when (diagnostic.severity) {
                            ScriptDiagnostic.Severity.ERROR, ScriptDiagnostic.Severity.FATAL -> Level.SEVERE
                            ScriptDiagnostic.Severity.WARNING -> Level.WARNING
                            ScriptDiagnostic.Severity.INFO -> Level.INFO
                            ScriptDiagnostic.Severity.DEBUG -> Level.FINE
                        },
                        diagnostic.message
                    )
                }
                val returnValue = result.valueOrThrow().returnValue
                if (returnValue is ResultValue.Error) {
                    throw returnValue.error
                } else {
                    for (planet in (returnValue.scriptInstance as PlanetScript).toRegister) {
                        if (planet is PlanetaryWorld) {
                            planet.register()
                        }
                        log("Registered planet: ${planet.name}")
                    }
                }
            }
        }

        runOnNextTick {
            log(
                Level.INFO,
                "################# Galactifun2 $pluginVersion #################",
                "",
                "Galactifun2 is open source, you can contribute or report bugs at $bugTrackerURL",
                "Join the Slimefun Addon Community Discord: discord.gg/SqD3gg5SAU",
                "",
                "###################################################"
            )
        }

        doTestingStuff()
    }

    override fun onDisable() {
        instance = null
    }

    override fun getJavaPlugin(): JavaPlugin = this

    override fun getBugTrackerURL(): String = "https://github.com/Slimefun-Addon-Community/Galactifun2/issues"

    private fun doTestingStuff() {
        val script = object : PlanetScript() {}

        script.planet {
            name = "Mars"
            item = Material.RED_CONCRETE
            orbiting = BaseUniverse.solarSystem
            orbit {
                distance = 1.52.au
                yearLength = 1.88.years
            }
            dayCycle = (1.days + 0.65.hours).long

            atmosphere {
                pressure = 0.006

                composition {
                    95 percent Gas.CARBON_DIOXIDE
                    2.8 percent Gas.NITROGEN
                    2 percent Gas.ARGON
                    0.2 percent Gas.OXYGEN
                }
            }

            world {
                generator(SimplePerlin) {
                    configNoise {
                        scale = 0.01
                        frequency = 0.5
                        amplitude = 0.1
                        smoothen = true
                    }

                    averageHeight = 50
                    maxDeviation = 20

                    blocks {
                        Material.RED_SAND top 2

                        fillInRestWith(random {
                            Material.RED_SANDSTONE withWeight 0.8f
                            Material.IRON_ORE withWeight 0.2f
                        })
                    }

                    singleBiome(Biome.DESERT)
                }
            }
        }

        for (planet in script.toRegister) {
            if (planet is PlanetaryWorld) {
                planet.register()
            }
            log("Registered planet: ${planet.name}")
        }
    }
}

private var instance: Galactifun2? = null

val pluginInstance: Galactifun2
    get() = checkNotNull(instance) { "Plugin is not enabled" }

fun JavaPlugin.log(level: Level, vararg messages: String) {
    for (message in messages) {
        logger.log(level, message)
    }
}

fun JavaPlugin.log(vararg messages: String) = log(Level.INFO, *messages)

fun JavaPlugin.runOnNextTick(runnable: Runnable) {
    server.scheduler.runTask(this, runnable)
}

fun JavaPlugin.runTaskRepeat(period: Long, delay: Long = 0, runnable: Runnable) {
    server.scheduler.runTaskTimer(this, runnable, delay, period)
}