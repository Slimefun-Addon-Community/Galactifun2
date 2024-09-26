package io.github.addoncommunity.galactifun.util.bukkit

import io.github.addoncommunity.galactifun.impl.items.CommandComputer
import io.github.addoncommunity.galactifun.util.SlimefunStructure
import io.github.seggan.sf4k.extensions.minus
import io.github.seggan.sf4k.extensions.plus
import io.github.seggan.sf4k.serial.pdc.getData
import io.github.seggan.sf4k.serial.pdc.setData
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.entity.FallingBlock
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.util.BlockVector
import java.io.ByteArrayOutputStream

operator fun PersistentDataContainer.contains(key: NamespacedKey): Boolean = has(key)

inline fun <reified T> PersistentDataHolder.getPdc(key: NamespacedKey): T? {
    return persistentDataContainer.getData<T>(key)
}

inline fun <reified T> PersistentDataHolder.setPdc(key: NamespacedKey, value: T) {
    persistentDataContainer.setData(key, value)
}

fun Block.toEntity(): FallingBlock {
    val display = world.summon<FallingBlock>(location + (UnitVector.X + UnitVector.Z))
    display.blockData = blockData
    display.cancelDrop = true
    display.isInvulnerable = true
    display.isSilent = true
    display.setGravity(false)
    display.setHurtEntities(false)

    val structure = SlimefunStructure()
    structure.fill(location, BlockVector(1, 1, 1), false)
    val bytes = ByteArrayOutputStream().apply(structure::saveToStream).toByteArray()
    display.persistentDataContainer.setData(CommandComputer.SERIALIZED_BLOCK_KEY, bytes)
    this.type = Material.AIR
    return display
}

fun FallingBlock.toBlock(): Boolean {
    val bytes = persistentDataContainer.getData<ByteArray>(CommandComputer.SERIALIZED_BLOCK_KEY) ?: return false
    val block = SlimefunStructure.loadFromStream(bytes.inputStream())
    block.placeDefault(location - (UnitVector.X + UnitVector.Z))
    remove()
    return true
}