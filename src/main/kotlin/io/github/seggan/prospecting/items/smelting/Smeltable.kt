package io.github.seggan.prospecting.items.smelting

import io.github.seggan.prospecting.ores.config.SmeltableConfig
import io.github.seggan.prospecting.util.itemKey
import io.github.seggan.prospecting.util.miniMessage
import io.github.seggan.prospecting.util.text
import io.github.seggan.sf4k.serial.serializers.BukkitSerializerRegistry
import io.github.seggan.sf4k.serial.serializers.DelegatingSerializer
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack

@Serializable(with = SmeltableSerializer::class)
class Smeltable(
    val id: NamespacedKey,
    val name: String,
    val item: ItemStack,
    val ingot: ItemStack = item,
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
            item: ItemStack,
            name: String = item.itemMeta.displayName()?.text?.lowercase() ?: item.type.name.lowercase(),
            id: NamespacedKey = itemKey(item),
            ingot: ItemStack = item,
            meltingPoint: Int? = null,
            boilingPoint: Int? = null
        ): Smeltable {
            val smeltable = Smeltable(
                id = id,
                name = name,
                item = item,
                ingot = ingot,
                meltingPoint = meltingPoint ?: Int.MAX_VALUE,
                boilingPoint = boilingPoint ?: Int.MAX_VALUE
            )
            registry[id] = smeltable
            return smeltable
        }

        fun register(smeltable: Smeltable) {
            registry[smeltable.id] = smeltable
        }

        operator fun get(id: NamespacedKey): Smeltable? {
            return registry[id]
        }

        operator fun get(id: String): Smeltable? {
            return registry[NamespacedKey.fromString(id)]
        }

        fun getByIngotOrDust(item: ItemStack): Smeltable? {
            return registry.values.firstOrNull { it.ingot.isSimilar(item) || it.item.isSimilar(item) }
        }

        fun loadFromConfig(config: String) {
            val configs = SmeltableConfig.parse(config)
            for (smeltable in configs) {
                register(smeltable.toSmeltable())
            }
        }
    }

    override fun toString(): String {
        return id.toString()
    }
}

private object SmeltableSerializer : DelegatingSerializer<Smeltable, NamespacedKey>(BukkitSerializerRegistry.serializer()) {
    override fun fromData(value: NamespacedKey) = Smeltable[value]!!
    override fun toData(value: Smeltable) = value.id
}
