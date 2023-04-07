package io.github.addoncommunity.galactifun.api.objects.planet

import io.github.addoncommunity.galactifun.api.objects.properties.atmosphere.Atmosphere
import io.github.addoncommunity.galactifun.core.managers.WorldManager
import org.bukkit.World
import org.bukkit.inventory.ItemStack


/**
 * A planetary object that has a world. This is for adapting vanilla or plugin worlds to the Galactifun API.
 * If you want to create a custom world, use [AlienWorld]
 *
 * @author Seggan
 */
abstract class PlanetaryWorld(
    name: String,
    baseItem: ItemStack
) : PlanetaryObject(name, baseItem) {

    abstract val atmosphere: Atmosphere

    lateinit var world: World
        private set

    fun register() {
        world = loadWorld()
        WorldManager.registerWorld(this)
    }

    abstract fun loadWorld(): World
}