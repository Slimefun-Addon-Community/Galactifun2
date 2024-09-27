package io.github.addoncommunity.galactifun.impl.managers

import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.addoncommunity.galactifun.Galactifun2
import io.github.addoncommunity.galactifun.api.objects.PlanetaryObject
import io.github.addoncommunity.galactifun.api.objects.planet.PlanetaryWorld
import io.github.addoncommunity.galactifun.api.objects.properties.OrbitPosition
import io.github.addoncommunity.galactifun.api.objects.properties.atmosphere.Atmosphere
import io.github.addoncommunity.galactifun.impl.Permissions
import io.github.addoncommunity.galactifun.impl.space.SpaceGenerator
import io.github.addoncommunity.galactifun.util.bukkit.getPdc
import io.github.addoncommunity.galactifun.util.bukkit.key
import io.github.addoncommunity.galactifun.util.bukkit.setPdc
import io.papermc.paper.event.entity.EntityMoveEvent
import org.bukkit.*
import org.bukkit.configuration.file.YamlConfiguration
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

    private val orbits: MutableMap<String, OrbitPosition>
    private val orbitsKey = "orbits".key()

    private val config = YamlConfiguration()

    init {
        val configFile = Galactifun2.dataFolder.resolve("worlds.yml")
        if (configFile.exists()) {
            config.load(configFile)
        }

        Galactifun2.launch {
            config.options().copyDefaults(true)
            config.save(configFile)
        }

        spaceWorld = WorldCreator("galactifun_space")
            .generator(SpaceGenerator)
            .environment(World.Environment.THE_END)
            .createWorld() ?: error("Could not create world galactifun_space")

        Atmosphere.NONE.applyEffects(spaceWorld)
        spaceWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false)

        orbits = spaceWorld.getPdc(orbitsKey) ?: mutableMapOf()

        Bukkit.getPluginManager().registerEvents(this, Galactifun2)
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
            spaceWorld.setPdc(orbitsKey, orbits)
        }

        if (planet is PlanetaryWorld) {
            planetaryWorlds[planet.world] = planet
        }
    }

    fun getOrbit(planet: PlanetaryObject): OrbitPosition {
        return orbits[planet.name] ?: error("Orbit not found for planet ${planet.name}")
    }

    fun getOrbiting(location: Location): PlanetaryObject? {
        val position = OrbitPosition.fromLocation(location)
        return allPlanets.find { it.orbitPosition == position }
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