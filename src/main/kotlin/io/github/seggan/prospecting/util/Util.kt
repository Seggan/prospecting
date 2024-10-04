package io.github.seggan.prospecting.util

import io.github.seggan.prospecting.Prospecting
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.entity.Entity

fun String.key() = NamespacedKey(Prospecting, this)

inline fun <reified T : Entity> Location.spawn(): T {
    return world!!.spawn(this, T::class.java)
}