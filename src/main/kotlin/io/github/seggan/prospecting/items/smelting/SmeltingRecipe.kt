package io.github.seggan.prospecting.items.smelting

import kotlinx.serialization.Serializable

@Serializable
data class SmeltingRecipe(
    val temperature: Int,
    val inputs: Map<Chemical, Int>,
    val outputs: Map<Chemical, Int>,
)
