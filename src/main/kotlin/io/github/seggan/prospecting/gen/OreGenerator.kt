package io.github.seggan.prospecting.gen

import io.github.seggan.prospecting.registries.Ore
import io.github.seggan.prospecting.util.getValue
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap
import org.bukkit.util.noise.SimplexOctaveGenerator
import java.util.Random
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch

class OreGenerator(initRandom: Random) {

    private val random by ThreadLocal.withInitial { Random() }

    private val noise = Ore.entries.associateWith {
        val noise = SimplexOctaveGenerator(initRandom.nextLong(), 8)
        noise.setScale(1.0 / 64.0)
        noise
    }

    private val chunkMap = ConcurrentHashMap<ChunkPosition, OreChunk>()

    private val synchronizers = ConcurrentHashMap<ChunkPosition, CountDownLatch>()

    fun generate(seed: Long, position: ChunkPosition, yRange: IntRange) {
        random.setSeed(seed)
        val synchronizer = CountDownLatch(1)
        synchronizers[position] = synchronizer

        val chunkX = position.blockX
        val chunkZ = position.blockZ

        val chunk = OreChunk(yRange)

        for (cx in 0..15) {
            val x = chunkX + cx
            for (cz in 0..15) {
                val z = chunkZ + cz

                val areaChances = Object2DoubleOpenHashMap<Ore>()
                for (ore in Ore.entries) {
                    val noise = noise[ore]!!
                    var value = noise.noise(x.toDouble(), z.toDouble(), 0.5, 0.5, true)
                    value *= value
                    areaChances.put(ore, value.coerceAtLeast(0.0) / 2)
                }

                for (y in yRange) {
                    for (ore in Ore.entries) {
                        val chance = areaChances.getDouble(ore)
                        if (random.nextFloat() > chance) continue

                        if (random.nextFloat() > ore.gravelDistribution[y + 64]) {
                            chunk.setGravelOre(cx, y, cz, ore)
                        } else if (random.nextFloat() > ore.sandDistribution[y + 64]) {
                            chunk.setSandOre(cx, y, cz, ore)
                        } else if (random.nextFloat() > ore.blockDistribution[y + 64]) {
                            chunk.setBlockOre(cx, y, cz, ore)
                        } else {
                            continue
                        }
                        break
                    }
                }
            }
        }

        chunkMap[position] = chunk
        synchronizer.countDown()
    }

    fun getChunk(position: ChunkPosition): OreChunk {
        val synchronizer = synchronizers[position]!!
        synchronizer.await()
        return chunkMap[position]!!
    }
}