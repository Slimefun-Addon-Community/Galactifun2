package io.github.addoncommunity.galactifun.api.rockets

import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition

data class RocketInfo(
    val commandComputer: BlockPosition,
    var blocks: Set<BlockPosition>
)
