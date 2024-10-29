package io.github.seggan.prospecting.registries

import io.github.seggan.prospecting.Prospecting
import io.github.seggan.prospecting.items.Mallet
import io.github.seggan.prospecting.items.Pebble
import io.github.seggan.prospecting.items.smelting.Chemical
import io.github.seggan.prospecting.items.smelting.Crucible
import io.github.seggan.prospecting.items.smelting.Kiln
import io.github.seggan.prospecting.items.smelting.Thermometer
import io.github.seggan.prospecting.util.subscript
import io.github.seggan.sf4k.item.builder.*
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
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

    val THERMOMETER by buildSlimefunItemDefaultId<Thermometer> {
        category = ProspectingCategories.TOOLS
        name = "Thermometer"
        material = Material.POTION.asMaterialType()
        recipeType = RecipeType.NULL
        recipe = emptyArray()

        +""
        +"Right click on a crucible to check its temperature"
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

    //<editor-fold desc="Ores" defaultstate="collapsed">
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
    //</editor-fold>

    val CRUCIBLE by buildSlimefunItemDefaultId<Crucible>(10) {
        category = ProspectingCategories.SMELTING
        name = "Crucible"
        material = Material.CAULDRON.asMaterialType()
        recipeType = ProspectingRecipeTypes.VANILLA_CRAFTING_TABLE
        recipe = buildRecipe {
            +"c c"
            +"c c"
            +"ccc"
            'c' means Material.CLAY_BALL
        }

        +""
        +"A crucible used to smelt ores"
    }

    val KILN by buildSlimefunItemDefaultId<Kiln> {
        category = ProspectingCategories.SMELTING
        name = "Kiln"
        material = Material.FURNACE.asMaterialType()
        recipeType = ProspectingRecipeTypes.VANILLA_CRAFTING_TABLE
        recipe = buildRecipe {
            +"ccc"
            +"c c"
            +"ccc"
            'c' means Material.CLAY_BALL
        }

        +""
        +"A kiln used to heat crucibles"
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