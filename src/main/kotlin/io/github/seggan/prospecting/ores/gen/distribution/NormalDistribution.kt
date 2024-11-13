package io.github.seggan.prospecting.ores.gen.distribution

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.exp

@Serializable
data class NormalDistribution(val mean: Double, @SerialName("std_dev") val standardDeviation: Double) : Distribution {
    override fun get(x: Double): Double {
        var squaredSuperscript = (x - mean) / standardDeviation
        squaredSuperscript *= squaredSuperscript
        return exp(-.5 * squaredSuperscript)
    }
}