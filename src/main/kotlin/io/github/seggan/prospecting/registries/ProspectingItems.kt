package io.github.seggan.prospecting.registries

import io.github.seggan.prospecting.Prospecting
import io.github.seggan.prospecting.items.Mallet
import io.github.seggan.prospecting.items.Pebble
import io.github.seggan.sf4k.item.builder.*
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object ProspectingItems : ItemRegistry(Prospecting) {

    val MALLET by buildSlimefunItemDefaultId<Mallet> {
        category = ProspectingCategories.TOOLS
        name = "Mallet"
        material = Material.WOODEN_PICKAXE.asMaterialType()
        recipeType = ProspectingRecipeTypes.VANILLA_CRAFTING_TABLE
        recipe = buildRecipe {
            +"lol"
            +" s "
            +" s "
            'l' means Material.LEATHER
            'o' means Material.STRIPPED_OAK_LOG
            's' means Material.STICK
        }
        +""
        +"A mallet used to crush stuff"
    }

    val STONE_PEBBLE = buildSlimefunItem<Pebble> {
        category = ProspectingCategories.ORES
        name = "Stone pebble"
        material = Material.STONE_BUTTON.asMaterialType()
        id = "PEBBLE_STONE"
        recipeType = ProspectingRecipeTypes.NATURALLY_GENERATED
        recipe = emptyArray()
        +""
        +"A normal stone pebble"
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
            SlimefunItem(
                ProspectingCategories.ORES,
                ore.crushedItem,
                ProspectingRecipeTypes.MALLET,
                arrayOf(ore.oreItem)
            ).register(addon)
        }

        ProspectingRecipeTypes.VANILLA_CRAFTING_TABLE.register(
            Array(9) { STONE_PEBBLE },
            ItemStack(Material.COBBLESTONE)
        )
    }
}