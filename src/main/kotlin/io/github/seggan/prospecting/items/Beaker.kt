package io.github.seggan.prospecting.items

import io.github.seggan.prospecting.util.key
import io.github.seggan.sf4k.item.BetterSlimefunItem
import io.github.seggan.sf4k.item.ItemHandler
import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler
import org.bukkit.event.Event
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

class Beaker(
    itemGroup: ItemGroup,
    item: SlimefunItemStack,
    recipeType: RecipeType,
    recipe: Array<out ItemStack?>
) : BetterSlimefunItem(itemGroup, editItem(item), recipeType, recipe) {

    companion object {
        val CONTENTS_KEY = "contents".key()

        private fun editItem(item: SlimefunItemStack): SlimefunItemStack {
            item.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
            return item
        }
    }

    @ItemHandler(ItemUseHandler::class)
    private fun onUse(e: PlayerRightClickEvent) {
        e.setUseItem(Event.Result.DENY)
    }
}