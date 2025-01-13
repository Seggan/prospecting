package io.github.seggan.prospecting.util

import io.github.seggan.prospecting.pluginInstance
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.ChunkSnapshot
import org.bukkit.NamespacedKey

internal fun String.key() = NamespacedKey(pluginInstance, this)

val Component.text: String get() = PlainTextComponentSerializer.plainText().serialize(this)

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

fun String.capitalizeWords(): String {
    return split(" ").joinToString(" ") { it.replaceFirstChar(Char::titlecase) }
}

val IntRange.size get() = last - first + 1

fun Double.moveAsymptoticallyTo(target: Double, rate: Double): Double {
    return this + (target - this) * rate
}

fun secondsToSfTicks(seconds: Int): Int {
    return seconds * 20 / Slimefun.getTickerTask().tickRate
}

fun List<String>.miniMessage(): List<Component> {
    return map { MiniMessage.miniMessage().deserialize("<!i>$it") }
}

tailrec fun ChunkSnapshot.getHighestOpaqueBlockY(x: Int, z: Int, y: Int = getHighestBlockYAt(x, z)): Int {
    return if (y < -64 || getBlockType(x, y, z).isOccluding) y else getHighestOpaqueBlockY(x, z, y - 1)
}