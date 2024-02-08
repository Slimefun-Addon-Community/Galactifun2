package io.github.addoncommunity.galactifun.core.managers

import io.github.addoncommunity.galactifun.api.objects.planet.PlanetaryWorld
import io.github.addoncommunity.galactifun.api.objects.properties.DayCycle
import io.github.addoncommunity.galactifun.api.objects.properties.atmosphere.Atmosphere
import io.github.addoncommunity.galactifun.core.space.SpaceGenerator
import io.github.addoncommunity.galactifun.pluginInstance
import io.github.addoncommunity.galactifun.runOnNextTick
import io.github.addoncommunity.galactifun.util.set
import org.bukkit.*
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.event.Listener
import java.util.concurrent.ConcurrentHashMap

object WorldManager : Listener {

    private val spaceWorlds = ConcurrentHashMap<World, PlanetaryWorld>()
    val allPlanetaryWorlds: Collection<PlanetaryWorld>
        get() = spaceWorlds.values

    val spaceWorld: World

    private val config: YamlConfiguration
    private val defaultConfig: YamlConfiguration

    init {
        val configFile = pluginInstance.dataFolder.resolve("worlds.yml")
        config = YamlConfiguration()
        defaultConfig = YamlConfiguration()
        config.setDefaults(defaultConfig)

        // Load the config
        if (configFile.exists()) {
            try {
                config.load(configFile)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        spaceWorld = WorldCreator("galactifun_space")
            .generator(SpaceGenerator)
            .environment(World.Environment.THE_END)
            .createWorld() ?: error("Could not create world galactifun_space")

        DayCycle.ETERNAL_NIGHT.applyEffects(spaceWorld)
        Atmosphere.NONE.applyEffects(spaceWorld)
        spaceWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false)

        spaceWorld[0, 0, 0] = Material.END_PORTAL
        spaceWorld[0, 1, 0] = Material.BEDROCK
        spaceWorld[1, 0, 0] = Material.BEDROCK
        spaceWorld[-1, 0, 0] = Material.BEDROCK
        spaceWorld[0, 0, 1] = Material.BEDROCK
        spaceWorld[0, 0, -1] = Material.BEDROCK

        pluginInstance.runOnNextTick {
            config.options().copyDefaults(true)
            config.save(configFile)
        }

        Bukkit.getPluginManager().registerEvents(this, pluginInstance)
    }

    fun registerWorld(world: PlanetaryWorld) {
        spaceWorlds[world.world] = world
    }
}