package io.github.seggan.prospecting.ore.gen

import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.seggan.prospecting.items.Pebble
import io.github.seggan.prospecting.ore.Ore
import io.github.seggan.prospecting.ore.gen.generator.OreGenerator
import io.github.seggan.prospecting.pluginInstance
import io.github.seggan.prospecting.registries.ProspectingItems
import io.github.seggan.prospecting.util.getHighestOpaqueBlockY
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
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
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlin.random.Random

class OreSpawnerThingy(private val world: String) : Listener {

    private var running = true

    @Volatile
    private lateinit var noise: ConcurrentMap<Ore, OctaveGenerator>

    @Volatile
    private lateinit var random: Random

    init {
        Bukkit.getPluginManager().registerEvents(this, pluginInstance)
    }

    fun disable() {
        running = false
    }

    @EventHandler
    private fun onChunkLoad(e: ChunkLoadEvent) {
        if (running && e.world.name == world && e.isNewChunk) {
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
            pluginInstance.launch(Dispatchers.Default) {
                generate(chunk, snapshot, minHeight)
            }
        }
    }

    private suspend fun generate(chunk: Chunk, snapshot: ChunkSnapshot, minHeight: Int) = coroutineScope {
        fun placeBrushableBlock(type: Material, x: Int, y: Int, z: Int, ore: Ore): Boolean {
            val replaceType = when (type) {
                in sandReplaceable -> Material.SUSPICIOUS_SAND
                in gravelReplaceable -> Material.SUSPICIOUS_GRAVEL
                else -> return false
            }
            pluginInstance.launch {
                val block = chunk.getBlock(x, y, z)
                block.setType(replaceType, false)
                val state = block.state as BrushableBlock
                state.setItem(ore.oreItem.clone())
                state.update(true, false)
            }
            return true
        }

        val chunkX = snapshot.x shl 4
        val chunkZ = snapshot.z shl 4

        for (x in 0..15) {
            for (z in 0..15) {
                for (y in minHeight..snapshot.getHighestBlockYAt(x, z)) {
                    val type = snapshot.getBlockType(x, y, z)
                    val replace = replaceOres[type]
                    if (replace != null) {
                        pluginInstance.launch {
                            chunk.getBlock(x, y, z).setType(replace, false)
                        }
                    }
                }

                if (random.nextFloat() < 0.003) {
                    val y = snapshot.getHighestOpaqueBlockY(x, z, snapshot.getHighestBlockYAt(x, z)) + 1
                    if (!snapshot.getBlockType(x, y, z).isLiquid) {
                        pluginInstance.launch {
                            stonePebble.place(chunk.getBlock(x, y, z))
                        }
                    }
                }
            }
        }

        for (ore in Ore.entries) {
            val generator = ore.generator
            val markers = ConcurrentHashMap<IntPair, Float>()
            generator.generate(chunk.world.seed, snapshot, chunkX, chunkZ, random) placeBlock@{ x, y, z, oreBlock ->
                val type = snapshot.getBlockType(x, y, z)
                var finalOre = ore
                if (random.nextFloat() < 0.1) {
                    finalOre = Ore.associations[ore]?.randomOrNull() ?: ore
                }
                val oreBlockType = oreBlock ?: when (type) {
                    in sandReplaceable -> OreGenerator.OreType.SAND
                    in gravelReplaceable -> OreGenerator.OreType.GRAVEL
                    in stoneReplaceable -> OreGenerator.OreType.BLOCK
                    else -> return@placeBlock
                }
                val material = when (oreBlockType) {
                    OreGenerator.OreType.SAND -> Material.SAND
                    OreGenerator.OreType.GRAVEL -> Material.GRAVEL
                    OreGenerator.OreType.BLOCK -> if (type == Material.STONE) ore.vanillaOre else ore.deepslateVanillaOre
                }
                val brushablePlaced = placeBrushableBlock(material, x, y, z, finalOre)
                if (!brushablePlaced && type in stoneReplaceable) {
                    pluginInstance.launch {
                        val block = chunk.getBlock(x, y, z)
                        block.setType(material, false)
                        BlockStorage.addBlockInfo(block, "id", finalOre.oreId)
                    }
                    markers.merge(IntPair(x, z), 0.01f, Float::plus)
                }
            }

            if (generator.generateMarker) {
                for ((place, markerChance) in markers) {
                    if (random.nextFloat() < markerChance) {
                        val (x, z) = place
                        val yBelow = snapshot.getHighestOpaqueBlockY(x, z)
                        val y = yBelow + 1
                        if (!snapshot.getBlockType(x, y, z).isLiquid) {
                            pluginInstance.launch {
                                ore.pebble.place(chunk.getBlock(x, y, z))
                            }
                        } else if (snapshot.getBlockType(x, y, z) == Material.WATER) {
                            val belowType = snapshot.getBlockType(x, yBelow, z)
                            placeBrushableBlock(belowType, x, yBelow, z, ore)
                        }
                    }
                }
            }
        }
    }
}

private data class IntPair(val x: Int, val z: Int)

private val gravelReplaceable = EnumSet.of(Material.GRAVEL, Material.SUSPICIOUS_GRAVEL, Material.DIRT)
private val sandReplaceable = EnumSet.of(Material.SAND, Material.SUSPICIOUS_SAND, Material.RED_SAND, Material.CLAY)
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