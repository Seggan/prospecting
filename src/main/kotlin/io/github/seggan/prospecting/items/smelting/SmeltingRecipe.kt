package io.github.seggan.prospecting.items.smelting

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SmeltingRecipe(
    val temperature: Int,
    val inputs: Map<Chemical, Int>,
    val output: Chemical,
    @SerialName("output_amount") val outputAmount: Int = 1
)
