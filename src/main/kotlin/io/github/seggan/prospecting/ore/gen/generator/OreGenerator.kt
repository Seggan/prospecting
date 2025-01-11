package io.github.seggan.prospecting.ore.gen.generator

import org.bukkit.ChunkSnapshot
import kotlin.random.Random

interface OreGenerator {

    val generateMarker: Boolean

    fun generate(
        seed: Long,
        chunk: ChunkSnapshot,
        cx: Int,
        cz: Int,
        random: Random,
        setBlock: OreSetter
    )

    enum class OreType {
        SAND,
        GRAVEL,
        BLOCK
    }

    fun interface OreSetter {
        operator fun invoke(x: Int, y: Int, z: Int, type: OreType?)

        operator fun invoke(x: Int, y: Int, z: Int) {
            invoke(x, y, z, null)
        }
    }
}

operator fun OreGenerator.plus(other: OreGenerator): OreGenerator {
    return object : OreGenerator {
        override val generateMarker = this@plus.generateMarker || other.generateMarker

        override fun generate(
            seed: Long,
            chunk: ChunkSnapshot,
            cx: Int,
            cz: Int,
            random: Random,
            setBlock: OreGenerator.OreSetter
        ) {
            this@plus.generate(seed, chunk, cx, cz, random, setBlock)
            other.generate(seed, chunk, cx, cz, random, setBlock)
        }
    }
}