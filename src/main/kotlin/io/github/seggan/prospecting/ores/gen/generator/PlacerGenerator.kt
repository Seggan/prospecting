package io.github.seggan.prospecting.ores.gen.generator

import io.github.seggan.prospecting.registries.BiomeTag
import io.github.seggan.prospecting.util.getHighestOpaqueBlockY
import kotlinx.serialization.Serializable
import org.bukkit.ChunkSnapshot
import org.bukkit.block.Biome
import kotlin.random.Random

@Serializable
class PlacerGenerator(private val rarity: Double, private val depth: Int, private val density: Float) : OreGenerator {

    override val generateMarker = false

    override fun generate(
        seed: Long,
        chunk: ChunkSnapshot,
        cx: Int,
        cz: Int,
        random: Random,
        setBlock: OreGenerator.OreSetter
    ) {
        val seed = longArrayOf(seed, cx.toLong(), cz.toLong()).contentHashCode()
        if (Random(seed).nextDouble() > rarity) return
        for (x in 0..15) {
            for (z in 0..15) {
                val top = chunk.getHighestOpaqueBlockY(x, z)
                if (chunk.getBiome(x, top, z) !in ALLOWED_BIOMES) continue
                repeat(depth) {
                    if (random.nextFloat() < density) {
                        setBlock(x, top - it, z)
                    }
                }
            }
        }
    }
}

private val ALLOWED_BIOMES = BiomeTag.OCEANS.values + BiomeTag.BEACHES.values + BiomeTag.RIVERS.values +
        BiomeTag.BADLANDS.values + BiomeTag.MOUNTAINS.values + BiomeTag.WINDSWEPT_HILLS.values +
        Biome.DESERT