package io.github.seggan.prospecting.items.smelting.items

import io.github.seggan.prospecting.items.smelting.Smeltable
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import org.bukkit.inventory.ItemStack

class Chemical(
    itemGroup: ItemGroup,
    item: SlimefunItemStack,
    recipeType: RecipeType,
    recipe: Array<out ItemStack?>
) : SlimefunItem(itemGroup, item, recipeType, recipe) {

    init {
        useableInWorkbench = true
    }

    override fun postRegister() {
        Smeltable.register(item)
    }
}