package io.github.seggan.prospecting.gen

import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.seggan.prospecting.Prospecting
import io.github.seggan.prospecting.registries.Ore
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
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
                    noise.setScale(1.0 / 128.0)
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
                    var value = noise.noise(
                        (chunkX + x).toDouble(),
                        (chunkZ + z).toDouble(),
                        0.01,
                        0.5,
                        true
                    )
                    value *= value
                    areaChances.put(ore, value.coerceAtLeast(0.0) / 2)
                }

                for (y in minHeight..snapshot.getHighestBlockYAt(x, z)) {
                    val type = snapshot.getBlockType(x, y, z)
                    if (!type.isOccluding) continue
                    val biome = snapshot.getBiome(x, y, z)
                    var replaceVanilla = true
                    for (ore in Ore.entries) {
                        val chance = areaChances.getDouble(ore).toFloat() * ore.biomeDistribution.getFloat(biome)
                        if (type in stoneReplaceable && random.nextFloat() < chance * ore.blockDistribution[y.toDouble()]) {
                            Prospecting.launch {
                                ore.placeOre(chunk.getBlock(x, y, z), type == Material.DEEPSLATE)
                            }
                            replaceVanilla = false
                            break
                        }
                        if (random.nextFloat() < chance * ore.surfaceDistribution[y.toDouble()]) {
                            val material = when (type) {
                                in sandReplaceable -> Material.SUSPICIOUS_SAND
                                in gravelReplaceable -> Material.SUSPICIOUS_GRAVEL
                                else -> null
                            }
                            if (material != null) {
                                Prospecting.launch {
                                    val block = chunk.getBlock(x, y, z)
                                    block.setType(material, false)
                                    val state = block.state as BrushableBlock
                                    state.setItem(ore.oreItem.clone())
                                    state.update(true, false)
                                }
                                replaceVanilla = false
                                break
                            }
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
            }
        }
    }
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