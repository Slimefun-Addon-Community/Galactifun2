package io.github.addoncommunity.galactifun.core

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import io.github.addoncommunity.galactifun.api.objects.PlanetaryObject
import io.github.addoncommunity.galactifun.api.objects.planet.PlanetaryWorld
import io.github.addoncommunity.galactifun.core.managers.PlanetManager
import io.github.addoncommunity.galactifun.util.galactifunTeleport
import io.github.seggan.kfun.location.plusAssign
import kotlinx.datetime.Clock
import org.bukkit.Location
import org.bukkit.entity.Player

@Suppress("unused")
@CommandAlias("gf2")
object Gf2Command : BaseCommand() {

    @Subcommand("planet")
    @CommandCompletion("@worlds")
    @CommandPermission(Permissions.TELEPORT)
    @Description("Teleport to a planet")
    fun tpPlanet(player: Player, planet: PlanetaryWorld, @Optional location: Location?) {
        val loc = location ?: planet.world.spawnLocation
        loc.world = planet.world
        player.galactifunTeleport(loc)
    }

    @Subcommand("orbit")
    @CommandCompletion("@planets")
    @CommandPermission(Permissions.TELEPORT)
    @Description("Teleport to orbit")
    fun tpSpace(player: Player, planet: PlanetaryObject, @Optional offset: Location?) {
        val location = planet.orbitPosition.centerLocation
        if (offset != null) {
            location += offset
        }
        player.galactifunTeleport(location)
    }

    @Subcommand("distance")
    @CommandCompletion("@planets")
    @Description("Get distance to a planet")
    fun distance(player: Player, planet: PlanetaryObject) {
        val start = PlanetManager.getByWorld(player.world) ?: return
        val distance = start.getDeltaVForTransferTo(planet, Clock.System.now())
        player.sendMessage("The delta-v required to transfer to ${planet.name} is $distance m/s")
    }
}