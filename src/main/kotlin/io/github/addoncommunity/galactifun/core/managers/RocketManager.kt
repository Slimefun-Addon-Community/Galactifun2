package io.github.addoncommunity.galactifun.core.managers

import io.github.addoncommunity.galactifun.api.rockets.RocketInfo

object RocketManager {

    private val _allRockets = mutableSetOf<RocketInfo>()
    val allRockets: Set<RocketInfo> get() = _allRockets

    fun register(rocket: RocketInfo) {
        _allRockets.add(rocket)
    }
}