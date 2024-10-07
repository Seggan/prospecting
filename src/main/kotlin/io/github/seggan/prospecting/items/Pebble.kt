package io.github.seggan.prospecting.items

import io.github.seggan.prospecting.registries.ProspectingItems
import io.github.seggan.prospecting.util.key
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockUseHandler
import me.mrCookieSlime.Slimefun.api.BlockStorage
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe

class Pebble(
    itemGroup: ItemGroup,
    item: SlimefunItemStack,
    recipeType: RecipeType,
    recipe: Array<out ItemStack>
) : SlimefunItem(itemGroup, item, recipeType, recipe) {
    override fun preRegister() {
        addItemHandler(BlockUseHandler {
            it.clickedBlock.ifPresent { block ->
                block.type = Material.AIR
                BlockStorage.clearBlockInfo(block)
            }
            it.player.inventory.addItem(item)
        })
    }
}