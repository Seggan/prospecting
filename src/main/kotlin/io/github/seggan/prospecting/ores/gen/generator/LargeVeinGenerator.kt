package io.github.seggan.prospecting.ores.gen.generator

import io.github.seggan.prospecting.ores.gen.distribution.Distribution
import io.github.seggan.prospecting.ores.gen.distribution.precalculate
import io.github.seggan.prospecting.ores.gen.distribution.times
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap
import org.bukkit.ChunkSnapshot
import org.bukkit.block.Biome
import org.bukkit.util.noise.OctaveGenerator
import org.bukkit.util.noise.SimplexOctaveGenerator
import java.util.Random
import kotlin.math.pow

class LargeVeinGenerator(
    private val size: Int,
    distribution: Distribution,
    biomes: Map<Biome, Float>
) : OreGenerator {

    override val generateMarker = true

    private var biomes = Object2FloatOpenHashMap(biomes)

    private val distribution = (distribution * 1.5).precalculate(WORLD_HEIGHT_RANGE)

    private lateinit var noise: OctaveGenerator

    override fun generate(
        chunk: ChunkSnapshot,
        cx: Int,
        cz: Int,
        random: Random,
        setBlock: (Int, Int, Int) -> Unit
    ) {
        if (!::noise.isInitialized) {
            noise = SimplexOctaveGenerator(random.nextLong(), 8)
            noise.setScale(1.0 / size)
        }

        for (x in 0..15) {
            for (z in 0..15) {
                val value = noise.noise(
                    (cx + x).toDouble(),
                    (cz + z).toDouble(),
                    1.0,
                    0.01,
                    true
                ).coerceAtLeast(0.0).pow(9).toFloat()
                for (y in -64..chunk.getHighestBlockYAt(x, z)) {
                    val type = chunk.getBlockType(x, y, z)
                    if (type.isAir) continue
                    val chance = distribution[y.toDouble()] * value
                    if (random.nextFloat() < chance) {
                        // getting a biome is really expensive for some reason,
                        // so we only get it if the first check passes
                        // random() < a && random() < b is equivalent to random() < a * b
                        val biome = chunk.getBiome(x, y, z)
                        if (random.nextFloat() < biomes.getFloat(biome)) {
                            setBlock(x, y, z)
                        }
                    }
                }
            }
        }
    }
}

private val WORLD_HEIGHT_RANGE = -64..320