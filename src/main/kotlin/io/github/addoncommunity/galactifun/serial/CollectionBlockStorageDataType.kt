package io.github.addoncommunity.galactifun.serial

import io.github.addoncommunity.galactifun.util.Constants
import io.github.seggan.kfun.serial.BlockStorageDataType

abstract class CollectionBlockStorageDataType<E, C : Collection<E>>(
    private val contentSerializer: BlockStorageDataType<E>
) : BlockStorageDataType<C> {

    override fun serialize(value: C): String {
        val sb = StringBuilder()
        sb.append(value.size.toString(Constants.MAX_RADIX))
        sb.append(' ')
        for (element in value) {
            val serialized = contentSerializer.serialize(element)
            sb.append(serialized.length.toString(Constants.MAX_RADIX))
            sb.append(' ')
            sb.append(serialized)
        }
        return sb.toString()
    }

    override fun deserialize(value: String): C? {
        val parts = value.split(' ', limit = 2)
        if (parts.size != 2) return null
        val size = parts[0].toInt(Constants.MAX_RADIX)
        val coll = provideCollection(size)
        var rest = parts[1]
        for (i in 0 until size) {
            val nextParts = rest.split(' ', limit = 2)
            if (nextParts.size != 2) return null
            val length = nextParts[0].toInt(Constants.MAX_RADIX)
            val serialized = nextParts[1].substring(0, length)
            coll.add(contentSerializer.deserialize(serialized) ?: return null)
            rest = nextParts[1].substring(length)
        }
        if (rest.isNotEmpty()) return null

        // You better not break on me
        @Suppress("UNCHECKED_CAST")
        return coll as C
    }

    protected abstract fun provideCollection(size: Int): MutableCollection<E>
}

class ListBlockStorageDataType<E>(contentSerializer: BlockStorageDataType<E>) :
    CollectionBlockStorageDataType<E, List<E>>(contentSerializer) {
    override fun provideCollection(size: Int): MutableList<E> = ArrayList(size)
}

class SetBlockStorageDataType<E>(contentSerializer: BlockStorageDataType<E>) :
    CollectionBlockStorageDataType<E, Set<E>>(contentSerializer) {
    override fun provideCollection(size: Int): MutableSet<E> = LinkedHashSet(size)
}

class MapBlockStorageDataType<K, V>(
    keySerializer: BlockStorageDataType<K>,
    valueSerializer: BlockStorageDataType<V>
) : BlockStorageDataType<Map<K, V>> {

    private val internalSerializer = ListBlockStorageDataType(
        PairBlockStorageDataType(keySerializer, valueSerializer)
    )

    override fun serialize(value: Map<K, V>): String = internalSerializer.serialize(value.toList())

    override fun deserialize(value: String): Map<K, V>? = internalSerializer.deserialize(value)?.toMap()
}