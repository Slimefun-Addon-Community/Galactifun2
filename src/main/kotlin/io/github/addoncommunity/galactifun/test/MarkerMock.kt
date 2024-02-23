package io.github.addoncommunity.galactifun.test

import be.seeseemelk.mockbukkit.MockBukkit
import be.seeseemelk.mockbukkit.entity.EntityMock
import org.bukkit.entity.EntityType
import org.bukkit.entity.Marker
import java.util.*

class MarkerMock : EntityMock(MockBukkit.getMock()!!, UUID.randomUUID()), Marker {
    override fun getType(): EntityType {
        return EntityType.MARKER
    }
}