package io.github.addoncommunity.galactifun.api.blocks

import io.github.addoncommunity.galactifun.units.Mass
import io.github.addoncommunity.galactifun.units.Mass.Companion.kilograms
import io.github.addoncommunity.galactifun.units.Mass.Companion.tons
import io.github.addoncommunity.galactifun.util.bukkit.contains
import io.github.thebusybiscuit.slimefun4.utils.tags.SlimefunTag
import me.mrCookieSlime.Slimefun.api.BlockStorage
import org.bukkit.Material
import org.bukkit.block.Block

private val INFINITE_MASS = Double.POSITIVE_INFINITY.kilograms

val Block.dryMass: Mass
    get() {
        val sfi = BlockStorage.check(this)
        return if (sfi is CustomMass) {
            sfi.getMass(this)
        } else {
            this.type.mass
        }
    }

val Block.wetMass: Mass
    get() {
        val sfi = BlockStorage.check(this)
        return if (sfi is CustomMass) {
            sfi.getWetMass(this)
        } else {
            this.type.mass
        }
    }

private val Material.mass: Mass
    get() = when (this) {
        Material.AIR, Material.CAVE_AIR, Material.VOID_AIR -> Mass.ZERO
        in SlimefunTag.UNBREAKABLE_MATERIALS -> INFINITE_MASS
        in SlimefunTag.FLUID_SENSITIVE_MATERIALS -> 100.kilograms
        in SlimefunTag.SENSITIVE_MATERIALS -> 100.kilograms
        Material.IRON_BLOCK -> 7.9.tons
        Material.GOLD_BLOCK -> 19.3.tons
        Material.DIAMOND_BLOCK -> 3.5.tons
        Material.EMERALD_BLOCK -> 2.8.tons
        Material.QUARTZ_BLOCK -> 2.6.tons
        Material.NETHERITE_BLOCK -> 19.3.tons // tungsten used as reference
        Material.GRANITE -> 2.6.tons
        Material.DIORITE -> 2.8.tons
        Material.ANDESITE -> 2.4.tons
        Material.CALCITE -> 2.7.tons
        Material.STONE, Material.COBBLESTONE -> 2.7.tons
        in SlimefunTag.WOODEN_DOORS,
        in SlimefunTag.WOODEN_SLABS,
        in SlimefunTag.WOODEN_STAIRS,
        in SlimefunTag.WOODEN_TRAPDOORS,
        in SlimefunTag.WOODEN_FENCES,
        in SlimefunTag.WOODEN_PRESSURE_PLATES,
        in SlimefunTag.WOODEN_BUTTONS -> 0.3.tons

        in SlimefunTag.LOGS, in SlimefunTag.PLANKS -> 0.6.tons
        else -> 2.5.tons
    }