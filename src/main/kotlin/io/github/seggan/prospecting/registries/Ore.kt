package io.github.seggan.prospecting.registries

import io.github.seggan.prospecting.gen.distribution.Distribution
import io.github.seggan.prospecting.gen.distribution.NormalDistribution
import io.github.seggan.prospecting.gen.distribution.precalculate
import io.github.seggan.prospecting.items.Pebble
import io.github.seggan.prospecting.util.subscript
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils
import it.unimi.dsi.fastutil.objects.Object2FloatMap
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap
import me.mrCookieSlime.Slimefun.api.BlockStorage
import org.bukkit.Material
import org.bukkit.block.Biome
import org.bukkit.block.Block

enum class Ore(
    private val metal: Metal,
    private val pebbleMaterial: Material,
    asciiFormula: String,
    blockDistribution: Distribution,
    val biomeDistribution: Object2FloatMap<Biome>,
    surfaceDistribution: Distribution = Distribution.NONE,
    private val vanillaOre: Material = metal.vanillaOre
) {
    // Iron ores
    LIMONITE(
        metal = Metal.IRON,
        pebbleMaterial = Material.SPRUCE_BUTTON,
        asciiFormula = "FeO(OH)",
        blockDistribution = NormalDistribution(50.0, 1.0),
        surfaceDistribution = NormalDistribution(66.0, 3.0),
        biomeDistribution = biomeDistribution {
            put(Biome.SWAMP, 1f)
            put(Biome.MANGROVE_SWAMP, 1f)
            put(Biome.BEACH, 0.7f)
            put(Biome.RIVER, 0.7f)
            put(Biome.FROZEN_RIVER, 0.7f)
            put(Biome.OCEAN, 0.5f)
            put(Biome.COLD_OCEAN, 0.7f)
            put(Biome.WARM_OCEAN, 0.5f)
            put(Biome.FROZEN_OCEAN, 0.7f)
        }
    ),

    // Copper ores
    AZURITE(
        metal = Metal.COPPER,
        vanillaOre = Material.LAPIS_ORE,
        pebbleMaterial = Material.WARPED_BUTTON,
        asciiFormula = "Cu3(CO3)2(OH)2",
        blockDistribution = NormalDistribution(30.0, 1.5),
        biomeDistribution = biomeDistribution {
            for (biome in Biome.entries) {
                put(biome, 1f)
            }
            put(Biome.SNOWY_SLOPES, 0.3f)
            put(Biome.FROZEN_PEAKS, 0.3f)
            put(Biome.JAGGED_PEAKS, 0.3f)
            put(Biome.STONY_PEAKS, 0.3f)
            put(Biome.STONY_SHORE, 0.3f)
            put(Biome.DRIPSTONE_CAVES, 1.8f)
        }
    )
    ;

    init {
        require("BUTTON" in pebbleMaterial.name) {
            "Pebble material for $name must be a button, got $pebbleMaterial"
        }
    }

    val oreName = ChatUtils.humanize(name)
    val formula = asciiFormula.subscript()

    private val deepslateVanillaOre = Material.getMaterial("DEEPSLATE_" + vanillaOre.name)!!

    val oreId = "PROSPECTING_ORE_$name"
    val oreItem = SlimefunItemStack(
        oreId,
        vanillaOre,
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

    val pebble by lazy { SlimefunItem.getById(pebbleId) as Pebble }

    private val crushedId = "PROSPECTING_CRUSHED_ORE_$name"
    val crushedItem = SlimefunItemStack(
        crushedId,
        Material.GUNPOWDER,
        "&fCrushed $oreName",
        "",
        "&aFormula: $formula"
    )

    val blockDistribution = blockDistribution.precalculate(WORLD_HEIGHT_RANGE)
    val surfaceDistribution = surfaceDistribution.precalculate(WORLD_HEIGHT_RANGE)

    fun placeOre(block: Block, deepslate: Boolean) {
        block.setType(if (deepslate) deepslateVanillaOre else vanillaOre, false)
        BlockStorage.addBlockInfo(block, "id", oreId)
    }
}

private val WORLD_HEIGHT_RANGE = -64..320

private inline fun biomeDistribution(block: MutableMap<Biome, Float>.() -> Unit): Object2FloatMap<Biome> {
    val map = Object2FloatOpenHashMap<Biome>()
    map.block()
    return map
}