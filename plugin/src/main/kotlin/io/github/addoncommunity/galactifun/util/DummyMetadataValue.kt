package io.github.addoncommunity.galactifun.util

import io.github.addoncommunity.galactifun.pluginInstance
import org.bukkit.metadata.MetadataValueAdapter

object DummyMetadataValue : MetadataValueAdapter(pluginInstance) {
    override fun value() = Unit
    override fun invalidate() {}
}