package io.github.addoncommunity.galactifun.core

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import io.github.addoncommunity.galactifun.api.objects.planet.PlanetaryWorld
import io.github.addoncommunity.galactifun.core.managers.WorldManager
import org.bukkit.Location
import org.bukkit.entity.Player

@Suppress("unused")
@CommandAlias("gf2")
object Gf2Command : BaseCommand() {

    @Subcommand("planet")
    @CommandCompletion("@planets")
    @CommandPermission("galactifun.admin")
    @Description("Teleport to a planet")
    fun tpPlanet(player: Player, planet: PlanetaryWorld, @Optional location: Location?) {
        val loc = location ?: planet.world.spawnLocation
        loc.world = planet.world
        player.teleport(loc)
    }

    @Subcommand("space")
    @CommandPermission("galactifun.admin")
    @Description("Teleport to space")
    fun tpSpace(player: Player, @Optional location: Location?) {
        val loc = location ?: WorldManager.spaceWorld.spawnLocation
        loc.world = WorldManager.spaceWorld
        player.teleport(loc)
    }
}