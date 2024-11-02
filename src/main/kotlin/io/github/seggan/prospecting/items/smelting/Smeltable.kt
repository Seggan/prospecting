package io.github.seggan.prospecting.items.smelting

import io.github.seggan.prospecting.util.itemKey
import io.github.seggan.prospecting.util.miniMessage
import io.github.seggan.prospecting.util.text
import io.github.seggan.sf4k.serial.serializers.BukkitSerializerRegistry
import io.github.seggan.sf4k.serial.serializers.DelegatingSerializer
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack

@Serializable(with = SmeltableSerializer::class)
class Smeltable private constructor(
    val id: NamespacedKey,
    val name: String,
    val ingot: ItemStack,
    val dust: ItemStack = ingot,
    val meltingPoint: Int,
    val boilingPoint: Int,
) {

    private val titleName = name.replaceFirstChar(Char::uppercase)

    val displayItem: ItemStack


    init {
        val stack = ingot.clone()
        stack.editMeta {
            it.displayName(MiniMessage.miniMessage().deserialize("<!i><white>$titleName"))
            it.lore(
                listOf(
                    "",
                    "<#ffa200>Melting Point: <white>${if (meltingPoint < Int.MAX_VALUE) "${meltingPoint}°C" else "N/A"}",
                    "<#ffa200>Boiling Point: <white>${if (boilingPoint < Int.MAX_VALUE) "${boilingPoint}°C" else "N/A"}",
                ).miniMessage()
            )
        }
        displayItem = stack
    }

    enum class State {
        SOLID,
        LIQUID,
        GAS
    }

    fun getState(temperature: Double): State {
        return when {
            temperature >= boilingPoint -> State.GAS
            temperature >= meltingPoint -> State.LIQUID
            else -> State.SOLID
        }
    }

    override fun equals(other: Any?): Boolean {
        return this === other || (other is Smeltable && id == other.id)
    }

    override fun hashCode(): Int = ingot.hashCode()

    companion object {

        private val registry = mutableMapOf<NamespacedKey, Smeltable>()

        val all: Collection<Smeltable> get() = registry.values

        fun register(
            ingot: ItemStack,
            name: String = ingot.itemMeta.displayName()?.text?.lowercase() ?: ingot.type.name.lowercase(),
            id: NamespacedKey = itemKey(ingot),
            dust: ItemStack = ingot,
            meltingPoint: Int? = null,
            boilingPoint: Int? = null
        ): Smeltable {
            val smeltable = Smeltable(
                id = id,
                name = name,
                ingot = ingot,
                dust = dust,
                meltingPoint = meltingPoint ?: Int.MAX_VALUE,
                boilingPoint = boilingPoint ?: Int.MAX_VALUE
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

        fun getByIngotOrDust(item: ItemStack): Smeltable? {
            return registry.values.firstOrNull { it.ingot.isSimilar(item) || it.dust.isSimilar(item) }
        }

        val IRON = register(
            name = "iron",
            ingot = ItemStack(Material.IRON_INGOT),
            dust = SlimefunItems.IRON_DUST,
            meltingPoint = 1538
        )

        val GOLD = register(
            name = "gold",
            ingot = ItemStack(Material.GOLD_INGOT),
            dust = SlimefunItems.GOLD_DUST,
            meltingPoint = 1064
        )

        val COPPER = register(
            name = "copper",
            ingot = SlimefunItems.COPPER_INGOT,
            dust = SlimefunItems.COPPER_DUST,
            meltingPoint = 1085
        )

        val TIN = register(
            name = "tin",
            ingot = SlimefunItems.TIN_INGOT,
            dust = SlimefunItems.TIN_DUST,
            meltingPoint = 231
        )

        val SILVER = register(
            name = "silver",
            ingot = SlimefunItems.SILVER_INGOT,
            dust = SlimefunItems.SILVER_DUST,
            meltingPoint = 962
        )

        val LEAD = register(
            name = "lead",
            ingot = SlimefunItems.LEAD_INGOT,
            dust = SlimefunItems.LEAD_DUST,
            meltingPoint = 327
        )

        val ALUMINUM = register(
            name = "aluminum",
            ingot = SlimefunItems.ALUMINUM_INGOT,
            dust = SlimefunItems.ALUMINUM_DUST,
            meltingPoint = 660
        )

        val ZINC = register(
            name = "zinc",
            ingot = SlimefunItems.ZINC_INGOT,
            dust = SlimefunItems.ZINC_DUST,
            meltingPoint = 419
        )

        val NICKEL = register(
            name = "nickel",
            ingot = SlimefunItems.NICKEL_INGOT,
            meltingPoint = 1455
        )

        val COBALT = register(
            name = "cobalt",
            ingot = SlimefunItems.COBALT_INGOT,
            meltingPoint = 1495
        )

        val BRONZE = register(
            name = "bronze",
            ingot = SlimefunItems.BRONZE_INGOT,
            meltingPoint = 950
        )

        val BRASS = register(
            name = "brass",
            ingot = SlimefunItems.BRASS_INGOT,
            meltingPoint = 900
        )

        val STEEL = register(
            name = "steel",
            ingot = SlimefunItems.STEEL_INGOT,
            meltingPoint = 1400
        )
    }

    override fun toString(): String {
        return id.toString()
    }
}

private object SmeltableSerializer : DelegatingSerializer<Smeltable, NamespacedKey>(BukkitSerializerRegistry.serializer()) {
    override fun fromData(value: NamespacedKey) = Smeltable[value]!!
    override fun toData(value: Smeltable) = value.id
}
