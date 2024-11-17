@file:UseContextualSerialization(NamespacedKey::class)

package io.github.seggan.prospecting.ores.config

import io.github.seggan.prospecting.Prospecting
import io.github.seggan.prospecting.items.smelting.Chemical
import io.github.seggan.prospecting.registries.ProspectingCategories
import io.github.seggan.prospecting.registries.ProspectingRecipeTypes
import io.github.seggan.prospecting.util.subscript
import io.github.seggan.sf4k.extensions.miniMessageToLegacy
import io.github.seggan.sf4k.serial.serializers.BukkitSerializerRegistry
import io.github.seggan.sf4k.serial.serializers.DelegatingSerializer
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseContextualSerialization
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack

@Serializable
data class ChemicalConfig(
    val item: ChemicalItemConfig,
    val key: NamespacedKey = item.key,
    val name: String = key.key.replace('_', ' '),
    val ingot: ChemicalItemConfig = ChemicalItemConfig.Existing(item.key),
    @SerialName("melting_point") val meltingPoint: Int = Int.MAX_VALUE,
    @SerialName("boiling_point") val boilingPoint: Int = Int.MAX_VALUE,
) {
    fun toChemical(): Chemical {
        val item = this.item.getItem()
        val ingot = this.ingot.getItem()
        return Chemical(
            key,
            name,
            item,
            ingot,
            meltingPoint,
            boilingPoint
        )
    }

    companion object {
        fun parse(config: String): List<ChemicalConfig> {
            return Prospecting.json.decodeFromString<List<ChemicalConfig>>(config)
        }
    }
}

@Serializable(with = ChemicalItemConfig.Serializer::class)
sealed interface ChemicalItemConfig {

    val key: NamespacedKey

    fun getItem(): ItemStack

    @Serializable
    data class NewItem(
        val name: String,
        override val key: NamespacedKey,
        val item: Material,
        val formula: String
    ) : ChemicalItemConfig {
        override fun getItem(): ItemStack {
            val sfis = SlimefunItemStack(
                key.key.uppercase(),
                item,
                name.miniMessageToLegacy(),
                "<green>$formula".subscript().miniMessageToLegacy()
            )
            val sfi = SlimefunItem(
                ProspectingCategories.RAW_MATERIALS,
                sfis,
                ProspectingRecipeTypes.MALLET,
                emptyArray()
            )
            sfi.isUseableInWorkbench = true
            val plugin = Bukkit.getPluginManager().plugins.find {
                it.name.equals(key.namespace, ignoreCase = true)
            } as? SlimefunAddon ?: error("Invalid Slimefun item: ${key.key}")
            sfi.register(plugin)
            return sfis
        }
    }

    @Serializable(with = Existing.Serializer::class)
    data class Existing(override val key: NamespacedKey) : ChemicalItemConfig {
        object Serializer : DelegatingSerializer<Existing, NamespacedKey>(BukkitSerializerRegistry.serializer()) {
            override fun toData(value: Existing) = value.key
            override fun fromData(value: NamespacedKey) = Existing(value)
        }

        override fun getItem(): ItemStack {
            if (key.namespace == "minecraft") {
                return ItemStack(Material.matchMaterial(key.key) ?: error("Invalid material: ${key.key}"))
            } else {
                val sfi = SlimefunItem.getById(key.key.uppercase()) ?: error("Invalid Slimefun item: ${key.key}")
                if (!sfi.addon.name.equals(key.namespace, ignoreCase = true)) {
                    error("Invalid Slimefun item: ${key.key}")
                }
                return sfi.item
            }
        }
    }

    object Serializer : JsonContentPolymorphicSerializer<ChemicalItemConfig>(ChemicalItemConfig::class) {
        override fun selectDeserializer(element: JsonElement) = when {
            element is JsonPrimitive -> Existing.serializer()
            else -> NewItem.serializer()
        }
    }
}
