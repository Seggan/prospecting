package io.github.seggan.prospecting.registries

import io.github.seggan.prospecting.util.key
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack
import org.bukkit.Material

object ProspectingRecipeTypes {
    val NATURALLY_GENERATED = RecipeType(
        "naturally_generated".key(),
        CustomItemStack(
            Material.IRON_ORE,
            "&fNaturally generated",
            "",
            "&7This block is found generating in the world"
        )
    )
}