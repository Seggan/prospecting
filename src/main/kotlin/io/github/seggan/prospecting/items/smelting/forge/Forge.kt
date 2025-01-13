package io.github.seggan.prospecting.items.smelting.forge

import io.github.seggan.prospecting.core.Chemical
import io.github.seggan.prospecting.util.sfb.SlimefunBlock
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack

class Forge(
    itemGroup: ItemGroup,
    item: SlimefunItemStack,
    recipeType: RecipeType,
    recipe: Array<out ItemStack?>
) : SlimefunItem(itemGroup, item, recipeType, recipe) {

    init {
        SlimefunBlock.Companion.applyBlock(this, ::ForgeBlock)
    }

    private class ForgeBlock(block: Block) : SlimefunBlock(block) {
        private val contents: MutableMap<Chemical, Int> by blockStorage { mutableMapOf() }
        private var temperature: Double by blockStorage { Chemical.ROOM_TEMPERATURE }
    }
}