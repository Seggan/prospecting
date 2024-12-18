package io.github.seggan.prospecting.ores.gen.generator

import kotlinx.serialization.Serializable
import org.bukkit.ChunkSnapshot
import org.bukkit.Material
import org.bukkit.block.BlockFace
import kotlin.random.Random

@Serializable
class NearLavaGenerator(private val chance: Float) : OreGenerator {

    override val generateMarker = false

    override fun generate(
        seed: Long,
        chunk: ChunkSnapshot,
        cx: Int,
        cz: Int,
        random: Random,
        setBlock: OreGenerator.OreSetter
    ) {
        for (x in 0..15) {
            for (z in 0..15) {
                for (y in -64..0) {
                    if (random.nextFloat() < chance) {
                        if (chunk.getBlockType(x, y, z) == Material.LAVA) continue
                        for (face in ADJACENT) {
                            val newX = x + face.modX
                            val newY = y + face.modY
                            val newZ = z + face.modZ
                            if (newX !in 0..15 || newZ !in 0..15 || newY < -64) continue
                            if (chunk.getBlockType(newX, newY, newZ) == Material.LAVA) {
                                setBlock(x, y, z)
                                break
                            }
                        }
                    }
                }
            }
        }
    }
}

private val ADJACENT =
    arrayOf(BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST)