package io.github.seggan.prospecting.items.smelting

import io.github.seggan.prospecting.ores.config.ChemicalConfig
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
class Chemical(
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

    enum class State(val adjective: String) {
        SOLID("solid"),
        LIQUID("liquid"),
        GAS("gaseous");
    }

    fun getState(temperature: Double): State {
        return when {
            temperature >= boilingPoint -> State.GAS
            temperature >= meltingPoint -> State.LIQUID
            else -> State.SOLID
        }
    }

    override fun equals(other: Any?): Boolean {
        return this === other || (other is Chemical && id == other.id)
    }

    override fun hashCode(): Int = ingot.hashCode()

    companion object {

        private val registry = mutableMapOf<NamespacedKey, Chemical>()

        val all: Collection<Chemical> get() = registry.values

        fun register(
            item: ItemStack,
            name: String = item.itemMeta.displayName()?.text?.lowercase() ?: item.type.name.lowercase(),
            id: NamespacedKey = itemKey(item),
            ingot: ItemStack = item,
            meltingPoint: Int? = null,
            boilingPoint: Int? = null
        ): Chemical {
            val chemical = Chemical(
                id = id,
                name = name,
                item = item,
                ingot = ingot,
                meltingPoint = meltingPoint ?: Int.MAX_VALUE,
                boilingPoint = boilingPoint ?: Int.MAX_VALUE
            )
            registry[id] = chemical
            return chemical
        }

        fun register(chemical: Chemical) {
            registry[chemical.id] = chemical
        }

        operator fun get(id: NamespacedKey): Chemical? {
            return registry[id]
        }

        operator fun get(id: String): Chemical? {
            return registry[NamespacedKey.fromString(id)]
        }

        fun getByIngotOrDust(item: ItemStack): Chemical? {
            return registry.values.firstOrNull { it.ingot.isSimilar(item) || it.item.isSimilar(item) }
        }

        fun loadFromConfig(config: String) {
            val configs = ChemicalConfig.parse(config)
            for (chemical in configs) {
                register(chemical.toChemical())
            }
        }
    }

    override fun toString(): String {
        return id.toString()
    }
}

private object SmeltableSerializer : DelegatingSerializer<Chemical, NamespacedKey>(BukkitSerializerRegistry.serializer()) {
    override fun fromData(value: NamespacedKey) = Chemical[value]!!
    override fun toData(value: Chemical) = value.id
}
