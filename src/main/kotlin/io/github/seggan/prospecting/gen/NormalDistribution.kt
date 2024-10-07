package io.github.seggan.prospecting.gen

import kotlin.math.exp

data class NormalDistribution(val mean: Double, val standardDeviation: Double) : Distribution {
    override fun get(x: Double): Double {
        val firstTerm = 1 / (standardDeviation * SQRT_2_PI)
        var squaredSuperscript = (x - mean) / standardDeviation
        squaredSuperscript *= squaredSuperscript
        val eSup = exp(-.5 * squaredSuperscript)
        return firstTerm * eSup
    }
}

private const val SQRT_2_PI = 2.50662827463