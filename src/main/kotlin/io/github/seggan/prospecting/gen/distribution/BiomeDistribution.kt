package io.github.seggan.prospecting.gen.distribution

import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap
import org.bukkit.block.Biome

class BiomeDistribution(private val default: Float, biomes: Map<Biome, Float>) {

    private val biomes = Object2FloatOpenHashMap<Biome>().apply {
        defaultReturnValue(default)
        putAll(biomes)
    }

    operator fun get(biome: Biome): Float = biomes.getFloat(biome)
}

inline fun biomeDistribution(default: Float = 0f, block: MutableMap<Biome, Float>.() -> Unit): BiomeDistribution {
    val map = mutableMapOf<Biome, Float>()
    map.block()
    return BiomeDistribution(default, map)
}