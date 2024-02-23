package io.github.addoncommunity.test.serial

import io.github.addoncommunity.galactifun.serial.ListBlockStorageDataType
import io.github.addoncommunity.galactifun.serial.MapBlockStorageDataType
import io.github.addoncommunity.galactifun.serial.SetBlockStorageDataType
import io.github.addoncommunity.test.CommonTest
import io.github.seggan.kfun.serial.BlockStorageDataType
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import kotlin.test.Test

class SerialTest : CommonTest() {

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
}