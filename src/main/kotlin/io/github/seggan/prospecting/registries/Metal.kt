package io.github.seggan.prospecting.registries

import org.bukkit.Material

enum class Metal(val vanillaOre: Material) {
    IRON(Material.IRON_ORE),
    GOLD(Material.GOLD_ORE),
    COPPER(Material.COPPER_ORE),
    TIN(Material.IRON_ORE),
    ZINC(Material.IRON_ORE),
    ALUMINUM(Material.IRON_ORE),
    MAGNESIUM(Material.IRON_ORE),

    // The following are specific to Prospecting
    SODIUM(Material.CALCITE),
    MERCURY(Material.BASALT)
}