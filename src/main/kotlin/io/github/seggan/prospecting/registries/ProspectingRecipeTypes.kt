package io.github.seggan.prospecting.registries

import io.github.seggan.prospecting.Prospecting
import io.github.seggan.prospecting.items.Mallet
import io.github.seggan.prospecting.util.key
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.inventory.ItemStack

object ProspectingRecipeTypes : Listener {

    init {
        Bukkit.getPluginManager().registerEvents(this, Prospecting)
    }

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
        { inputs, output -> vanillaRecipes.add(inputs to output) }
    )

    val MALLET by lazy {
        RecipeType(
            "mallet".key(),
            ProspectingItems.MALLET,
            { inputs, output -> Mallet.registerRecipe(inputs.filterNotNull(), output) }
        )
    }

    private val vanillaRecipes = mutableSetOf<Pair<Array<out ItemStack?>, ItemStack>>()

    @EventHandler
    private fun onCraft(e: PrepareItemCraftEvent) {
        val inv = e.inventory
        val items = inv.matrix
        for ((inputs, output) in vanillaRecipes) {
            if (inputs.zip(items).all { (input, needed) -> SlimefunUtils.isItemSimilar(input, needed, false, false) }) {
                inv.result = output
                break
            }
        }
    }
}