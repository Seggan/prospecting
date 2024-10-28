package io.github.seggan.prospecting.registries

import io.github.seggan.prospecting.Prospecting
import io.github.seggan.prospecting.items.Mallet
import io.github.seggan.prospecting.items.Pebble
import io.github.seggan.prospecting.items.smelting.Chemical
import io.github.seggan.prospecting.util.subscript
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

    val IRON_OXIDE by buildSlimefunItemDefaultId<Chemical> {
        category = ProspectingCategories.RAW_MATERIALS
        name = "<red>Iron Oxide"
        material = Material.RED_DYE.asMaterialType()
        recipeType = ProspectingRecipeTypes.NATURALLY_GENERATED
        recipe = emptyArray()
        +""
        +"<green>Formula: FeO".subscript()
    }

    val NICKEL_OXIDE by buildSlimefunItemDefaultId<SlimefunItem> {
        category = ProspectingCategories.RAW_MATERIALS
        name = "<white>Nickel Oxide"
        material = Material.WHITE_DYE.asMaterialType()
        recipeType = ProspectingRecipeTypes.NATURALLY_GENERATED
        recipe = emptyArray()
        +""
        +"<green>Formula: NiO".subscript()
    }

    val COPPER_CARBONATE by buildSlimefunItemDefaultId<Chemical> {
        category = ProspectingCategories.RAW_MATERIALS
        name = "<dark_aqua>Copper Carbonate"
        material = Material.CYAN_DYE.asMaterialType()
        recipeType = ProspectingRecipeTypes.NATURALLY_GENERATED
        recipe = emptyArray()
        +""
        +"<green>Formula: CuCO3".subscript()
    }

    val COPPER_OXIDE by buildSlimefunItemDefaultId<Chemical> {
        category = ProspectingCategories.RAW_MATERIALS
        name = "<dark_aqua>Copper Oxide"
        material = Material.BLACK_DYE.asMaterialType()
        recipeType = ProspectingRecipeTypes.NATURALLY_GENERATED
        recipe = emptyArray()
        +""
        +"<green>Formula: CuO".subscript()
    }

    fun initExtra() {
        for (ore in Ore.entries) {
            ore.register(addon)
        }

        ProspectingRecipeTypes.VANILLA_CRAFTING_TABLE.register(
            Array(9) { STONE_PEBBLE },
            ItemStack(Material.COBBLESTONE)
        )
    }
}