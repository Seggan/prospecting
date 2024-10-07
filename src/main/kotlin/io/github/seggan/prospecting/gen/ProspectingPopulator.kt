package io.github.seggan.prospecting.gen

import io.github.seggan.prospecting.Prospecting
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.generator.BlockPopulator
import org.bukkit.generator.LimitedRegion
import org.bukkit.generator.WorldInfo
import java.util.Random
import java.util.logging.Level
import kotlin.system.measureTimeMillis

object ProspectingPopulator : BlockPopulator() {
    override fun populate(
        worldInfo: WorldInfo,
        random: Random,
        chunkX: Int,
        chunkZ: Int,
        region: LimitedRegion
    ) {
        val xc = chunkX shl 4
        val zc = chunkZ shl 4
        for (cx in 0 until 17) {
            val x = xc + cx
            for (cz in 0 until 17) {
                val z = zc + cz
                for (y in worldInfo.minHeight until worldInfo.maxHeight) {
                    val location = Location(region.world, x.toDouble(), y.toDouble(), z.toDouble())
                    val originalType = region.getType(location)
                    if (!originalType.isSolid) continue // ignore air n stuff
                    region.setType(location, originalType.replaceOre())
                }
            }
        }
    }
}

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