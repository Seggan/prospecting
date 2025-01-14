package io.github.seggan.prospecting.util.serial

import io.github.seggan.sf4k.serial.serializers.BukkitSerializerRegistry
import io.github.seggan.sf4k.serial.serializers.DelegatingSerializer
import org.bukkit.Bukkit
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

object InventorySerializer : DelegatingSerializer<Inventory, List<ItemStack?>>(BukkitSerializerRegistry.serializer()) {
    override fun toData(value: Inventory): List<ItemStack?> {
        return List(value.size) { value.getItem(it) }
    }

    override fun fromData(value: List<ItemStack?>): Inventory {
        val inv = Bukkit.createInventory(null, value.size)
        for ((index, item) in value.withIndex()) {
            inv.setItem(index, item)
        }
        return inv
    }
}