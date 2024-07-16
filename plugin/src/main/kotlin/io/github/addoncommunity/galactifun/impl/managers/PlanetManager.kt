package io.github.addoncommunity.galactifun.impl.managers

import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.addoncommunity.galactifun.api.objects.PlanetaryObject
import io.github.addoncommunity.galactifun.api.objects.planet.PlanetaryWorld
import io.github.addoncommunity.galactifun.api.objects.properties.OrbitPosition
import io.github.addoncommunity.galactifun.api.objects.properties.atmosphere.Atmosphere
import io.github.addoncommunity.galactifun.impl.Permissions
import io.github.addoncommunity.galactifun.impl.space.SpaceGenerator
import io.github.addoncommunity.galactifun.pluginInstance
import io.github.addoncommunity.galactifun.util.bukkit.key
import io.github.addoncommunity.galactifun.util.bukkit.locationZero
import io.github.addoncommunity.galactifun.util.bukkit.nearbyEntitiesByType
import io.github.addoncommunity.galactifun.util.bukkit.summon
import io.github.seggan.sf4k.serial.pdc.get
import io.github.seggan.sf4k.serial.pdc.set
import io.papermc.paper.event.entity.EntityMoveEvent
import org.bukkit.Bukkit
import org.bukkit.GameRule
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Entity
import org.bukkit.entity.Marker
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerTeleportEvent
import java.util.concurrent.ConcurrentHashMap

object PlanetManager : Listener {

    private val planetaryWorlds = ConcurrentHashMap<World, PlanetaryWorld>()
    val allPlanetaryWorlds: Set<PlanetaryWorld>
        get() = planetaryWorlds.values.toSet()

    private val planets = ConcurrentHashMap.newKeySet<PlanetaryObject>()
    val allPlanets: Set<PlanetaryObject>
        get() = planets

    val spaceWorld: World
    private val spaceWorldMarker: Entity

    private val orbits: MutableMap<String, OrbitPosition>
    private val orbitsKey = "orbits".key()

    private val config = YamlConfiguration()

    init {
        val configFile = pluginInstance.dataFolder.resolve("worlds.yml")
        if (configFile.exists()) {
            config.load(configFile)
        }

        pluginInstance.launch {
            config.options().copyDefaults(true)
            config.save(configFile)
        }

        spaceWorld = WorldCreator("galactifun_space")
            .generator(SpaceGenerator)
            .environment(World.Environment.THE_END)
            .createWorld() ?: error("Could not create world galactifun_space")

        Atmosphere.NONE.applyEffects(spaceWorld)
        spaceWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false)

        spaceWorldMarker = spaceWorld.nearbyEntitiesByType<Marker>(
            locationZero(spaceWorld),
            0.1
        ).firstOrNull() ?: spaceWorld.summon<Marker>(locationZero(spaceWorld))

        orbits = spaceWorldMarker.persistentDataContainer.get(orbitsKey) ?: mutableMapOf()

        Bukkit.getPluginManager().registerEvents(this, pluginInstance)
    }

    fun register(planet: PlanetaryObject) {
        planets.add(planet)

        if (planet.name !in orbits) {
            val border = spaceWorld.worldBorder.size.toInt()
            val maxOrbits = border / OrbitPosition.ORBIT_SIZE
            val offset = -maxOrbits / 2
            val orbitPos = OrbitPosition(
                orbits.size % maxOrbits + offset,
                orbits.size / maxOrbits + offset
            )
            orbits[planet.name] = orbitPos
            spaceWorldMarker.persistentDataContainer.set(orbitsKey, orbits)
        }

        if (planet is PlanetaryWorld) {
            planetaryWorlds[planet.world] = planet
        }
    }

    fun getOrbit(planet: PlanetaryObject): OrbitPosition {
        return orbits[planet.name] ?: error("Orbit not found for planet ${planet.name}")
    }

    fun getByWorld(world: World): PlanetaryWorld? {
        return planetaryWorlds[world]
    }

    fun getByName(name: String): PlanetaryObject? {
        return planets.firstOrNull { it.name.equals(name, ignoreCase = true) }
    }

    //<editor-fold desc="Orbit barrier handlers" defaultstate="collapsed">
    @EventHandler
    private fun onEntityMoveInSpace(e: EntityMoveEvent) {
        val from = e.from
        if (from.world != spaceWorld) return
        if (OrbitPosition.fromLocation(from) != OrbitPosition.fromLocation(e.to)) {
            e.isCancelled = true
        }
    }

    @EventHandler
    private fun onPlayerMoveInSpace(e: PlayerMoveEvent) {
        val from = e.from
        if (from.world != spaceWorld) return
        if (OrbitPosition.fromLocation(from) != OrbitPosition.fromLocation(e.to)) {
            e.isCancelled = true
        }
    }

    @EventHandler
    private fun onPlayerTeleport(e: PlayerTeleportEvent) {
        val from = e.from
        val to = e.to
        if (from.world != spaceWorld || to.world != spaceWorld) return
        if (
            OrbitPosition.fromLocation(e.from) != OrbitPosition.fromLocation(e.to)
            && !e.player.hasMetadata("galactifun.teleporting")
            && !e.player.hasPermission(Permissions.TELEPORT)
        ) {
            e.isCancelled = true
        }
    }
    //</editor-fold>
}