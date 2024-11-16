package io.github.seggan.prospecting.items.smelting

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SmeltingRecipe(
    val temperature: Int,
    val inputs: Map<Smeltable, Int>,
    val output: Smeltable,
    @SerialName("output_amount") val outputAmount: Int = 1
)
