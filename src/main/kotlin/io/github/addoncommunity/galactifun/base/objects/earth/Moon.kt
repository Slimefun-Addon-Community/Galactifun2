package io.github.addoncommunity.galactifun.base.objects.earth

import io.github.addoncommunity.galactifun.api.objects.planet.AlienWorld
import io.github.addoncommunity.galactifun.api.objects.planet.gen.WorldGenerator
import io.github.addoncommunity.galactifun.api.objects.properties.DayCycle
import io.github.addoncommunity.galactifun.api.objects.properties.Orbit
import io.github.addoncommunity.galactifun.api.objects.properties.atmosphere.Atmosphere
import io.github.addoncommunity.galactifun.base.BaseUniverse
import io.github.addoncommunity.galactifun.util.units.Distance.Companion.kilometers
import io.github.addoncommunity.galactifun.util.units.Mass.Companion.kilograms
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

class Moon : AlienWorld("Moon", ItemStack(Material.END_STONE)) {
    override val atmosphere = Atmosphere.NONE
    override val dayCycle = DayCycle(29.days + 12.hours)
    override val orbiting = BaseUniverse.earth
    override val orbit = Orbit(382500.kilometers, 27.days)
    override val mass = 7.346e22.kilograms
    override val radius = 1737.4.kilometers
    override val generator: WorldGenerator = MoonGenerator()
}