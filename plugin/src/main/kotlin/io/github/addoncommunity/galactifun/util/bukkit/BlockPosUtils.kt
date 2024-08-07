package io.github.addoncommunity.galactifun.util.bukkit

import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition
import org.bukkit.block.Block
import org.bukkit.block.BlockFace

val BlockPosition.location get() = this.toLocation()

fun BlockPosition.getFace(face: BlockFace): BlockPosition {
    return BlockPosition(this.world, this.x + face.modX, this.y + face.modY, this.z + face.modZ)
}

val adjacentFaces = arrayOf(
    BlockFace.UP,
    BlockFace.DOWN,
    BlockFace.NORTH,
    BlockFace.SOUTH,
    BlockFace.EAST,
    BlockFace.WEST
)

inline fun BlockPosition.floodSearch(
    searchLimit: Int = 1024,
    ignoreAir: Boolean = true,
    predicate: Block.(Block) -> Boolean
): FloodSearchResult {
    var toSearch = mutableSetOf(this)
    var toSearchNext = mutableSetOf<BlockPosition>()
    val found = mutableSetOf<BlockPosition>()
    while (true) {
        if (toSearch.isEmpty()) {
            return FloodSearchResult(found, false)
        }
        for (pos in toSearch) {
            if (pos in found) continue
            found.add(pos)
            if (found.size >= searchLimit) {
                return FloodSearchResult(found, true)
            }
            val block = pos.block
            for (face in adjacentFaces) {
                val next = pos.getFace(face)
                if (next !in found && next !in toSearch && next !in toSearchNext) {
                    try {
                        val nextBlock = next.block
                        if (ignoreAir && nextBlock.type.isAir) continue
                        if (block.predicate(nextBlock)) {
                            toSearchNext.add(next)
                        }
                    } catch (e: IllegalArgumentException) {
                        // Invalid block position, ignore
                    }
                }
            }
        }
        toSearch = toSearchNext
        toSearchNext = mutableSetOf()
    }
}

data class FloodSearchResult(val found: Set<BlockPosition>, val exceededMax: Boolean)

tailrec fun BlockPosition.isHighest(): Boolean {
    val world = this.world
    val next = this.getFace(BlockFace.UP)
    if (next.y >= world.maxHeight) return true
    return if (next.block.type.isAir) next.isHighest() else false
}