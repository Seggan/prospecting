package io.github.seggan.prospecting.registries

import io.github.seggan.prospecting.util.key
import io.github.thebusybiscuit.slimefun4.api.items.groups.NestedItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.groups.SubItemGroup
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack
import org.bukkit.Material

object ProspectingCategories {

    val MAIN = NestedItemGroup(
        "prospecting".key(),
        CustomItemStack(Material.IRON_ORE, "&fProspecting")
    )

    val ORES = SubItemGroup(
        "ores".key(),
        MAIN,
        CustomItemStack(Material.COPPER_ORE, "&7Ores")
    )
}