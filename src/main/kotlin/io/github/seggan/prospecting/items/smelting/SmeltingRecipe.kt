package io.github.seggan.prospecting.items.smelting

data class SmeltingRecipe(
    val inputs: List<Pair<Smeltable, Int>>,
    val output: Smeltable,
    val temperature: Int,
    val requiredState: Smeltable.State? = null
) {

    fun canSmelt(temperature: Double): Boolean {
        return temperature >= this.temperature
                && (requiredState == null || inputs.all { it.first.getState(temperature) == requiredState })
    }
}
