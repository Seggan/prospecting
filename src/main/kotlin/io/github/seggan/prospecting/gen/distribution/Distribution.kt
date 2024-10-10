package io.github.seggan.prospecting.gen.distribution

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