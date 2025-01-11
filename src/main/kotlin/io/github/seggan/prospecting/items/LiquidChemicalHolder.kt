package io.github.seggan.prospecting.items

import io.github.seggan.prospecting.core.Chemical
import io.github.seggan.prospecting.registries.ProspectingCategories
import io.github.seggan.prospecting.registries.ProspectingRecipeTypes
import io.github.seggan.prospecting.util.capitalizeWords
import io.github.seggan.sf4k.item.BetterSlimefunItem
import io.github.seggan.sf4k.item.ItemHandler
import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.core.handlers.ItemUseHandler
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.event.Event
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.meta.PotionMeta

/**
 * Either a beaker or bucket of a liquid [Chemical]
 */
class LiquidChemicalHolder(val chemical: Chemical) : BetterSlimefunItem(
    ProspectingCategories.LIQUIDS,
    generateItem(chemical),
    ProspectingRecipeTypes.LIQUID,
    arrayOfNulls(9)
) {

    val emptyMaterial = if (chemical.meltingPoint < BUCKET_THRESHOLD) Material.GLASS_BOTTLE else Material.BUCKET

    companion object {
        private const val BUCKET_THRESHOLD = 100

        private fun generateId(chemical: Chemical): String {
            return "PROSPECTING_${if (chemical.meltingPoint < BUCKET_THRESHOLD) "BOTTLE" else "BUCKET"}_${chemical.name.uppercase()}"
        }

        private fun generateItem(chemical: Chemical): SlimefunItemStack {
            return if (chemical.meltingPoint < BUCKET_THRESHOLD) {
                // Bottle
                val item = SlimefunItemStack(
                    generateId(chemical),
                    Material.POTION,
                    "&fBottle of ${chemical.name.capitalizeWords()}"
                )
                item.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
                item.editMeta { meta ->
                    meta as PotionMeta
                    meta.color = Color.fromRGB(0x636359)
                }
                item
            } else {
                // Bucket
                SlimefunItemStack(
                    generateId(chemical),
                    Material.LAVA_BUCKET,
                    "&fBucket of Liquid ${chemical.name.capitalizeWords()}"
                )
            }
        }

        fun getHolder(chemical: Chemical): LiquidChemicalHolder? {
            return getById(generateId(chemical)) as? LiquidChemicalHolder
        }
    }

    @ItemHandler(ItemUseHandler::class)
    private fun onUse(e: PlayerRightClickEvent) {
        e.setUseItem(Event.Result.DENY)
    }
}