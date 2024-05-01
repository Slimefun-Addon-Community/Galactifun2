package io.github.addoncommunity.galactifun.api.objects.planet

import io.github.addoncommunity.galactifun.api.objects.PlanetaryObject
import io.github.addoncommunity.galactifun.impl.managers.PlanetManager
import org.bukkit.World
import org.bukkit.inventory.ItemStack
import org.metamechanists.displaymodellib.models.ModelBuilder


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

    lateinit var world: World
        private set

    protected open val sun: ModelBuilder? = null
    protected open val moon: ModelBuilder? = null

    fun register() {
        orbit.parent.addOrbiter(this)
        world = loadWorld()
        PlanetManager.register(this)
    }

    abstract fun loadWorld(): World
}