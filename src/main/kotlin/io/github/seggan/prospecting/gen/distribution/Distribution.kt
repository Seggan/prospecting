package io.github.seggan.prospecting.gen.distribution

import io.github.seggan.prospecting.util.size

interface Distribution {
    operator fun get(x: Double): Double

    companion object {

        val NONE = constant(0.0)

        fun constant(constant: Double) = object : Distribution {
            override fun get(x: Double): Double = constant
        }
    }
}

operator fun Distribution.plus(other: Distribution) = object : Distribution {
    override fun get(x: Double): Double = this@plus[x] + other[x]
}

operator fun Distribution.minus(other: Distribution) = object : Distribution {
    override fun get(x: Double): Double = this@minus[x] - other[x]
}

operator fun Distribution.times(scalar: Double) = object : Distribution {
    override fun get(x: Double): Double = this@times[x] * scalar
}

operator fun Distribution.div(scalar: Double) = object : Distribution {
    override fun get(x: Double): Double = this@div[x] * scalar
}

fun Distribution.precalculate(range: IntRange) = object : Distribution {

    private val precalculated = DoubleArray(range.size)

    init {
        for ((i, x) in range.withIndex()) {
            precalculated[i] = this@precalculate[x.toDouble()]
        }
    }

    override fun get(x: Double): Double {
        val index = x.toInt() - range.first
        return if (index in precalculated.indices) precalculated[index] else this@precalculate[x]
    }
}