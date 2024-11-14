package io.github.seggan.prospecting.items.smelting

import io.github.seggan.prospecting.registries.ProspectingItems
import io.github.seggan.prospecting.util.key
import io.github.seggan.sf4k.serial.pdc.getData
import io.github.seggan.sf4k.serial.pdc.setData
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import org.bukkit.inventory.ItemStack

class Slag(
    itemGroup: ItemGroup,
    item: SlimefunItemStack,
    recipeType: RecipeType,
    recipe: Array<out ItemStack?>
) : SlimefunItem(itemGroup, item, recipeType, recipe) {

    companion object {

        private val contentsKey = "contents".key()

        fun create(contents: Map<Smeltable, Int>): ItemStack {
            val item = ProspectingItems.SLAG.clone()
            val meta = item.itemMeta
            meta.persistentDataContainer.setData<Map<Smeltable, Int>>(contentsKey, contents)
            item.itemMeta = meta
            return item
        }

        fun getContents(item: ItemStack): List<ItemStack> {
            val meta = item.itemMeta
            val data = meta.persistentDataContainer.getData<Map<Smeltable, Int>>(contentsKey) ?: return emptyList()
            return data.map { (smeltable, amount) ->
                smeltable.item.clone().also { it.amount = amount }
            }
        }
    }
}