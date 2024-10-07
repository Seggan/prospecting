package io.github.seggan.prospecting.gen

import org.bukkit.World
import org.bukkit.generator.BlockPopulator
import org.bukkit.generator.ChunkGenerator

object ProspectingGenerator : ChunkGenerator() {

    override fun shouldGenerateNoise(): Boolean = true
    override fun shouldGenerateSurface(): Boolean = true
    override fun shouldGenerateCaves(): Boolean = true
    override fun shouldGenerateDecorations(): Boolean = true
    override fun shouldGenerateMobs(): Boolean = true
    override fun shouldGenerateStructures(): Boolean = true

    override fun getDefaultPopulators(world: World): MutableList<BlockPopulator> {
        return mutableListOf(ProspectingPopulator)
    }
}