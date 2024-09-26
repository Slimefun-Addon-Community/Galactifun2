package io.github.addoncommunity.galactifun.util.bukkit

import io.github.addoncommunity.galactifun.Galactifun2
import org.bukkit.metadata.MetadataValueAdapter

object DummyMetadataValue : MetadataValueAdapter(Galactifun2) {
    override fun value() = Unit
    override fun invalidate() {}
}