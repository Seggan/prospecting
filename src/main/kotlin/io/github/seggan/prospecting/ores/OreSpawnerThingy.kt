package io.github.seggan.prospecting.ores

import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.seggan.prospecting.items.Pebble
import io.github.seggan.prospecting.pluginInstance
import io.github.seggan.prospecting.registries.Ore
import io.github.seggan.prospecting.registries.ProspectingItems
import io.github.seggan.prospecting.util.IntPair
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap
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
import org.bukkit.generator.WorldInfo
import org.bukkit.util.noise.OctaveGenerator
import org.bukkit.util.noise.SimplexOctaveGenerator
import java.util.EnumMap
import java.util.EnumSet
import java.util.Random
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class OreSpawnerThingy(private val worlds: Set<String>) : Listener {

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
        if (running && e.world.name in worlds && e.isNewChunk) {
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
            pluginInstance.launch(Dispatchers.Default) {
                generate(chunk, snapshot, chunk.world)
            }
        }
    }

    private suspend fun generate(chunk: Chunk, snapshot: ChunkSnapshot, world: WorldInfo) = coroutineScope {
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
        val oreChunk = OreWorld.getWorld(world).getChunk(snapshot.x, snapshot.z)

        for (x in 0..15) {
            for (z in 0..15) {
                for (y in world.minHeight..snapshot.getHighestBlockYAt(x, z)) {
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
            val markers = Object2FloatOpenHashMap<IntPair>()
            generator.generate(snapshot, chunkX, chunkZ, random) { x, y, z ->
                val type = snapshot.getBlockType(x, y, z)
                var finalOre = ore
                if (random.nextFloat() < 0.1) {
                    finalOre = Ore.associations[ore]?.getRandom(random) ?: ore
                }
                val brushablePlaced = placeBrushableBlock(type, x, y, z, finalOre)
                if (!brushablePlaced && type in stoneReplaceable) {
                    val material =
                        if (type == Material.DEEPSLATE) finalOre.deepslateVanillaOre
                        else finalOre.vanillaOre
                    pluginInstance.launch {
                        val block = chunk.getBlock(x, y, z)
                        block.setType(material, false)
                    }
                    oreChunk[x, y, z] = finalOre
                    markers.mergeFloat(IntPair(x, z), 0.01f, Float::plus)
                }
            }

            if (generator.generateMarker) {
                for ((place, markerChance) in markers.object2FloatEntrySet()) {
                    if (random.nextFloat() < markerChance) {
                        val (x, z) = place
                        val yBelow = snapshot.getHighestOpaqueBlockY(x, z, snapshot.getHighestBlockYAt(x, z))
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