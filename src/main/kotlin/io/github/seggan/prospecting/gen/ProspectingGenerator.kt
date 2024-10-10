package io.github.seggan.prospecting.gen

import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.seggan.prospecting.Prospecting
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import org.bukkit.World
import org.bukkit.generator.BlockPopulator
import org.bukkit.generator.ChunkGenerator
import org.bukkit.generator.WorldInfo
import java.util.Random
import kotlin.time.DurationUnit
import kotlin.time.measureTime

object ProspectingGenerator : ChunkGenerator() {

    private lateinit var generator: OreGenerator

    override fun shouldGenerateNoise(worldInfo: WorldInfo, random: Random, chunkX: Int, chunkZ: Int): Boolean {
        if (!::generator.isInitialized) {
            generator = OreGenerator(Random(worldInfo.seed))
        }
        val handler = CoroutineExceptionHandler { _, ex -> throw ex }
        Prospecting.launch(Dispatchers.Default + handler) {
            println(measureTime {
                generator.generate(
                    random.nextLong(),
                    ChunkPosition(chunkX, chunkZ),
                    worldInfo.minHeight until worldInfo.maxHeight
                )
            }.toDouble(DurationUnit.MILLISECONDS))
        }
        return true
    }

    override fun shouldGenerateSurface(): Boolean = true
    override fun shouldGenerateCaves(): Boolean = true
    override fun shouldGenerateDecorations(): Boolean = true
    override fun shouldGenerateMobs(): Boolean = true
    override fun shouldGenerateStructures(): Boolean = true

    override fun getDefaultPopulators(world: World): MutableList<BlockPopulator> {
        if (!::generator.isInitialized) {
            generator = OreGenerator(Random(world.seed))
        }
        return mutableListOf(ProspectingPopulator(generator))
    }
}