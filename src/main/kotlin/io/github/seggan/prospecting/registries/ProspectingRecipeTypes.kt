package io.github.seggan.prospecting.registries

import io.github.seggan.prospecting.items.Mallet
import io.github.seggan.prospecting.util.key
import io.github.seggan.sf4k.extensions.getSlimefun
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.ShapedRecipe

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

    val VANILLA_CRAFTING_TABLE = RecipeType(
        "vanilla_crafting_table".key(),
        CustomItemStack(
            Material.CRAFTING_TABLE,
            "&fVanilla crafting table",
            "",
            "&7This its is crafted in a vanilla crafting table"
        ),
        { inputs, output ->
            val item = output.getSlimefun<SlimefunItem>()
            checkNotNull(item) { "Output must be a Slimefun item, got $output" }
            val key = item.id.lowercase().key()
            if (Bukkit.getRecipe(key) != null) {
                Bukkit.removeRecipe(key)
            }
            val shape = ('a'..'i').toMutableList()
            val ingredients = mutableMapOf<Char, ItemStack>()
            for ((index, input) in inputs.withIndex()) {
                if (input == null) {
                    shape[index] = ' '
                } else {
                    ingredients['a' + index] = input
                }
            }
            val recipe = ShapedRecipe(item.id.lowercase().key(), output)
            recipe.shape(*shape.chunked(3) { it.joinToString("") }.toTypedArray())
            for ((char, ingredient) in ingredients) {
                recipe.setIngredient(char, ingredient)
            }
            Bukkit.addRecipe(recipe)
        }
    )

    val MALLET by lazy {
        RecipeType(
            "mallet".key(),
            ProspectingItems.MALLET,
            { inputs, output -> Mallet.registerRecipe(inputs.filterNotNull(), output) }
        )
    }
}