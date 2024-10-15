package io.github.seggan.prospecting.gen

import io.github.seggan.prospecting.registries.Ore
import io.github.seggan.prospecting.util.size
import java.util.EnumMap

private typealias Distribution3D = Array<Array<FloatArray>>

class OreChunk(private val yRange: IntRange) {

    private val blockOres = EnumMap<Ore, Distribution3D>(Ore::class.java).apply {
        for (ore in Ore.entries) {
            this[ore] = Array(16) { Array(yRange.size) { FloatArray(16) { 0f } } }
        }
    }
    private val sandOres = EnumMap<Ore, Distribution3D>(Ore::class.java).apply {
        for (ore in Ore.entries) {
            this[ore] = Array(16) { Array(yRange.size) { FloatArray(16) { 0f } } }
        }
    }
    private val gravelOres = EnumMap<Ore, Distribution3D>(Ore::class.java).apply {
        for (ore in Ore.entries) {
            this[ore] = Array(16) { Array(yRange.size) { FloatArray(16) { 0f } } }
        }
    }

    fun getBlockOre(x: Int, y: Int, z: Int, ore: Ore): Float = blockOres[ore]!![x][y - yRange.first][z]

    fun getSandOre(x: Int, y: Int, z: Int, ore: Ore): Float = sandOres[ore]!![x][y - yRange.first][z]

    fun getGravelOre(x: Int, y: Int, z: Int, ore: Ore): Float = gravelOres[ore]!![x][y - yRange.first][z]

    fun setBlockOre(x: Int, y: Int, z: Int, ore: Ore, value: Float) {
        blockOres[ore]!![x][y - yRange.first][z] = value
    }

    fun setSandOre(x: Int, y: Int, z: Int, ore: Ore, value: Float) {
        sandOres[ore]!![x][y - yRange.first][z] = value
    }

    fun setGravelOre(x: Int, y: Int, z: Int, ore: Ore, value: Float) {
        gravelOres[ore]!![x][y - yRange.first][z] = value
    }
}