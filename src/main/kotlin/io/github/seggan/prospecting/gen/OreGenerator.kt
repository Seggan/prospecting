package io.github.seggan.prospecting.gen

import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.seggan.prospecting.Prospecting
import io.github.seggan.prospecting.items.Pebble
import io.github.seggan.prospecting.registries.Ore
import io.github.seggan.prospecting.registries.ProspectingItems
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import me.mrCookieSlime.Slimefun.api.BlockStorage
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.ChunkSnapshot
import org.bukkit.Material
import org.bukkit.block.BrushableBlock
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.util.noise.OctaveGenerator
import org.bukkit.util.noise.SimplexOctaveGenerator
import java.util.EnumMap
import java.util.EnumSet
import java.util.Random
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlin.math.pow

class OreGenerator(private val worlds: Set<String>) : Listener {

    @Volatile
    private lateinit var noise: ConcurrentMap<Ore, OctaveGenerator>

    @Volatile
    private lateinit var random: Random

    init {
        Bukkit.getPluginManager().registerEvents(this, Prospecting)
    }

    @EventHandler
    private fun onChunkLoad(e: ChunkLoadEvent) {
        if (e.world.name in worlds && e.isNewChunk) {
            if (!::noise.isInitialized) {
                random = Random(e.world.seed)
                noise = Ore.entries.associateWithTo(ConcurrentHashMap()) {
                    val noise = SimplexOctaveGenerator(random.nextLong(), 8)
                    noise.setScale(1 / 64.0)
                    noise
                }
            }
            val chunk = e.chunk
            val snapshot = chunk.getChunkSnapshot(true, true, false)
            val minHeight = chunk.world.minHeight
            Prospecting.launch(Dispatchers.Default) {
                generate(chunk, snapshot, minHeight)
            }
        }
    }

    private suspend fun generate(chunk: Chunk, snapshot: ChunkSnapshot, minHeight: Int) = coroutineScope {
        val chunkX = chunk.x shl 4
        val chunkZ = chunk.z shl 4

        for (x in 0..15) {
            for (z in 0..15) {
                val areaChances = Object2DoubleOpenHashMap<Ore>()
                for (ore in Ore.entries) {
                    val noise = noise[ore]!!
                    val value = noise.noise(
                        (chunkX + x).toDouble(),
                        (chunkZ + z).toDouble(),
                        1.0,
                        0.01,
                        true
                    )
                    areaChances.put(ore, value.coerceAtLeast(0.0).pow(9))
                }

                val markers = mutableMapOf<Ore, Float>()

                for (y in minHeight..snapshot.getHighestBlockYAt(x, z)) {
                    val type = snapshot.getBlockType(x, y, z)
                    if (!type.isOccluding) continue
                    val biome = snapshot.getBiome(x, y, z)
                    var replaceVanilla = true
                    for (ore in Ore.entries) {
                        val chance = areaChances.getDouble(ore).toFloat() *
                                ore.biomeDistribution.getFloat(biome) *
                                ore.distribution[y.toDouble()]
                        if (type in stoneReplaceable && random.nextFloat() < chance ) {
                            Prospecting.launch {
                                val block = chunk.getBlock(x, y, z)
                                block.setType(
                                    if (type == Material.DEEPSLATE) ore.deepslateVanillaOre else ore.vanillaOre,
                                    false
                                )
                                BlockStorage.addBlockInfo(block, "id", ore.oreId)
                            }
                            markers.merge(ore, 0.01f, Float::plus)
                            replaceVanilla = false
                            break
                        }
                    }
                    if (replaceVanilla) {
                        val replace = replaceOres[type]
                        if (replace != null) {
                            Prospecting.launch {
                                chunk.getBlock(x, y, z).setType(replace, false)
                            }
                        }
                    }
                }

                if (random.nextFloat() < 0.003) {
                    val y = snapshot.getHighestOpaqueBlockY(x, z, snapshot.getHighestBlockYAt(x, z)) + 1
                    if (!snapshot.getBlockType(x, y, z).isLiquid) {
                        Prospecting.launch {
                            stonePebble.place(chunk.getBlock(x, y, z))
                        }
                    }
                }

                for ((ore, chance) in markers) {
                    if (random.nextFloat() < chance) {
                        val yBelow = snapshot.getHighestOpaqueBlockY(x, z, snapshot.getHighestBlockYAt(x, z))
                        val y = yBelow + 1
                        if (!snapshot.getBlockType(x, y, z).isLiquid) {
                            Prospecting.launch {
                                ore.pebble.place(chunk.getBlock(x, y, z))
                            }
                            break
                        } else if (snapshot.getBlockType(x, y, z) == Material.WATER) {
                            val belowType = snapshot.getBlockType(x, yBelow, z)
                            val replaceType = when (belowType) {
                                in sandReplaceable -> Material.SUSPICIOUS_SAND
                                in gravelReplaceable -> Material.SUSPICIOUS_GRAVEL
                                else -> null
                            }
                            if (replaceType != null) {
                                Prospecting.launch {
                                    val block = chunk.getBlock(x, yBelow, z)
                                    block.setType(replaceType, false)
                                    val state = block.state as BrushableBlock
                                    state.setItem(ore.oreItem.clone())
                                    state.update(true, false)
                                }
                                break
                            }
                        }
                    }
                }
            }
        }
    }
}

private tailrec fun ChunkSnapshot.getHighestOpaqueBlockY(x: Int, z: Int, y: Int): Int {
    return if (y < -64 || getBlockType(x, y, z).isOccluding) y else getHighestOpaqueBlockY(x, z, y - 1)
}

private val gravelReplaceable = EnumSet.of(Material.GRAVEL, Material.DIRT)
private val sandReplaceable = EnumSet.of(Material.SAND, Material.RED_SAND, Material.CLAY)
private val stoneReplaceable = EnumSet.of(
    Material.STONE,
    Material.DEEPSLATE,
    Material.ANDESITE,
    Material.DIORITE,
    Material.GRANITE
)

private val replaceOres = EnumMap<Material, Material>(Material::class.java).apply {
    put(Material.IRON_ORE, Material.STONE)
    put(Material.DEEPSLATE_IRON_ORE, Material.DEEPSLATE)
    put(Material.COPPER_ORE, Material.STONE)
    put(Material.DEEPSLATE_COPPER_ORE, Material.DEEPSLATE)
    put(Material.GOLD_ORE, Material.STONE)
    put(Material.DEEPSLATE_GOLD_ORE, Material.DEEPSLATE)
    put(Material.COAL_ORE, Material.STONE)
    put(Material.DEEPSLATE_COAL_ORE, Material.DEEPSLATE)
}

private val Material.isLiquid: Boolean
    get() = this == Material.WATER || this == Material.LAVA

private val stonePebble by lazy { SlimefunItem.getById(ProspectingItems.STONE_PEBBLE.itemId) as Pebble }