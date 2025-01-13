package io.github.seggan.prospecting.core

import kotlinx.serialization.Serializable

@Serializable
data class SmeltingRecipe(
    val temperature: Int,
    val inputs: Map<Chemical, Int>,
    val outputs: Map<Chemical, Int>,
) {
    fun performRecipe(temperature: Double, contents: MutableMap<Chemical, Int>) {
        if (
            temperature >= this.temperature
            &&
            inputs.all { (input, amount) -> contents.getOrDefault(input, 0) >= amount }
            &&
            (inputs.all { (input, _) -> input.getState(temperature) != Chemical.State.GAS }
                    || inputs.size == 1)
        ) {
            for ((input, amount) in inputs) {
                contents.merge(input, amount, Int::minus)
            }
            for ((output, amount) in outputs) {
                contents.merge(output, amount, Int::plus)
            }
        }
    }
}