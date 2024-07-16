package io.github.addoncommunity.galactifun.impl.managers

import io.github.addoncommunity.galactifun.api.rockets.RocketInfo
import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition
import kotlinx.coroutines.Job

object RocketManager {

    private val rockets = mutableMapOf<BlockPosition, RocketInfo>()
    val allRockets: Set<RocketInfo> get() = rockets.values.toSet()

    val launches = mutableListOf<Job>()

    fun register(rocket: RocketInfo) {
        rockets.remove(rocket.commandComputer)
        rockets[rocket.commandComputer] = rocket
    }

    fun getInfo(commandComputer: BlockPosition) = rockets[commandComputer]
}