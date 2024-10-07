package io.github.seggan.prospecting.registries

import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.seggan.prospecting.Prospecting
import io.github.seggan.prospecting.gen.Distribution
import io.github.seggan.prospecting.util.randomizedSetOf
import io.github.seggan.prospecting.util.subscript
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.RandomizedSet
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils
import me.mrCookieSlime.Slimefun.api.BlockStorage
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.RegionAccessor
import org.bukkit.inventory.ItemStack
import java.util.Random
import kotlin.random.asKotlinRandom

enum class Ore(
    private val metal: Metal,
    private val pebbleMaterial: Material,
    formula: String,
    private val crushResult: RandomizedSet<ItemStack>,
    private val crushTries: IntRange,
    private val blockDistribution: Distribution,
    private val sandDistribution: Distribution,
    private val gravelDistribution: Distribution
) {
    // Iron ores
    GOETHITE(
        metal = Metal.IRON,
        pebbleMaterial = Material.SPRUCE_BUTTON,
        formula = "FeO(OH)",
        crushResult = randomizedSetOf(ProspectingItems.IRON_OXIDE),
        crushTries = 1..1,
        blockDistribution = Distribution.NONE,
        sandDistribution = Distribution.NONE,
        gravelDistribution = Distribution.NONE
    ),
    ;

    private val oreId = "PROSPECTING_ORE_$name"
    val oreItem = SlimefunItemStack(
        oreId,
        metal.vanillaOre,
        "&4${ChatUtils.humanize(name)}",
        "&7" + formula.subscript()
    )

    private val pebbleId = "PROSPECTING_PEBBLE_$name"
    val pebbleItem = SlimefunItemStack(
        pebbleId,
        pebbleMaterial,
        "&f${ChatUtils.humanize(name)} pebble",
        "&7A pebble of ${ChatUtils.humanize(name).lowercase()}",
        "&7" + formula.subscript()
    )

    fun placeOre(region: RegionAccessor, location: Location) {
        region.setType(location, metal.vanillaOre)
        BlockStorage.addBlockInfo(location, "id", oreId)
    }

    fun placePebble(region: RegionAccessor, location: Location) {
        region.setType(location, pebbleMaterial)
        BlockStorage.addBlockInfo(location, "id", pebbleId)
    }

    fun getCrushResult(random: Random, fortune: Int): Set<ItemStack> {
        return crushResult.getRandomSubset(random, crushTries.random(random.asKotlinRandom()) + fortune)
    }
}