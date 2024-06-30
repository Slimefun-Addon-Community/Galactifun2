package io.github.addoncommunity.galactifun.api.betteritem

import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler
import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class ItemHandler(val handler: KClass<out ItemHandler>)
