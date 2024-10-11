package io.github.seggan.prospecting.registries

import io.github.seggan.prospecting.gen.distribution.Distribution
import io.github.seggan.prospecting.gen.distribution.NormalDistribution
import io.github.seggan.prospecting.gen.distribution.div
import io.github.seggan.prospecting.util.size
import io.github.seggan.prospecting.util.subscript
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils
import it.unimi.dsi.fastutil.objects.Object2FloatMap
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap
import me.mrCookieSlime.Slimefun.api.BlockStorage
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.RegionAccessor
import org.bukkit.block.Biome

enum class Ore(
    private val metal: Metal,
    private val pebbleMaterial: Material,
    asciiFormula: String,
    blockDistribution: Distribution,
    sandDistribution: Distribution,
    gravelDistribution: Distribution,
    val biomeDistribution: Object2FloatMap<Biome>
) {
    // Iron ores
    LIMONITE(
        metal = Metal.IRON,
        pebbleMaterial = Material.SPRUCE_BUTTON,
        asciiFormula = "FeO(OH)",
        blockDistribution = Distribution.NONE,
        sandDistribution = Distribution.NONE,
        gravelDistribution = NormalDistribution(66.0, 1.0) / 100.0,
        biomeDistribution = object2FloatMapOf(
            Biome.SWAMP to 1f,
            Biome.MANGROVE_SWAMP to 1f,
            Biome.RIVER to 0.7f,
            Biome.FROZEN_RIVER to 0.7f,
            Biome.OCEAN to 0.5f,
            Biome.COLD_OCEAN to 0.7f,
            Biome.WARM_OCEAN to 0.5f,
        )
    ),
    ;

    val oreName = ChatUtils.humanize(name)
    val formula = asciiFormula.subscript()

    private val oreId = "PROSPECTING_ORE_$name"
    val oreItem = SlimefunItemStack(
        oreId,
        metal.vanillaOre,
        "&4$oreName",
        "",
        "&aFormula: $formula"
    )

    private val pebbleId = "PROSPECTING_PEBBLE_$name"
    val pebbleItem = SlimefunItemStack(
        pebbleId,
        pebbleMaterial,
        "&f$oreName pebble",
        "",
        "&7A pebble of ${oreName.lowercase()}",
        "&aFormula: $formula"
    )

    private val crushedId = "PROSPECTING_CRUSHED_ORE_$name"
    val crushedItem = SlimefunItemStack(
        crushedId,
        Material.GUNPOWDER,
        "&fCrushed $oreName",
        "",
        "&aFormula: $formula"
    )

    val blockDistribution = blockDistribution.precalculate(WORLD_HEIGHT_RANGE)
    val sandDistribution = sandDistribution.precalculate(WORLD_HEIGHT_RANGE)
    val gravelDistribution = gravelDistribution.precalculate(WORLD_HEIGHT_RANGE)

    fun placeOre(region: RegionAccessor, location: Location) {
        region.setType(location, metal.vanillaOre)
        BlockStorage.addBlockInfo(location, "id", oreId)
    }

    fun placePebble(region: RegionAccessor, location: Location) {
        region.setType(location, pebbleMaterial)
        BlockStorage.addBlockInfo(location, "id", pebbleId)
    }
}

private val WORLD_HEIGHT_RANGE = -64..320

private fun Distribution.precalculate(range: IntRange): FloatArray {
    return FloatArray(range.size) { get(range.first + it.toDouble()).toFloat() }
}

private fun <T> object2FloatMapOf(vararg pairs: Pair<T, Float>): Object2FloatMap<T> {
    return Object2FloatOpenHashMap<T>().apply {
        for ((a, b) in pairs) {
            put(a, b)
        }
    }
}