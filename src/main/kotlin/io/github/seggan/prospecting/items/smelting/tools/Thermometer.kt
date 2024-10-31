package io.github.seggan.prospecting.items.smelting.tools

import io.github.seggan.sf4k.item.BetterSlimefunItem
import io.github.seggan.sf4k.item.ItemHandler
import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler
import org.bukkit.Color
import org.bukkit.event.Event
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta

class Thermometer(
    itemGroup: ItemGroup,
    item: SlimefunItemStack,
    recipeType: RecipeType,
    recipe: Array<out ItemStack?>
) : BetterSlimefunItem(itemGroup, item.also(Companion::modifyItem), recipeType, recipe) {

    companion object {
        private fun modifyItem(item: SlimefunItemStack) {
            val meta = item.itemMeta as PotionMeta
            meta.color = Color.RED
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
            item.itemMeta = meta
        }
    }

    @ItemHandler(ItemUseHandler::class)
    private fun onUse(e: PlayerRightClickEvent) {
        e.setUseItem(Event.Result.DENY)
    }
}