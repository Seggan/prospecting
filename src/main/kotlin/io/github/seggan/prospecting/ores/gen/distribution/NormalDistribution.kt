package io.github.seggan.prospecting.ores.gen.distribution

import kotlin.math.exp

data class NormalDistribution(val mean: Double, val standardDeviation: Double) : Distribution {
    override fun get(x: Double): Double {
        var squaredSuperscript = (x - mean) / standardDeviation
        squaredSuperscript *= squaredSuperscript
        return exp(-.5 * squaredSuperscript)
    }
}