package io.github.seggan.prospecting.util

import io.github.seggan.prospecting.Prospecting
import io.github.seggan.sf4k.serial.blockstorage.getBlockStorage
import io.github.seggan.sf4k.serial.blockstorage.setBlockStorage
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.RandomizedSet
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.block.Block
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

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

fun <T : Any> randomizedSetOf(vararg pairs: Pair<T, Float>): RandomizedSet<T> {
    val set = RandomizedSet<T>()
    for ((value, weight) in pairs) {
        set.add(value, weight)
    }
    return set
}

operator fun <T> ThreadLocal<T>.getValue(thisRef: Any?, property: KProperty<*>): T = get()

val IntRange.size get() = last - first + 1

inline fun <reified T> blockStorage(block: Block, default: T) = object :  ReadWriteProperty<Any?, T> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return block.getBlockStorage<T>(property.name) ?: default
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        block.setBlockStorage<T>(property.name, value)
    }
}

fun itemKey(item: ItemStack): NamespacedKey {
    val sfi = SlimefunItem.getByItem(item)
    if (sfi != null) {
        val addon = sfi.addon.javaPlugin
        val id = sfi.id.lowercase()
        return NamespacedKey(addon, id)
    } else {
        return item.type.key
    }
}