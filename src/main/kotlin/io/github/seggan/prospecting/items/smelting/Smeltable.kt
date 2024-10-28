package io.github.seggan.prospecting.items.smelting

import io.github.seggan.prospecting.util.itemKey
import io.github.seggan.sf4k.serial.serializers.DelegatingSerializer
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems
import kotlinx.serialization.Serializable
import kotlinx.serialization.serializer
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack

@Serializable(with = SmeltableSerializer::class)
class Smeltable private constructor(
    val id: NamespacedKey,
    val ingot: ItemStack,
    val dust: ItemStack = ingot,
    val meltingPoint: Int?,
    val boilingPoint: Int?,
) {

    enum class State {
        SOLID,
        LIQUID,
        GAS
    }

    fun getState(temperature: Int): State {
        return when {
            boilingPoint != null && temperature >= boilingPoint -> State.GAS
            meltingPoint != null && temperature >= meltingPoint -> State.LIQUID
            else -> State.SOLID
        }
    }

    override fun equals(other: Any?): Boolean {
        return this === other || (other is Smeltable && id == other.id)
    }

    override fun hashCode(): Int = ingot.hashCode()
    
    companion object {

        private val registry = mutableMapOf<NamespacedKey, Smeltable>()

        fun register(
            ingot: ItemStack,
            id: NamespacedKey = itemKey(ingot),
            dust: ItemStack = ingot,
            meltingPoint: Int? = null,
            boilingPoint: Int? = null
        ): Smeltable {
            val smeltable = Smeltable(
                id = id,
                ingot = ingot,
                dust = dust,
                meltingPoint = meltingPoint,
                boilingPoint = boilingPoint
            )
            registry[id] = smeltable
            return smeltable
        }

        operator fun get(ingot: ItemStack): Smeltable? {
            return registry[itemKey(ingot)]
        }

        operator fun get(material: Material): Smeltable? {
            return registry[material.key]
        }

        operator fun get(id: NamespacedKey): Smeltable? {
            return registry[id]
        }

        val IRON = register(
            ingot = ItemStack(Material.IRON_INGOT),
            dust = SlimefunItems.IRON_DUST,
            meltingPoint = 1538
        )

        val GOLD = register(
            ingot = ItemStack(Material.GOLD_INGOT),
            dust = SlimefunItems.GOLD_DUST,
            meltingPoint = 1064
        )

        val COPPER = register(
            ingot = SlimefunItems.COPPER_INGOT,
            dust = SlimefunItems.COPPER_DUST,
            meltingPoint = 1085
        )

        val TIN = register(
            ingot = SlimefunItems.TIN_INGOT,
            dust = SlimefunItems.TIN_DUST,
            meltingPoint = 231
        )

        val SILVER = register(
            ingot = SlimefunItems.SILVER_INGOT,
            dust = SlimefunItems.SILVER_DUST,
            meltingPoint = 962
        )

        val LEAD = register(
            ingot = SlimefunItems.LEAD_INGOT,
            dust = SlimefunItems.LEAD_DUST,
            meltingPoint = 327
        )

        val ALUMINUM = register(
            ingot = SlimefunItems.ALUMINUM_INGOT,
            dust = SlimefunItems.ALUMINUM_DUST,
            meltingPoint = 660
        )

        val ZINC = register(
            ingot = SlimefunItems.ZINC_INGOT,
            dust = SlimefunItems.ZINC_DUST,
            meltingPoint = 419
        )

        val NICKEL = register(
            ingot = SlimefunItems.NICKEL_INGOT,
            meltingPoint = 1455
        )

        val COBALT = register(
            ingot = SlimefunItems.COBALT_INGOT,
            meltingPoint = 1495
        )

        val BRONZE = register(
            ingot = SlimefunItems.BRONZE_INGOT,
            meltingPoint = 950
        )

        val BRASS = register(
            ingot = SlimefunItems.BRASS_INGOT,
            meltingPoint = 900
        )

        val STEEL = register(
            ingot = SlimefunItems.STEEL_INGOT,
            meltingPoint = 1400
        )
    }
}

private object SmeltableSerializer : DelegatingSerializer<Smeltable, String>(serializer()) {
    override fun toData(value: Smeltable): String = value.id.toString()
    override fun fromData(value: String): Smeltable = Smeltable[NamespacedKey.fromString(value)!!]!!
}
