package io.github.addoncommunity.galactifun.serial

import be.seeseemelk.mockbukkit.MockBukkit
import io.github.seggan.kfun.serial.BlockStorageDataType
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.BeforeAll
import kotlin.test.Test

class SerialTest {

    @Test
    fun testList() {
        val serializer = ListBlockStorageDataType(BlockStorageDataType.INT)
        val list = listOf(1, 2, 3, 4, 5)
        val serialized = serializer.serialize(list)
        val deserialized = serializer.deserialize(serialized)
        deserialized.shouldNotBeNull() shouldBeEqual list
    }

    @Test
    fun testMap() {
        val serializer = MapBlockStorageDataType(BlockStorageDataType.INT, BlockStorageDataType.STRING)
        val map = mapOf(1 to "one", 2 to "two", 3 to "three")
        val serialized = serializer.serialize(map)
        val deserialized = serializer.deserialize(serialized)
        deserialized.shouldNotBeNull() shouldBeEqual map
    }

    @Test
    fun testSet() {
        val serializer = SetBlockStorageDataType(BlockStorageDataType.INT)
        val set = setOf(1, 2, 3, 4, 5)
        val serialized = serializer.serialize(set)
        val deserialized = serializer.deserialize(serialized)
        deserialized.shouldNotBeNull() shouldBeEqual set
    }

    companion object {
        @JvmStatic
        @BeforeAll
        fun setUp() {
            MockBukkit.mock()
        }
    }
}