package io.github.seggan.prospecting.gen

import io.github.seggan.prospecting.registries.Ore
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BrushableBlock
import org.bukkit.generator.BlockPopulator
import org.bukkit.generator.LimitedRegion
import org.bukkit.generator.WorldInfo
import org.bukkit.util.noise.OctaveGenerator
import java.util.EnumSet
import java.util.Random

class ProspectingPopulator(private val generator: OreGenerator) : BlockPopulator() {

    private lateinit var noise: Map<Ore, OctaveGenerator>

    override fun populate(
        worldInfo: WorldInfo,
        random: Random,
        chunkX: Int,
        chunkZ: Int,
        region: LimitedRegion
    ) {
        val xc = chunkX shl 4
        val zc = chunkZ shl 4
        val oreChunk = generator.getChunk(ChunkPosition(chunkX, chunkZ))
        for (cx in 0 until 16) {
            val x = xc + cx
            for (cz in 0 until 16) {
                val z = zc + cz
                for (y in worldInfo.minHeight until worldInfo.maxHeight) {
                    val location = Location(region.world, x.toDouble(), y.toDouble(), z.toDouble())
                    val oldType = region.getType(location)
                    if (!oldType.isSolid) continue // ignore air n stuff
                    val type = oldType.replaceOre()
                    region.setType(location, type)

                    val ore = oreChunk.getGravelOre(cx, y, cz)
                    if (ore != null && type in gravelReplaceable) {
                        val biomeWeight = ore.biomeDistribution.getFloat(region.getBiome(location))
                        if (random.nextFloat() > biomeWeight) continue
                        region.setType(location, Material.SUSPICIOUS_GRAVEL)
                        val state = region.getBlockState(location) as BrushableBlock
                        state.setItem(ore.oreItem.clone())
                        state.update(true, false)
                        continue
                    }
                }
            }
        }
    }
}

private val gravelReplaceable = EnumSet.of(Material.GRAVEL, Material.DIRT)

private fun Material.replaceOre(): Material {
    return when (this) {
        Material.IRON_ORE -> Material.STONE
        Material.DEEPSLATE_IRON_ORE -> Material.DEEPSLATE
        Material.COPPER_ORE -> Material.STONE
        Material.DEEPSLATE_COPPER_ORE -> Material.DEEPSLATE
        Material.GOLD_ORE -> Material.STONE
        Material.DEEPSLATE_GOLD_ORE -> Material.DEEPSLATE
        Material.COAL_ORE -> Material.STONE
        Material.DEEPSLATE_COAL_ORE -> Material.DEEPSLATE
        else -> this
    }
}