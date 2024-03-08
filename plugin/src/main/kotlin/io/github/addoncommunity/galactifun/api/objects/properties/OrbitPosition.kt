package io.github.addoncommunity.galactifun.api.objects.properties

import io.github.addoncommunity.galactifun.impl.managers.PlanetManager
import org.bukkit.Location
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType

data class OrbitPosition(val x: Int, val z: Int) {

    companion object {

        const val ORBIT_SIZE = 535714

        fun fromLocation(x: Int, z: Int) = OrbitPosition(x.floorDiv(ORBIT_SIZE), z.floorDiv(ORBIT_SIZE))

        fun fromLocation(location: Location) = fromLocation(location.blockX, location.blockZ)
    }

    val centerLocation: Location
        get() = Location(
            PlanetManager.spaceWorld,
            x * ORBIT_SIZE + ORBIT_SIZE / 2.0,
            0.0,
            z * ORBIT_SIZE + ORBIT_SIZE / 2.0
        )

    object DataType : PersistentDataType<IntArray, OrbitPosition> {
        override fun getPrimitiveType() = IntArray::class.java
        override fun getComplexType() = OrbitPosition::class.java

        override fun fromPrimitive(primitive: IntArray, context: PersistentDataAdapterContext): OrbitPosition {
            return OrbitPosition(primitive[0], primitive[1])
        }
        override fun toPrimitive(complex: OrbitPosition, context: PersistentDataAdapterContext): IntArray {
            return intArrayOf(complex.x, complex.z)
        }
    }
}
