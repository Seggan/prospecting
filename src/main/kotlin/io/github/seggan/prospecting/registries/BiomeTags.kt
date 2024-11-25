package io.github.seggan.prospecting.registries

import io.github.seggan.prospecting.pluginInstance
import org.bukkit.NamespacedKey
import org.bukkit.Tag
import org.bukkit.block.Biome

enum class BiomeTag(vararg values: Set<Biome>) : Tag<Biome> {
    ALL(Biome.entries.toSet()),
    OCEANS(
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
    RIVERS(setOf(Biome.RIVER, Biome.FROZEN_RIVER)),
    SWAMPS(setOf(Biome.SWAMP, Biome.MANGROVE_SWAMP)),
    MOUNTAINS(
        setOf(
            Biome.JAGGED_PEAKS,
            Biome.FROZEN_PEAKS,
            Biome.STONY_PEAKS,
            Biome.MEADOW,
            Biome.CHERRY_GROVE,
            Biome.GROVE,
            Biome.SNOWY_SLOPES
        )
    ),
    WINDSWEPT_HILLS(
        setOf(
            Biome.WINDSWEPT_HILLS,
            Biome.WINDSWEPT_FOREST,
            Biome.WINDSWEPT_GRAVELLY_HILLS
        )
    ),
    BEACHES(
        setOf(
            Biome.BEACH,
            Biome.STONY_SHORE,
            Biome.SNOWY_BEACH
        )
    ),
    BADLANDS(
        setOf(
            Biome.BADLANDS,
            Biome.ERODED_BADLANDS,
            Biome.WOODED_BADLANDS
        )
    ),
    ;

    private val key = NamespacedKey(pluginInstance, name.lowercase())
    private val biomes = values.flatMapTo(mutableSetOf()) { it }

    override fun getKey(): NamespacedKey = key
    override fun isTagged(item: Biome): Boolean = item in biomes
    override fun getValues(): MutableSet<Biome> = biomes.toMutableSet()
}