package io.github.seggan.prospecting.registries

import io.github.seggan.prospecting.pluginInstance
import org.bukkit.NamespacedKey
import org.bukkit.Tag
import org.bukkit.block.Biome

enum class BiomeTag(vararg values: Set<Biome>) : Tag<Biome> {
    OCEAN(
        setOf(
            Biome.OCEAN,
            Biome.DEEP_OCEAN,
            Biome.WARM_OCEAN,
            Biome.LUKEWARM_OCEAN,
            Biome.DEEP_LUKEWARM_OCEAN,
            Biome.COLD_OCEAN,
            Biome.DEEP_COLD_OCEAN,
            Biome.FROZEN_OCEAN,
            Biome.DEEP_FROZEN_OCEAN
        )
    ),
    WATERY(setOf(Biome.RIVER, Biome.FROZEN_RIVER, Biome.MUSHROOM_FIELDS), OCEAN.values),
    ;

    private val key = NamespacedKey(pluginInstance, name.lowercase())
    private val biomes = values.flatMapTo(mutableSetOf()) { it }

    override fun getKey(): NamespacedKey = key
    override fun isTagged(item: Biome): Boolean = item in biomes
    override fun getValues(): MutableSet<Biome> = biomes.toMutableSet()
}