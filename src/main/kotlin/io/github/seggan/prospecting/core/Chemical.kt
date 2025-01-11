package io.github.seggan.prospecting.core

import io.github.seggan.prospecting.config.ChemicalConfig
import io.github.seggan.prospecting.util.miniMessage
import io.github.seggan.sf4k.serial.serializers.BukkitSerializerRegistry
import io.github.seggan.sf4k.serial.serializers.DelegatingSerializer
import kotlinx.serialization.Serializable
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import java.nio.file.Path
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readText

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
                    "<#ffa200>Melting Point: <white>${if (meltingPoint < Int.MAX_VALUE) "$meltingPoint°C" else "N/A"}",
                    "<#ffa200>Boiling Point: <white>${if (boilingPoint < Int.MAX_VALUE) "$boilingPoint°C" else "N/A"}",
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

    val isSolidAtRoomTemperature = getState(ROOM_TEMPERATURE) == State.SOLID

    override fun equals(other: Any?): Boolean {
        return this === other || (other is Chemical && id == other.id)
    }

    override fun hashCode(): Int = ingot.hashCode()

    companion object {

        const val ROOM_TEMPERATURE = 20.0

        private val registry = mutableMapOf<NamespacedKey, Chemical>()

        val all: Collection<Chemical> get() = registry.values

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

        fun loadFromConfigs(configs: Path) {
            for (config in configs.listDirectoryEntries("*.json")) {
                val configs = ChemicalConfig.Companion.parse(config.readText())
                for (chemical in configs) {
                    register(chemical.toChemical())
                }
            }
        }
    }

    override fun toString(): String {
        return id.toString()
    }
}

private object SmeltableSerializer : DelegatingSerializer<Chemical, NamespacedKey>(BukkitSerializerRegistry.serializer()) {
    override fun fromData(value: NamespacedKey) = Chemical[value] ?: error("Unknown chemical: $value")
    override fun toData(value: Chemical) = value.id
}