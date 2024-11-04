package io.github.seggan.prospecting.items.smelting

import io.github.seggan.prospecting.registries.ProspectingItems
import io.github.seggan.prospecting.util.SlimefunBlock
import io.github.seggan.prospecting.util.miniMessage
import io.github.seggan.prospecting.util.moveAsymptoticallyTo
import io.github.seggan.prospecting.util.secondsToSfTicks
import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils
import kotlinx.serialization.Serializable
import me.mrCookieSlime.Slimefun.api.BlockStorage
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.type.Furnace
import org.bukkit.event.Event
import org.bukkit.inventory.ItemStack

class Kiln(
    itemGroup: ItemGroup,
    item: SlimefunItemStack,
    recipeType: RecipeType,
    recipe: Array<out ItemStack?>
) : SlimefunItem(itemGroup, item, recipeType, recipe), RecipeDisplayItem {

    companion object {

        private lateinit var fuels: Map<ItemStack, Fuel>

        internal fun initFuels() {
            fuels = mapOf(
                ItemStack(Material.COAL) to Fuel(900, secondsToSfTicks(10)),
                ItemStack(Material.CHARCOAL) to Fuel(900, secondsToSfTicks(10)),
                ItemStack(Material.COAL_BLOCK) to Fuel(900, secondsToSfTicks(10 * 9)),
                ProspectingItems.COKE to Fuel(1100, secondsToSfTicks(3 * 60)),
            )
        }
    }

    init {
        SlimefunBlock.applyBlock(this@Kiln, ::KilnBlock)
    }

    fun useBellowsOn(block: Block) {
        val kiln = KilnBlock(block)
        val currentFuel = kiln.currentFuel ?: return
        currentFuel.currentMax = currentFuel.maxTemp * 1.2
        kiln.saveData()
    }

    @Serializable
    private data class Fuel(val maxTemp: Int, var burnTime: Int, var currentMax: Double = maxTemp.toDouble())

    private inner class KilnBlock(block: Block) : SlimefunBlock(block) {

        val fuelQueue: ArrayDeque<Fuel> by blockStorage { ArrayDeque() }
        var currentFuel: Fuel? by blockStorage { null }

        override fun tick() {
            if (currentFuel == null && fuelQueue.isNotEmpty()) {
                currentFuel = fuelQueue.removeFirst()
                val blockData = block.blockData as Furnace
                blockData.isLit = true
                block.blockData = blockData
            }

            val fuel = currentFuel ?: return
            if (fuel.currentMax >= fuel.maxTemp) {
                fuel.currentMax = fuel.currentMax.moveAsymptoticallyTo(fuel.maxTemp.toDouble(), 0.1)
            }
            if (fuel.burnTime-- <= 0) {
                currentFuel = fuelQueue.removeFirstOrNull()
                if (currentFuel == null) {
                    val blockData = block.blockData as Furnace
                    blockData.isLit = false
                    block.blockData = blockData
                    return
                }
            }

            val blockAbove = block.getRelative(BlockFace.UP)
            val crucible = BlockStorage.check(blockAbove) as? Crucible ?: return
            val crucibleBlock = crucible.CrucibleBlock(blockAbove)
            if (crucibleBlock.temperature < fuel.currentMax) {
                val rate = 0.1 / (crucibleBlock.contents.values.sum() + 1)
                crucibleBlock.temperature = crucibleBlock.temperature.moveAsymptoticallyTo(fuel.currentMax, rate)
            }
            crucibleBlock.saveData()
        }

        override fun onInteract(e: PlayerRightClickEvent) {
            e.setUseBlock(Event.Result.DENY)
            val item = e.item
            for ((fuelItem, fuel) in fuels) {
                if (SlimefunUtils.isItemSimilar(item, fuelItem, false)) {
                    fuelQueue.add(fuel.copy())
                    item.subtract()
                    return
                }
            }
            if ("SHOVEL" in e.item.type.name) {
                val blockData = block.blockData as Furnace
                blockData.isLit = !blockData.isLit
                block.blockData = blockData
                currentFuel = null
                fuelQueue.clear()
            }
        }
    }

    override fun getDisplayRecipes(): MutableList<ItemStack> {
        val recipes = mutableListOf<ItemStack>()
        for ((fuel, data) in fuels) {
            val item = fuel.clone()
            item.editMeta {
                val tick = if (data.burnTime != 1) "ticks" else "tick"
                it.lore(
                    listOf(
                        "",
                        "<#ffa200>Max Temperature: <white>${data.maxTemp}°C",
                        "<#ffa200>Burn Time: <white>${data.burnTime} Slimefun $tick",
                    ).miniMessage()
                )
            }
            recipes.add(item)
        }
        return recipes
    }
}