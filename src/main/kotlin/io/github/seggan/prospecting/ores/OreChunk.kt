package io.github.seggan.prospecting.ores

import io.github.seggan.prospecting.registries.Ore
import org.bukkit.NamespacedKey
import org.bukkit.generator.WorldInfo
import org.msgpack.core.MessagePack
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

class OreChunk private constructor(private var data: ConcurrentMap<ChunkBlockPosition, Ore>) {

    operator fun get(x: Int, y: Int, z: Int): Ore? {
        return data[ChunkBlockPosition(x, y, z)]
    }

    operator fun set(x: Int, y: Int, z: Int, ore: Ore?) {
        if (ore == null) {
            data.remove(ChunkBlockPosition(x, y, z))
        } else {
            data[ChunkBlockPosition(x, y, z)] = ore
        }
    }

    fun save(): ByteArray {
        val palette = data.values.toSet().toList()
        val packer = MessagePack.newDefaultBufferPacker()

        packer.packArrayHeader(palette.size)
        for (ore in palette) {
            packer.packString(ore.id.toString())
        }
        for ((position, ore) in data) {
            val index = palette.indexOf(ore)
            val xzByte = (position.x shl 4) or (position.z)
            packer.packInt(index)
            packer.packInt(xzByte)
            packer.packInt(position.y)
        }

        return packer.toByteArray()
    }

    companion object {

        fun load(data: ByteArray): OreChunk {
            val unpacker = MessagePack.newDefaultUnpacker(data)

            val paletteSize = unpacker.unpackArrayHeader()
            val palette = ArrayList<Ore>(paletteSize)
            repeat(paletteSize) {
                val id = unpacker.unpackString()
                palette.add(Ore.getById(NamespacedKey.fromString(id)!!)!!)
            }
            val ores = ConcurrentHashMap<ChunkBlockPosition, Ore>()
            while (unpacker.hasNext()) {
                val index = unpacker.unpackInt()
                val xzByte = unpacker.unpackInt()
                val y = unpacker.unpackInt()
                val x = xzByte shr 4
                val z = xzByte and 0xF
                ores[ChunkBlockPosition(x, y, z)] = palette[index]
            }

            return OreChunk(ores)
        }

        fun empty(world: WorldInfo): OreChunk {
            return OreChunk(ConcurrentHashMap())
        }
    }
}

private data class ChunkBlockPosition(val x: Int, val y: Int, val z: Int)