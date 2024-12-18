package io.github.seggan.prospecting.ores.config

import io.github.seggan.prospecting.ores.gen.distribution.Distribution
import io.github.seggan.prospecting.ores.gen.distribution.NormalDistribution
import io.github.seggan.prospecting.ores.gen.distribution.times
import io.github.seggan.prospecting.ores.gen.generator.LargeVeinGenerator
import io.github.seggan.prospecting.ores.gen.generator.NearLavaGenerator
import io.github.seggan.prospecting.ores.gen.generator.OreGenerator
import io.github.seggan.prospecting.ores.gen.generator.PlacerGenerator
import io.github.seggan.prospecting.pluginInstance
import io.github.seggan.prospecting.registries.BiomeTag
import io.github.seggan.prospecting.util.key
import io.github.seggan.sf4k.serial.serializers.BukkitSerializerRegistry
import io.github.seggan.sf4k.serial.serializers.DelegatingSerializer
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import kotlinx.serialization.json.*
import kotlinx.serialization.modules.plus
import org.bukkit.NamespacedKey
import org.bukkit.block.Biome

object GeneratorSerializer : DelegatingSerializer<OreGenerator, JsonObject>(JsonObject.serializer()) {

    private val json = Json {
        serializersModule += BukkitSerializerRegistry.serializersModule
        ignoreUnknownKeys = true
    }

    override fun toData(value: OreGenerator) = throw UnsupportedOperationException()

    override fun fromData(value: JsonObject): OreGenerator {
        val key = json.decodeFromJsonElement<NamespacedKey>(value["type"]!!)
        val type = generators[key] ?: error("Unknown generator key $key")
        return type(value)
    }

    val generators = mutableMapOf<NamespacedKey, (JsonObject) -> OreGenerator>()
    val distributions = mutableMapOf<NamespacedKey, (JsonObject) -> Distribution>()

    init {
        generators["noise_vein".key()] = { obj ->
            val size = obj["size"]!!.jsonPrimitive.int
            val distributionObj = obj["height_distribution"]!!.jsonObject
            val distribution = getDistribution(distributionObj)
            val biomeDistribution = mutableMapOf<Biome, Float>()
            for ((biomeOrTag, chance) in obj["biome_distribution"]!!.jsonObject) {
                val key = NamespacedKey.fromString(biomeOrTag.removePrefix("#"), pluginInstance)!!
                if (biomeOrTag.startsWith('#')) {
                    val tag = BiomeTag.entries.find { it.key == key } ?: error("Unknown biome tag $key")
                    for (biome in tag.values) {
                        biomeDistribution[biome] = chance.jsonPrimitive.float
                    }
                } else {
                    val biome = RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME).get(key)
                        ?: error("Unknown biome $key")
                    biomeDistribution[biome] = chance.jsonPrimitive.float
                }
            }
            LargeVeinGenerator(size, distribution, biomeDistribution)
        }
        generators["placer".key()] = { json.decodeFromJsonElement<PlacerGenerator>(it) }
        generators["near_lava".key()] = { json.decodeFromJsonElement<NearLavaGenerator>(it) }

        distributions["normal".key()] = { json.decodeFromJsonElement<NormalDistribution>(it) }
        distributions["multiply".key()] = {
            val distribution = getDistribution(it["distribution"]!!.jsonObject)
            val multiplier = it["value"]!!.jsonPrimitive.double
            distribution * multiplier
        }
    }

    private fun getDistribution(obj: JsonObject): Distribution {
        val key = json.decodeFromJsonElement<NamespacedKey>(obj["type"]!!)
        val serializer = distributions[key] ?: error("Unknown distribution $key")
        return serializer(obj)
    }
}