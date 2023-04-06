package io.github.addoncommunity.galactifun.api.objects.properties.atmosphere

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils

enum class Gas(texture: String?) {
    OXYGEN("5b3ad76aadb80ecf4b4cdbe76b8704b0f2dc090b49b65c36d87ed879f1065ef2"),
    NITROGEN("5b3ad76aadb80ecf4b4cdbe76b8704b0f2dc090b49b65c36d87ed879f1065ef2"),
    CARBON_DIOXIDE("5b3ad76aadb80ecf4b4cdbe76b8704b0f2dc090b49b65c36d87ed879f1065ef2"),
    WATER("5b3ad76aadb80ecf4b4cdbe76b8704b0f2dc090b49b65c36d87ed879f1065ef2"),
    HELIUM("93dfa904fe3d0306666a573c22eec1dd0a8051e32a38ea2d19c4b5867e232a49"),
    ARGON("ea005531b6167a86fb09d6c0f3db60f2650162d0656c2908d07b377111d8f2a2"),
    METHANE("ea005531b6167a86fb09d6c0f3db60f2650162d0656c2908d07b377111d8f2a2"),
    HYDROCARBONS("725691372e0734bfb57bb03690490661a83f053a3488860df3436ce1caa24d11"),
    HYDROGEN("725691372e0734bfb57bb03690490661a83f053a3488860df3436ce1caa24d11"),
    SULFUR("c7a1ece691ad28d17bbbcecb22270c85e1c9581485806264c676de67c272e2d0"),
    AMMONIA("c7a1ece691ad28d17bbbcecb22270c85e1c9581485806264c676de67c272e2d0"),
    OTHER(null);

    val item: SlimefunItemStack?

    init {
        item = texture?.let {
            SlimefunItemStack(
                "ATMOSPHERIC_GAS_$name",
                SlimefunUtils.getCustomHead(texture),
                "&f${ChatUtils.humanize(name)} Gas Canister",
                "",
                "&f&oTexture by Sefiraat"
            )
        }
    }

    override fun toString(): String = ChatUtils.humanize(name)
}