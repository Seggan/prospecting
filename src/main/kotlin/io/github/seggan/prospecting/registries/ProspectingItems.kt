package io.github.seggan.prospecting.registries

import io.github.seggan.prospecting.Prospecting
import io.github.seggan.prospecting.items.Pebble
import io.github.seggan.prospecting.util.key
import io.github.seggan.sf4k.item.BetterSlimefunItem
import io.github.seggan.sf4k.item.builder.ItemRegistry
import io.github.seggan.sf4k.item.builder.asMaterialType
import io.github.seggan.sf4k.item.builder.buildSlimefunItem
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe

object ProspectingItems : ItemRegistry(Prospecting) {

    val STONE_PEBBLE = buildSlimefunItem<Pebble> {
        category = ProspectingCategories.ORES
        name = "Stone pebble"
        material = Material.STONE_BUTTON.asMaterialType()
        id = "PEBBLE_STONE"
        recipeType = ProspectingRecipeTypes.NATURALLY_GENERATED
        recipe = emptyArray()
        +"A normal stone pebble"
    }

    val IRON_OXIDE = buildSlimefunItem<BetterSlimefunItem> {
        category = ProspectingCategories.ORES
        name = "<red>IRON_OXIDE"
        material = Material.REDSTONE.asMaterialType()
        id = "IRON_OXIDE"
        recipeType = ProspectingRecipeTypes.NATURALLY_GENERATED
        recipe = emptyArray()
        +"FeO"
    }

    fun initExtra() {
        for (ore in Ore.entries) {
            SlimefunItem(
                ProspectingCategories.ORES,
                ore.oreItem,
                ProspectingRecipeTypes.NATURALLY_GENERATED,
                emptyArray()
            ).register(addon)
            Pebble(
                ProspectingCategories.ORES,
                ore.pebbleItem,
                ProspectingRecipeTypes.NATURALLY_GENERATED,
                emptyArray()
            ).register(addon)
        }

        val recipe = ShapedRecipe("pobble".key(), ItemStack(Material.COBBLESTONE))
        recipe.shape("xxx", "xxx", "xxx")
        recipe.setIngredient('x', STONE_PEBBLE)
        Bukkit.addRecipe(recipe, true)
    }
}