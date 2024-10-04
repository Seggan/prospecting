package io.github.seggan.prospecting.registries

import io.github.seggan.prospecting.Prospecting
import io.github.seggan.prospecting.items.Ore
import io.github.seggan.sf4k.item.builder.ItemRegistry
import io.github.seggan.sf4k.item.builder.asMaterialType
import io.github.seggan.sf4k.item.builder.buildSlimefunItem
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import org.bukkit.Material

object ProspectingItems : ItemRegistry(Prospecting) {

    val HEMATITE = buildSlimefunItem<Ore>("hematite") {
        category = ProspectingCategories.ORES
        name = "<dark_red>Hematite"
        id = "HEMATITE"
        material = Material.IRON_ORE.asMaterialType()
        recipeType = RecipeType.NULL
        recipe = emptyArray()
    }
}