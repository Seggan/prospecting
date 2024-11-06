package io.github.seggan.prospecting.ores

import io.github.seggan.prospecting.pluginInstance
import io.github.seggan.prospecting.util.IntPair
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.generator.WorldInfo
import org.msgpack.core.MessagePack
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class OreWorld private constructor(
    private val world: WorldInfo,
    private val chunks: ConcurrentMap<IntPair, OreChunk>
) : Listener {

    init {
        Bukkit.getPluginManager().registerEvents(this, pluginInstance)
    }

    fun getChunk(x: Int, z: Int): OreChunk {
        return chunks.getOrPut(IntPair(x, z)) { OreChunk.empty(world) }
    }

    fun save() {
        val file = folder.resolve("${world.name}.bin")
        val packer = MessagePack.newDefaultBufferPacker()
        for ((pair, chunk) in chunks) {
            packer.packInt(pair.x)
            packer.packInt(pair.z)
            val saved = chunk.save()
            packer.packBinaryHeader(saved.size)
            packer.writePayload(saved)
        }
        file.writeBytes(packer.toByteArray())
    }

    companion object {

        private val worlds = mutableMapOf<UUID, OreWorld>()
        private val folder = pluginInstance.dataFolder.resolve("worlds")

        init {
            folder.mkdirs()
        }

        fun getWorld(world: WorldInfo): OreWorld {
            return worlds.computeIfAbsent(world.uid) { load(world) }
        }

        fun saveAll() {
            for (world in worlds.values) {
                world.save()
            }
        }

        private fun load(world: WorldInfo): OreWorld {
            val file = folder.resolve("${world.name}.bin")
            if (!file.exists()) {
                return OreWorld(world, ConcurrentHashMap())
            }
            val unpacker = MessagePack.newDefaultUnpacker(file.readBytes())
            val chunks = ConcurrentHashMap<IntPair, OreChunk>()
            while (unpacker.hasNext()) {
                val x = unpacker.unpackInt()
                val z = unpacker.unpackInt()
                val data = unpacker.readPayload(unpacker.unpackBinaryHeader())
                chunks[IntPair(x, z)] = OreChunk.load(data)
            }
            return OreWorld(world, chunks)
        }
    }
}