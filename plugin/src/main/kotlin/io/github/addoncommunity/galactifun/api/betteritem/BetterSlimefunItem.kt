package io.github.addoncommunity.galactifun.api.betteritem

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.core.handlers.*
import io.github.thebusybiscuit.slimefun4.implementation.handlers.SimpleBlockBreakHandler
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker
import org.bukkit.block.Block
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack
import java.lang.invoke.MethodHandle
import java.lang.invoke.MethodHandles
import java.lang.reflect.Method
import kotlin.reflect.KClass
import io.github.thebusybiscuit.slimefun4.api.items.ItemHandler as BaseItemHandler

open class BetterSlimefunItem : SlimefunItem {

    constructor(
        itemGroup: ItemGroup,
        item: SlimefunItemStack,
        recipeType: RecipeType,
        recipe: Array<out ItemStack?>
    ) : super(itemGroup, item, recipeType, recipe)

    constructor(
        itemGroup: ItemGroup,
        item: SlimefunItemStack,
        recipeType: RecipeType,
        recipe: Array<out ItemStack?>,
        recipeOutput: ItemStack
    ) : super(itemGroup, item, recipeType, recipe, recipeOutput)

    constructor(
        itemGroup: ItemGroup,
        item: ItemStack,
        id: String,
        recipeType: RecipeType,
        recipe: Array<out ItemStack?>
    ) : super(itemGroup, item, id, recipeType, recipe)

    companion object {

        private val handlerMap =
            mutableMapOf<KClass<out BaseItemHandler>, (MethodHandle) -> BaseItemHandler>()

        fun <T : BaseItemHandler> registerHandler(
            clazz: KClass<T>,
            handler: (MethodHandle) -> BaseItemHandler
        ) {
            handlerMap[clazz] = handler
        }

        init {
            registerHandler(BlockBreakHandler::class) { handle ->
                object : BlockBreakHandler(false, false) {
                    override fun onPlayerBreak(e: BlockBreakEvent, item: ItemStack, drops: MutableList<ItemStack>) {
                        handle.invoke(e, item, drops)
                    }
                }
            }
            registerHandler(BlockDispenseHandler::class) { BlockDispenseHandler(it::invoke) }
            registerHandler(BlockPlaceHandler::class) { handle ->
                object : BlockPlaceHandler(false) {
                    override fun onPlayerPlace(e: BlockPlaceEvent) {
                        handle.invoke(e)
                    }
                }
            }
            registerHandler(BlockUseHandler::class) { BlockUseHandler(it::invoke) }
            registerHandler(BowShootHandler::class) { BowShootHandler(it::invoke) }
            registerHandler(EntityInteractHandler::class) { EntityInteractHandler(it::invoke) }
            registerHandler(EntityKillHandler::class) { EntityKillHandler(it::invoke) }
            registerHandler(ItemConsumptionHandler::class) { ItemConsumptionHandler(it::invoke) }
            registerHandler(ItemDropHandler::class) {
                ItemDropHandler { e, p, i -> it.invoke(e, p, i) as Boolean }
            }
            registerHandler(ItemUseHandler::class) { ItemUseHandler(it::invoke) }
            registerHandler(MultiBlockInteractionHandler::class) {
                MultiBlockInteractionHandler { p, mb, b -> it.invoke(p, mb, b) as Boolean }
            }
            registerHandler(SimpleBlockBreakHandler::class) {
                object : SimpleBlockBreakHandler() {
                    override fun onBlockBreak(b: Block) {
                        it.invoke(b)
                    }
                }
            }
            registerHandler(ToolUseHandler::class) { ToolUseHandler(it::invoke) }
            registerHandler(WeaponUseHandler::class) { WeaponUseHandler(it::invoke) }
        }
    }

    override fun preRegister() {
        for (method in javaClass.getAllMethods()) {
            if (method.isAnnotationPresent(ItemHandler::class.java)) {
                method.isAccessible = true
                val handle = MethodHandles.lookup().unreflect(method).bindTo(this)
                val handler = method.getAnnotation(ItemHandler::class.java).handler
                val handlerInstance = handlerMap[handler]?.invoke(handle)
                    ?: throw IllegalStateException("Handler $handler is not registered for BetterSlimefunItem")
                addItemHandler(handlerInstance)
            } else if (method.isAnnotationPresent(Ticker::class.java)) {
                method.isAccessible = true
                val handle = MethodHandles.lookup().unreflect(method).bindTo(this)
                val ticker = method.getAnnotation(Ticker::class.java)
                addItemHandler(object : BlockTicker() {
                    override fun tick(b: Block, item: SlimefunItem, data: Config) {
                        handle.invoke(b)
                    }

                    override fun isSynchronized(): Boolean {
                        return !ticker.async
                    }
                })
            }
        }
    }
}

private fun Class<*>.getAllMethods(): List<Method> {
    val methods = mutableListOf<Method>()
    var currentClass: Class<*>? = this
    while (currentClass != null) {
        methods.addAll(currentClass.declaredMethods)
        currentClass = currentClass.superclass
    }
    return methods
}