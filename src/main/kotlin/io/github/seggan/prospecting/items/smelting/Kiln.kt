package io.github.seggan.prospecting.items.smelting

import io.github.seggan.prospecting.util.SlimefunBlock
import io.github.seggan.prospecting.util.SlimefunBlock.Companion.applySlimefunBlock
import io.github.seggan.prospecting.util.moveAsymptoticallyTo
import io.github.seggan.sf4k.serial.blockstorage.getBlockStorage
import io.github.seggan.sf4k.serial.blockstorage.setBlockStorage
import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
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
) : SlimefunItem(itemGroup, item, recipeType, recipe) {

    companion object {
        private val fuels = mapOf(
            ItemStack(Material.COAL) to Fuel(800, 20),
            ItemStack(Material.CHARCOAL) to Fuel(800, 20),
        )
    }

    init {
        applySlimefunBlock(::KilnBlock)
    }

    @Serializable
    private data class Fuel(val maxTemp: Int, var burnTime: Int, var currentMax: Double = maxTemp.toDouble())

    private inner class KilnBlock(block: Block) : SlimefunBlock(block) {

        private val fuelQueue: ArrayDeque<Fuel> by blockStorage { ArrayDeque() }
        private var currentFuel: Fuel? by blockStorage { null }

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

            val crucibleBlock = block.getRelative(BlockFace.UP)
            if (BlockStorage.check(crucibleBlock) !is Crucible) return
            var temperature = crucibleBlock.getBlockStorage<Double>("temperature") ?: 0.0
            val contents = crucibleBlock.getBlockStorage<Map<Smeltable, Int>>("contents") ?: emptyMap()
            if (temperature < fuel.currentMax) {
                val rate = 0.1 / (contents.values.sum() + 1)
                temperature = temperature.moveAsymptoticallyTo(fuel.currentMax, rate)
            }
            crucibleBlock.setBlockStorage<Double>("temperature", temperature)
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
        }
    }
}