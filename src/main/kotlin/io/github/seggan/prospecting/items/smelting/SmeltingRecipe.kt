package io.github.seggan.prospecting.items.smelting

data class SmeltingRecipe(
    val inputs: List<Pair<Smeltable, Int>>,
    val output: Pair<Smeltable, Int>,
    val temperature: Int
)
