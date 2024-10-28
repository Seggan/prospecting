package io.github.seggan.prospecting.items.smelting

import io.github.seggan.prospecting.registries.ProspectingItems
import io.github.seggan.sf4k.item.BetterSlimefunItem
import io.github.seggan.sf4k.item.Ticker
import io.github.seggan.sf4k.serial.blockstorage.getBlockStorage
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems
import org.bukkit.block.Block
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack

class Crucible(
    itemGroup: ItemGroup,
    item: SlimefunItemStack,
    recipeType: RecipeType,
    recipe: Array<out ItemStack?>,
    private val capacity: Int,
) : BetterSlimefunItem(itemGroup, item, recipeType, recipe) {

    companion object {
        private val recipes = mutableSetOf<SmeltingRecipe>()

        fun registerRecipe(recipe: SmeltingRecipe) {
            recipes.add(recipe)
        }

        fun registerRecipe(
            output: Smeltable,
            temperature: Int,
            vararg inputs: Pair<Smeltable, Int>
        ) = registerRecipe(SmeltingRecipe(inputs.toList(), output, temperature))

        init {
            registerRecipe(
                Smeltable[ProspectingItems.COPPER_OXIDE]!!,
                300,
                Smeltable[ProspectingItems.COPPER_CARBONATE]!! to 1
            )
            registerRecipe(
                Smeltable[SlimefunItems.COPPER_INGOT]!!,
                1200,
                Smeltable[ProspectingItems.COPPER_OXIDE]!! to 1
            )
        }
    }

    @Ticker
    private fun tick(b: Block) {
        val contents = b.getBlockStorage<MutableMap<Smeltable, Int>>("contents") ?: mutableMapOf()
        addNewItems(b, contents)
    }

    private fun addNewItems(b: Block, contents: MutableMap<Smeltable, Int>) {
        val items = b.world.getNearbyEntities(b.location.add(0.5, 0.5, 0.5), 0.5, 0.5, 0.5)
            .filterIsInstance<Item>()
        for (item in items) {
            val stack = item.itemStack
            if (contents.values.sum() + stack.amount > capacity) continue
            val smeltable = Smeltable[stack] ?: continue
            contents.merge(smeltable, stack.amount, Int::plus)
            item.remove()
        }
    }
}