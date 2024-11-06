package io.github.seggan.prospecting.gen

import org.bukkit.ChunkSnapshot
import java.util.Random

interface OreGenerator {

    val generateMarker: Boolean

    fun generate(
        chunk: ChunkSnapshot,
        cx: Int,
        cz: Int,
        random: Random,
        setBlock: (Int, Int, Int) -> Unit
    )
}

operator fun OreGenerator.plus(other: OreGenerator): OreGenerator {
    return object : OreGenerator {
        override val generateMarker = this@plus.generateMarker || other.generateMarker

        override fun generate(
            chunk: ChunkSnapshot,
            cx: Int,
            cz: Int,
            random: Random,
            setBlock: (Int, Int, Int) -> Unit
        ) {
            this@plus.generate(chunk, cx, cz, random, setBlock)
            other.generate(chunk, cx, cz, random, setBlock)
        }
    }
}