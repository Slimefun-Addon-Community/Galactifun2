package io.github.addoncommunity.galactifun.core.managers

import io.github.addoncommunity.galactifun.api.rockets.RocketInfo
import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition

object RocketManager {

    private val rockets = mutableMapOf<BlockPosition, RocketInfo>()
    val allRockets: Set<RocketInfo> get() = rockets.values.toSet()

    fun register(rocket: RocketInfo) {
        rockets.remove(rocket.commandComputer)
        rockets[rocket.commandComputer] = rocket
    }

    fun getInfo(commandComputer: BlockPosition) = rockets[commandComputer]
}