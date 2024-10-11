package io.github.seggan.prospecting.items

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockUseHandler
import me.mrCookieSlime.Slimefun.api.BlockStorage
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class Pebble(
    itemGroup: ItemGroup,
    item: SlimefunItemStack,
    recipeType: RecipeType,
    recipe: Array<out ItemStack>
) : SlimefunItem(itemGroup, item, recipeType, recipe) {
    override fun preRegister() {
        addItemHandler(BlockUseHandler {
            it.cancel()
            val block = it.clickedBlock.get()
            BlockStorage.clearBlockInfo(block)
            block.type = Material.AIR
            it.player.inventory.addItem(item)
        })
    }
}