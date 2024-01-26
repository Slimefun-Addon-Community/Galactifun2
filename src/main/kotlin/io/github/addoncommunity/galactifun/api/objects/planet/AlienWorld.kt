package io.github.addoncommunity.galactifun.api.objects.planet

import io.github.addoncommunity.galactifun.api.objects.planet.gen.WorldGenerator
import io.github.addoncommunity.galactifun.log
import io.github.addoncommunity.galactifun.pluginInstance
import io.github.addoncommunity.galactifun.util.set
import org.bukkit.GameRule
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.WorldCreator
import org.bukkit.inventory.ItemStack
import java.util.*

abstract class AlienWorld(name: String, baseItem: ItemStack) : PlanetaryWorld(name, baseItem) {

    protected abstract val generator: WorldGenerator
    protected open val spawnVanillaMobs = false

    private val blockMappings = EnumMap<Material, ItemStack>(Material::class.java)

    override fun loadWorld(): World {
        pluginInstance.log("Loading world $name")

        val world = WorldCreator("world_galactifun_$id")
            .generator(generator)
            .environment(atmosphere.environment)
            .createWorld() ?: error("Could not create world world_galactifun_$id")

        if (world.environment == World.Environment.THE_END) {
            // Prevents ender dragon spawn using portal, surrounds portal with bedrock
            world[0, 0, 0] = Material.END_PORTAL
            world[0, 1, 0] = Material.BEDROCK
            world[1, 0, 0] = Material.BEDROCK
            world[-1, 0, 0] = Material.BEDROCK
            world[0, 0, 1] = Material.BEDROCK
            world[0, 0, -1] = Material.BEDROCK
        }

        atmosphere.applyEffects(world)
        dayCycle.applyEffects(world)

        world.setGameRule(GameRule.DO_MOB_SPAWNING, spawnVanillaMobs)

        return world
    }

    fun addBlockMapping(material: Material, item: ItemStack) {
        blockMappings[material] = item
    }

    fun getBlockMapping(material: Material): ItemStack? {
        return blockMappings[material]
    }
}