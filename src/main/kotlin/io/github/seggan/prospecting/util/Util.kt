package io.github.seggan.prospecting.util

import io.github.seggan.prospecting.Prospecting
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.RandomizedSet
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity
import kotlin.math.exp

fun String.key() = NamespacedKey(Prospecting, this)

inline fun <reified T : Entity> Location.spawn(): T {
    return world!!.spawn(this, T::class.java)
}

fun String.subscript(): String {
    val sb = StringBuilder()
    for (c in this) {
        sb.append(
            when (c) {
                '0' -> '₀'
                '1' -> '₁'
                '2' -> '₂'
                '3' -> '₃'
                '4' -> '₄'
                '5' -> '₅'
                '6' -> '₆'
                '7' -> '₇'
                '8' -> '₈'
                '9' -> '₉'
                else -> c
            }
        )
    }
    return sb.toString()
}

fun <T : Any> randomizedSetOf(vararg values: T): RandomizedSet<T> {
    val set = RandomizedSet<T>()
    for (value in values) {
        set.add(value, 1f)
    }
    return set
}

fun <T : Any> randomizedSetOf(vararg pairs: Pair<T, Double>): RandomizedSet<T> {
    val set = RandomizedSet<T>()
    for ((value, weight) in pairs) {
        set.add(value, weight.toFloat())
    }
    return set
}