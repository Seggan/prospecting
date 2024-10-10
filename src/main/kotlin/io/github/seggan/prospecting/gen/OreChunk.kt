package io.github.seggan.prospecting.gen

import io.github.seggan.prospecting.registries.Ore
import io.github.seggan.prospecting.util.size

class OreChunk(private val yRange: IntRange) {

    private val blockOres = Array(16) { Array(yRange.size) { Array<Ore?>(16) { null } } }
    private val sandOres = Array(16) { Array(yRange.size) { Array<Ore?>(16) { null } } }
    private val gravelOres = Array(16) { Array(yRange.size) { Array<Ore?>(16) { null } } }

    fun getBlockOre(x: Int, y: Int, z: Int): Ore? = blockOres[x][y - yRange.first][z]

    fun getSandOre(x: Int, y: Int, z: Int): Ore? = sandOres[x][y - yRange.first][z]

    fun getGravelOre(x: Int, y: Int, z: Int): Ore? = gravelOres[x][y - yRange.first][z]

    fun setBlockOre(x: Int, y: Int, z: Int, ore: Ore) {
        blockOres[x][y - yRange.first][z] = ore
    }

    fun setSandOre(x: Int, y: Int, z: Int, ore: Ore) {
        sandOres[x][y - yRange.first][z] = ore
    }

    fun setGravelOre(x: Int, y: Int, z: Int, ore: Ore) {
        gravelOres[x][y - yRange.first][z] = ore
    }
}