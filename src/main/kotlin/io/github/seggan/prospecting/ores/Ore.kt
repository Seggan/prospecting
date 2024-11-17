package io.github.seggan.prospecting.ores

import io.github.seggan.prospecting.items.Pebble
import io.github.seggan.prospecting.items.smelting.Chemical
import io.github.seggan.prospecting.ores.config.OreConfig
import io.github.seggan.prospecting.ores.gen.generator.OreGenerator
import io.github.seggan.prospecting.registries.ProspectingCategories
import io.github.seggan.prospecting.registries.ProspectingRecipeTypes
import io.github.seggan.prospecting.util.subscript
import io.github.thebusybiscuit.slimefun4.api.SlimefunAddon
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.libraries.dough.collections.RandomizedSet
import io.github.thebusybiscuit.slimefun4.utils.ChatUtils
import org.bukkit.Material
import org.bukkit.NamespacedKey

class Ore(
    val id: NamespacedKey,
    private val pebbleMaterial: Material,
    val vanillaOre: Material,
    asciiFormula: String,
    val crushResult: RandomizedSet<Chemical>,
    val crushAmount: IntRange,
    val generator: OreGenerator
) {
    init {
        require("BUTTON" in pebbleMaterial.name) {
            "Pebble material for $id must be a button, got $pebbleMaterial"
        }
    }

    val oreName = ChatUtils.humanize(id.key)
    val formula = asciiFormula.subscript()

    val deepslateVanillaOre = Material.getMaterial("DEEPSLATE_" + vanillaOre.name)!!

    val oreId = "${id.namespace.uppercase()}_ORE_${id.key.uppercase()}"
    val oreItem = SlimefunItemStack(
        oreId,
        vanillaOre,
        "&4$oreName",
        "",
        "&aFormula: $formula"
    )

    private val pebbleId = "${id.namespace.uppercase()}_PEBBLE_${id.key.uppercase()}"
    val pebbleItem = SlimefunItemStack(
        pebbleId,
        pebbleMaterial,
        "&f$oreName pebble",
        "",
        "&7A pebble of ${oreName.lowercase()}",
        "&aFormula: $formula"
    )

    val pebble by lazy { SlimefunItem.getById(pebbleId) as Pebble }

    companion object {
        private val ores = mutableMapOf<NamespacedKey, Ore>()

        private val byId = entries.associateBy { it.oreId } + entries.associateBy { it.pebbleId }
        fun getBySlimefunId(id: String): Ore? = byId[id]
        fun getById(id: NamespacedKey): Ore? = ores[id]

        val entries get() = ores.values

        val associations = mutableMapOf<Ore, Set<Ore>>()

        fun loadFromConfig(file: String) {
            val configs = OreConfig.parse(file)
            for (config in configs) {
                val ore = config.toOre()
                ore.register()
            }
            for (config in configs) {
                val ore = getById(config.key)!!
                val associations = config.associations.mapNotNull(::getById).toSet()
                this.associations[ore] = associations
            }
        }
    }

    fun register() {
        ores[id] = this
    }

    fun registerItems(addon: SlimefunAddon) {
        SlimefunItem(
            ProspectingCategories.ORES,
            oreItem,
            ProspectingRecipeTypes.NATURALLY_GENERATED,
            emptyArray()
        ).register(addon)
        Pebble(
            ProspectingCategories.ORES,
            pebbleItem,
            ProspectingRecipeTypes.NATURALLY_GENERATED,
            emptyArray()
        ).register(addon)
    }

    override fun equals(other: Any?): Boolean {
        return other is Ore && other.id == id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}

