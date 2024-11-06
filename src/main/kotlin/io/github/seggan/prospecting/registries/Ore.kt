package io.github.seggan.prospecting.registries

import io.github.seggan.prospecting.gen.LargeVeinGenerator
import io.github.seggan.prospecting.gen.OreGenerator
import io.github.seggan.prospecting.gen.distribution.NormalDistribution
import io.github.seggan.prospecting.items.Pebble
import io.github.seggan.prospecting.util.randomizedSetOf
import io.github.seggan.prospecting.util.subscript
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.RandomizedSet
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils
import it.unimi.dsi.fastutil.objects.Object2FloatMap
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap
import org.bukkit.Material
import org.bukkit.block.Biome
import org.bukkit.inventory.ItemStack
import java.util.EnumMap

enum class Ore(
    private val pebbleMaterial: Material,
    asciiFormula: String,
    val crushResult: RandomizedSet<ItemStack>,
    val crushAmount: IntRange,
    val generator: OreGenerator,
    val vanillaOre: Material
) {
    // Iron ores
    LIMONITE(
        pebbleMaterial = Material.SPRUCE_BUTTON,
        asciiFormula = "(Fe,Ni)O(OH)",
        crushResult = randomizedSetOf(ProspectingItems.IRON_OXIDE to 3f, ProspectingItems.NICKEL_OXIDE to 1f),
        crushAmount = 2..3,
        generator = LargeVeinGenerator(
            64,
            NormalDistribution(50.0, 2.0),
            biomeDistribution {
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
        vanillaOre = Material.IRON_ORE
    ),

    // Copper ores
    AZURITE(
        pebbleMaterial = Material.WARPED_BUTTON,
        asciiFormula = "Cu3(CO3)2(OH)2",
        generator = LargeVeinGenerator(
            64,
            NormalDistribution(0.0, 1.5),
            biomeDistribution {
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
        ),
        crushResult = randomizedSetOf(ProspectingItems.COPPER_CARBONATE to 1f),
        crushAmount = 2..3,
        vanillaOre = Material.LAPIS_ORE
    ),
    MALACHITE(
        pebbleMaterial = Material.WARPED_BUTTON,
        asciiFormula = "Cu2CO3(OH)2",
        generator = LargeVeinGenerator(
            64,
            NormalDistribution(35.0, 2.0),
            biomeDistribution {
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
        ),
        crushResult = randomizedSetOf(ProspectingItems.COPPER_CARBONATE to 1f),
        crushAmount = 1..2,
        vanillaOre = Material.COPPER_ORE
    ),

    // Tin ores
    CASSITERITE(
        pebbleMaterial = Material.POLISHED_BLACKSTONE_BUTTON,
        asciiFormula = "SnO2",
        generator = LargeVeinGenerator(
            128,
            NormalDistribution(-30.0, 0.9),
            biomeDistribution {
                for (biome in Biome.entries) {
                    if ("OCEAN" in biome.name || "RIVER" in biome.name
                        || biome == Biome.BEACH || biome == Biome.STONY_SHORE
                    ) {
                        put(biome, 1f)
                    }
                }
            }
        ),
        crushResult = randomizedSetOf(ProspectingItems.TIN_OXIDE to 1f),
        crushAmount = 1..2,
        vanillaOre = Material.COAL_ORE
    )
    ;

    init {
        require("BUTTON" in pebbleMaterial.name) {
            "Pebble material for $name must be a button, got $pebbleMaterial"
        }
    }

    val oreName = ChatUtils.humanize(name)
    val formula = asciiFormula.subscript()

    val deepslateVanillaOre = Material.getMaterial("DEEPSLATE_" + vanillaOre.name)!!

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

    companion object {
        private val byId = entries.associateBy { it.oreId } + entries.associateBy { it.pebbleId }
        fun getById(id: String): Ore? = byId[id]

        val associations = EnumMap<Ore, RandomizedSet<Ore>>(Ore::class.java).apply {
            put(AZURITE, randomizedSetOf(MALACHITE to 1f))
            put(MALACHITE, randomizedSetOf(AZURITE to 1f))
        }
    }

    fun register(addon: SlimefunAddon) {
        SlimefunItem(
            ProspectingCategories.ORES,
            oreItem,
            ProspectingRecipeTypes.NATURALLY_GENERATED,
            emptyArray()
        ).register(addon)
        Pebble(
            ProspectingCategories.ORES,
            pebbleItem,
            ProspectingRecipeTypes.NATURALLY_GENERATED,
            emptyArray()
        ).register(addon)
    }
}

private inline fun biomeDistribution(block: MutableMap<Biome, Float>.() -> Unit): Object2FloatMap<Biome> {
    val map = Object2FloatOpenHashMap<Biome>()
    map.block()
    return map
}