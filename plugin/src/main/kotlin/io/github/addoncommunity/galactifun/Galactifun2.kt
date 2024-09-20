package io.github.addoncommunity.galactifun

import co.aikar.commands.PaperCommandManager
import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.addoncommunity.galactifun.api.objects.PlanetaryObject
import io.github.addoncommunity.galactifun.api.objects.planet.PlanetaryWorld
import io.github.addoncommunity.galactifun.api.objects.properties.atmosphere.Gas
import io.github.addoncommunity.galactifun.api.objects.properties.atmosphere.composition
import io.github.addoncommunity.galactifun.impl.BaseUniverse
import io.github.addoncommunity.galactifun.impl.GalactifunItems
import io.github.addoncommunity.galactifun.impl.Gf2Command
import io.github.addoncommunity.galactifun.impl.managers.PlanetManager
import io.github.addoncommunity.galactifun.scripting.PlanetScript
import io.github.addoncommunity.galactifun.scripting.dsl.*
import io.github.addoncommunity.galactifun.scripting.dsl.gen.*
import io.github.addoncommunity.galactifun.scripting.evalScript
import io.github.addoncommunity.galactifun.serial.BlockVectorSerializer
import io.github.addoncommunity.galactifun.units.Angle.Companion.degrees
import io.github.addoncommunity.galactifun.units.Distance.Companion.au
import io.github.addoncommunity.galactifun.units.Distance.Companion.kilometers
import io.github.addoncommunity.galactifun.units.Mass.Companion.kilograms
import io.github.addoncommunity.galactifun.util.bukkit.plus
import io.github.addoncommunity.galactifun.util.general.log
import io.github.seggan.sf4k.AbstractAddon
import io.github.seggan.sf4k.serial.serializers.CustomSerializerRegistry
import io.github.thebusybiscuit.slimefun4.api.MinecraftVersion
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun
import io.github.thebusybiscuit.slimefun4.libraries.paperlib.PaperLib
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import net.kyori.adventure.text.format.NamedTextColor
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Biome
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level
import kotlin.coroutines.CoroutineContext
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.valueOrThrow
import kotlin.script.experimental.host.toScriptSource
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

open class Galactifun2 : AbstractAddon() {

    private lateinit var manager: PaperCommandManager
    lateinit var launchMessages: List<String> private set

    var isTest = classLoader.javaClass.packageName.startsWith("be.seeseemelk.mockbukkit")

    override suspend fun onLoadAsync() {
        if (!isTest) {
            Bukkit.spigot().config["world-settings.default.verbose"] = false
        }
        CustomSerializerRegistry.register(BlockVectorSerializer)
    }

    override suspend fun onEnableAsync() {
        instance = this

        var shouldDisable = false
        if (!PaperLib.isPaper() && !isTest) {
            logger.log(Level.SEVERE, "Galactifun2 only supports Paper and its forks (e.x. Airplane or Purpur)")
            logger.log(Level.SEVERE, "Please use Paper or a fork of Paper")
            shouldDisable = true
        }
        if (Slimefun.getMinecraftVersion().isBefore(MinecraftVersion.MINECRAFT_1_19)) {
            logger.log(Level.SEVERE, "Galactifun2 only supports Minecraft 1.19 and above")
            logger.log(Level.SEVERE, "Please use Minecraft 1.19 or above")
            shouldDisable = true
        }
        if (Bukkit.getPluginManager().isPluginEnabled("ClayTech")) {
            logger.log(Level.SEVERE, "Galactifun2 will not work properly with ClayTech")
            logger.log(Level.SEVERE, "Please disable ClayTech")
            shouldDisable = true
        }
        if (Bukkit.getPluginManager().isPluginEnabled("Galactifun")) {
            logger.log(Level.SEVERE, "Galactifun2 will not work properly with Galactifun")
            logger.log(Level.SEVERE, "Please remove Galactifun")
            shouldDisable = true
        }

        if (shouldDisable) {
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }

        if (!isTest) {
            Metrics(this, 11613)
        }

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

        launchMessages = config.getStringList("rockets.launch-msgs")

        BaseUniverse.init()

        val scriptsFolder = dataFolder.resolve("planets")
        if (!scriptsFolder.exists()) {
            scriptsFolder.mkdirs()
        }
        for (script in scriptsFolder.listFiles()!!) {
            if (script.isFile && script.name.endsWith(".planet.kts")) {
                logger.log("Loading planet script: ${script.name}")
                val result = evalScript(script.toScriptSource())
                for (diagnostic in result.reports) {
                    logger.log(
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
                        logger.log("Registered planet: ${planet.name}")
                    }
                }
            }
        }

        GalactifunItems // Trigger static init

        launch {
            Bukkit.getConsoleSender().sendMessage(
                NamedTextColor.GREEN + """################# Galactifun2 $pluginVersion #################
                
                Galactifun2 is open source, you can contribute or report bugs at $bugTrackerURL
                Join the Slimefun Addon Community Discord: discord.gg/SqD3gg5SAU
                
                ###################################################""".trimIndent()
            )
        }

        doTestingStuff()
    }

    override fun getJavaPlugin(): JavaPlugin = this

    override fun getBugTrackerURL(): String = "https://github.com/Slimefun-Addon-Community/Galactifun2/issues"

    private fun doTestingStuff() {
        val script = object : PlanetScript() {}

        script.planet {
            name = "Mars"
            item = Material.RED_CONCRETE
            orbit {
                parent = BaseUniverse.sun
                semimajorAxis = 1.524.au
                eccentricity = 0.0934
                argumentOfPeriapsis = 336.04.degrees
                timeOfPeriapsis = Instant.parse("2022-05-21T15:00:00Z")
            }
            mass = 6.417e23.kilograms
            radius = 3389.5.kilometers
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
                            Material.RED_SANDSTONE withWeight 80f
                            Material.IRON_ORE withWeight 20f
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
            logger.log("Registered planet: ${planet.name}")
        }
    }
}

private var instance: Galactifun2? = null

val pluginInstance: Galactifun2
    get() = checkNotNull(instance) { "Plugin is not enabled" }

fun JavaPlugin.launchAsync(
    context: CoroutineContext = asyncDispatcher,
    block: suspend CoroutineScope.() -> Unit
): Job = pluginInstance.launch {
    withContext(context, block)
}