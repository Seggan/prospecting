package io.github.seggan.prospecting.items.smelting.forge

import io.github.seggan.prospecting.util.sfb.MenuBlock
import io.github.seggan.prospecting.util.sfb.SlimefunBlock
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils
import org.bukkit.block.Block
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.Gui

class Forge(
    itemGroup: ItemGroup,
    item: SlimefunItemStack,
    recipeType: RecipeType,
    recipe: Array<out ItemStack?>,
    private val capacity: Int,
) : SlimefunItem(itemGroup, item, recipeType, recipe) {

    init {
        SlimefunBlock.applyBlock(this, ::ForgeBlock)
    }

    private class ForgeBlock(block: Block, item: Forge) : MenuBlock<Forge>(block, item) {
        override val gui = Gui.normal()
            .setStructure("#########")
            .addIngredient('#', ChestMenuUtils.getBackground())
            .build()
    }
}