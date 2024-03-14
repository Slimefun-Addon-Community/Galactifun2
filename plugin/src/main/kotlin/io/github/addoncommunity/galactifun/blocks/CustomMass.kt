package io.github.addoncommunity.galactifun.blocks

import io.github.addoncommunity.galactifun.units.Mass
import org.bukkit.block.Block

interface CustomMass {
    fun getMass(block: Block): Mass

    fun getWetMass(block: Block): Mass = getMass(block)
}