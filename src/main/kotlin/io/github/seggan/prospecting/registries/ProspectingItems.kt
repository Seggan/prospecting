package io.github.seggan.prospecting.registries

import io.github.seggan.prospecting.Prospecting
import io.github.seggan.prospecting.items.Mallet
import io.github.seggan.prospecting.items.Pebble
import io.github.seggan.prospecting.items.smelting.Crucible
import io.github.seggan.prospecting.items.smelting.Kiln
import io.github.seggan.prospecting.items.smelting.items.Chemical
import io.github.seggan.prospecting.items.smelting.items.Slag
import io.github.seggan.prospecting.items.smelting.tools.Bellows
import io.github.seggan.prospecting.items.smelting.tools.Thermometer
import io.github.seggan.prospecting.util.subscript
import io.github.seggan.sf4k.item.builder.*
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object ProspectingItems : ItemRegistry(Prospecting) {

    val MALLET by buildSlimefunItem<Mallet> {
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

    val THERMOMETER by buildSlimefunItem<Thermometer> {
        category = ProspectingCategories.TOOLS
        name = "Thermometer"
        material = Material.POTION.asMaterialType()
        recipeType = RecipeType.NULL
        recipe = emptyArray()

        +""
        +"Right click on a crucible to check its temperature"
    }

    val STONE_PEBBLE by buildSlimefunItem<Pebble> {
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
    val IRON_OXIDE by buildSlimefunItem<Chemical> {
        category = ProspectingCategories.RAW_MATERIALS
        name = "<red>Iron Oxide"
        material = Material.RED_DYE.asMaterialType()
        recipeType = ProspectingRecipeTypes.NATURALLY_GENERATED
        recipe = emptyArray()
        +""
        +"<green>Formula: FeO".subscript()
    }

    val NICKEL_OXIDE by buildSlimefunItem<SlimefunItem> {
        category = ProspectingCategories.RAW_MATERIALS
        name = "<white>Nickel Oxide"
        material = Material.WHITE_DYE.asMaterialType()
        recipeType = ProspectingRecipeTypes.NATURALLY_GENERATED
        recipe = emptyArray()
        +""
        +"<green>Formula: NiO".subscript()
    }

    val COPPER_CARBONATE by buildSlimefunItem<Chemical> {
        category = ProspectingCategories.RAW_MATERIALS
        name = "<dark_aqua>Copper Carbonate"
        material = Material.CYAN_DYE.asMaterialType()
        recipeType = ProspectingRecipeTypes.NATURALLY_GENERATED
        recipe = emptyArray()
        +""
        +"<green>Formula: CuCO3".subscript()
    }

    val COPPER_OXIDE by buildSlimefunItem<Chemical> {
        category = ProspectingCategories.RAW_MATERIALS
        name = "<dark_aqua>Copper Oxide"
        material = Material.BLACK_DYE.asMaterialType()
        recipeType = ProspectingRecipeTypes.NATURALLY_GENERATED
        recipe = emptyArray()
        +""
        +"<green>Formula: CuO".subscript()
    }
    //</editor-fold>

    val CRUCIBLE by buildSlimefunItem<Crucible>(10) {
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

    val KILN by buildSlimefunItem<Kiln> {
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

    val BELLOWS by buildSlimefunItem<Bellows> {
        category = ProspectingCategories.SMELTING
        name = "Bellows"
        material = MaterialType.Head("ewogICJ0aW1lc3RhbXAiIDogMTczMDI0Mjk0MDE4NSwKICAicHJvZmlsZUlkIiA6ICI3MzE4MWQxZDRjYWQ0ZmU0YTcxNWNjNmUxOGNjYzVkNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJaZmVybjRuZGl0byIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8xZTNmMTZjOWE4Y2YyMzg0ZDU4MTUyYjEzMDBlNDgyZGVjMzM3MTAyNWEzOTIyYzM5ZWRjOGI3MTY1ZTBiMjk2IgogICAgfQogIH0KfQ==")
        recipeType = ProspectingRecipeTypes.VANILLA_CRAFTING_TABLE
        recipe = buildRecipe {
            +"l l"
            +"l l"
            +"l l"
            'l' means Material.LEATHER
        }

        +""
        +"Place down against a kiln and"
        +"right click to increase its temperature"
    }

    val COKE by buildSlimefunItem<Chemical> {
        category = ProspectingCategories.RAW_MATERIALS
        name = "<black>Coke"
        material = Material.COAL.asMaterialType()
        recipeType = ProspectingRecipeTypes.CRUCIBLE
        recipe = arrayOf(ItemStack(Material.COAL_BLOCK))
        +""
        +"<green>Formula: C".subscript()
    }

    val SLAG by buildSlimefunItem<Slag> {
        category = ProspectingCategories.RAW_MATERIALS
        name = "<gray>Slag"
        material = Material.GRAY_DYE.asMaterialType()
        recipeType = ProspectingRecipeTypes.CRUCIBLE
        recipe = emptyArray()

        +""
        +"A byproduct of smelting ores"
        +"Crush it with a mallet to get the contents"
    }

    fun initExtra() {
        for (ore in Ore.entries) {
            ore.register(addon)
        }

        ProspectingRecipeTypes.VANILLA_CRAFTING_TABLE.register(
            Array(9) { STONE_PEBBLE },
            ItemStack(Material.COBBLESTONE)
        )

        Crucible.initRecipes()
        Kiln.initFuels()
    }
}