package io.github.seggan.prospecting.gen

data class ChunkPosition(val x: Int, val z: Int) {
    val blockX = x shl 4
    val blockZ = z shl 4
}
