package io.github.addoncommunity.galactifun.util.worldgen

import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.World

data class ChunkPosition(val x: Int, val y: Int) {

    val worldX = x * 16
    val worldY = y * 16

    fun toLocation(world: World) = Location(world, worldX.toDouble(), 0.0, worldY.toDouble())
}

val Chunk.position: ChunkPosition
    get() = ChunkPosition(x, z)
