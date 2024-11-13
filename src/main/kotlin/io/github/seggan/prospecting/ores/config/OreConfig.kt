package io.github.seggan.prospecting.ores.config

import io.github.seggan.prospecting.items.smelting.Smeltable
import io.github.seggan.prospecting.ores.Ore
import io.github.seggan.prospecting.ores.gen.generator.OreGenerator
import io.github.seggan.sf4k.serial.serializers.BukkitSerializerRegistry
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.RandomizedSet
import kotlinx.serialization.Contextual
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.plus
import org.bukkit.Material
import org.bukkit.NamespacedKey

@Serializable
data class OreConfig(
    val pebble: Material,
    @SerialName("ore_type") val oreType: Material,
    val formula: String,
    val crushing: CrushConfig,
    @Serializable(with = GeneratorSerializer::class) val generator: OreGenerator,
    val associations: List<@Contextual NamespacedKey> = emptyList()
) {
    companion object {
        @OptIn(ExperimentalSerializationApi::class)
        private val json = Json {
            serializersModule += BukkitSerializerRegistry.serializersModule
            ignoreUnknownKeys = true
            decodeEnumsCaseInsensitive = true
            allowComments = true
            isLenient = true
        }

        fun parse(s: String): Map<NamespacedKey, OreConfig> {
            return json.decodeFromString<Map<NamespacedKey, OreConfig>>(s)
        }
    }

    fun toOre(key: NamespacedKey): Ore {
        val (yield, items) = crushing.convert()
        return Ore(key, pebble, oreType, formula, items, yield, generator)
    }
}

@Serializable
data class CrushConfig(val yield: Yield, val results: Map<@Contextual NamespacedKey, Float>) {

    @Serializable
    data class Yield(val min: Int, val max: Int)

    fun convert(): Pair<IntRange, RandomizedSet<Smeltable>> {
        val range = yield.min..yield.max
        val set = RandomizedSet<Smeltable>()
        for ((key, value) in results) {
            set.add(Smeltable[key] ?: error("No smeltable found with key $key"), value)
        }
        return range to set
    }
}

