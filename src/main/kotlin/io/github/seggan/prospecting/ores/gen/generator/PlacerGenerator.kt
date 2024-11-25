package io.github.seggan.prospecting.ores.gen.generator

import io.github.seggan.prospecting.registries.BiomeTag
import io.github.seggan.prospecting.util.getHighestOpaqueBlockY
import io.github.seggan.sf4k.extensions.plus
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import org.bukkit.ChunkSnapshot
import org.bukkit.block.Biome
import org.bukkit.util.BlockVector
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.abs
import kotlin.math.max
import kotlin.random.Random
import kotlin.random.nextInt

@Serializable
class PlacerGenerator(private val chance: Float, private val size: Int, private val density: Float) : OreGenerator {

    override val generateMarker = false

    @Transient
    private val instanceSeed = instanceNum++

    override fun generate(
        seed: Long,
        chunk: ChunkSnapshot,
        cx: Int,
        cz: Int,
        random: Random,
        setBlock: OreGenerator.OreSetter
    ) {
        val chunkRandom = Random(longArrayOf(seed, instanceSeed, cx.toLong(), cz.toLong()).contentHashCode())
        if (chunkRandom.nextFloat() > chance) return
        val vein = floodVein(size, density)
        val furthest = vein.maxOf { max(abs(it.blockX), abs(it.blockZ)) }
        val center = if (furthest >= 8) {
            BlockVector(8, 0, 8)
        } else {
            BlockVector(random.nextInt(8 - furthest, 8 + furthest), 0, random.nextInt(8 - furthest, 8 + furthest))
        }
        val highest = chunk.getHighestOpaqueBlockY(center.blockX, center.blockZ)
        center.y = highest.toDouble() - random.nextInt(1..5)
        val type = when (chunk.getBiome(center.blockX, center.blockY, center.blockZ)) {
            in VALID_BIOMES_SAND -> OreGenerator.OreType.SAND
            in VALID_BIOMES_GRAVEL -> OreGenerator.OreType.GRAVEL
            else -> return
        }
        for (block in vein) {
            val x = center.blockX + block.blockX
            val y = center.blockY + block.blockY
            val z = center.blockZ + block.blockZ
            if (x in 0..15 && z in 0..15 && chunk.getBlockType(x, y, z).isSolid) {
                setBlock(x, y, z, type)
            }
        }
    }

    companion object {
        private var instanceNum = 0L
    }
}

private fun floodVein(size: Int, density: Float): Set<BlockVector> {
    val visited = mutableSetOf<BlockVector>()
    val next = ArrayDeque<BlockVector>()
    next.addLast(BlockVector(0, 0, 0))
    while (next.isNotEmpty()) {
        val current = next.removeFirst()
        visited.add(current)
        for (adj in ADJACENT) {
            val nextBlock = current + adj
            if (nextBlock in visited) continue
            if (ThreadLocalRandom.current().nextFloat() > density) continue
            if (visited.size >= size) return visited
            next.addLast(nextBlock.toBlockVector())
        }
    }
    return visited
}

private val ADJACENT = arrayOf(
    BlockVector(1, 0, 0),
    BlockVector(-1, 0, 0),
    BlockVector(0, 1, 0),
    BlockVector(0, -1, 0),
    BlockVector(0, 0, 1),
    BlockVector(0, 0, -1)
)

private val VALID_BIOMES_SAND = BiomeTag.BADLANDS.values + BiomeTag.BEACHES.values + Biome.DESERT

private val VALID_BIOMES_GRAVEL = BiomeTag.RIVERS.values + BiomeTag.MOUNTAINS.values + BiomeTag.WINDSWEPT_HILLS.values