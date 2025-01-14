package io.github.seggan.prospecting.items.smelting

import com.destroystokyo.paper.ParticleBuilder
import io.github.seggan.prospecting.Prospecting
import io.github.seggan.prospecting.core.Chemical
import io.github.seggan.prospecting.core.SmeltingRecipe
import io.github.seggan.prospecting.items.LiquidChemicalHolder
import io.github.seggan.prospecting.items.smelting.tools.Thermometer
import io.github.seggan.prospecting.util.key
import io.github.seggan.prospecting.util.miniMessage
import io.github.seggan.prospecting.util.moveAsymptoticallyTo
import io.github.seggan.prospecting.util.sfb.SlimefunBlock
import io.github.seggan.prospecting.util.sfb.modules.Ticker
import io.github.seggan.prospecting.util.sfb.modules.Useable
import io.github.seggan.sf4k.extensions.plus
import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Item
import org.bukkit.entity.LivingEntity
import org.bukkit.event.Event
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.BoundingBox
import java.nio.file.Path
import java.util.TreeSet
import java.util.concurrent.ThreadLocalRandom
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.readText

class Crucible(
    itemGroup: ItemGroup,
    item: SlimefunItemStack,
    recipeType: RecipeType,
    recipe: Array<out ItemStack?>,
    private val capacity: Int,
) : SlimefunItem(itemGroup, item, recipeType, recipe), RecipeDisplayItem {

    companion object {
        private val recipes = TreeSet<SmeltingRecipe>(
            compareByDescending<SmeltingRecipe> { it.temperature }
                .thenComparingInt(System::identityHashCode) // why treeset not use equals aaaa
        )

        fun addRecipe(recipe: SmeltingRecipe) {
            recipes += recipe
        }

        internal fun initRecipes(configs: Path) {
            for (config in configs.listDirectoryEntries("*.json")) {
                val loaded = Prospecting.json.decodeFromString<List<SmeltingRecipe>>(config.readText())
                recipes += loaded
            }
        }
    }

    init {
        SlimefunBlock.applyBlock(this@Crucible, ::CrucibleBlock)
    }

    fun cast(block: Block): Chemical? {
        CrucibleBlock(block, this).use { crucible ->
            val top = crucible.sortedContents.find { (chemical, _) ->
                chemical.getState(crucible.temperature) == Chemical.State.LIQUID &&
                        chemical.getState(Chemical.ROOM_TEMPERATURE) == Chemical.State.SOLID
            }?.first ?: return null
            crucible.contents.merge(top, 1, Int::minus)
            crucible.contents = crucible.contents.filterValues { it > 0 }.toMutableMap()
            return top
        }
    }

    class CrucibleBlock(block: Block, item: Crucible) : SlimefunBlock<Crucible>(block, item), Ticker, Useable {

        var contents: MutableMap<Chemical, Int> by blockStorage { mutableMapOf() }
        var temperature: Double by blockStorage { Chemical.ROOM_TEMPERATURE }

        override fun tick() {
            // Add new items
            val items = block.world.getNearbyEntities(BoundingBox.of(block)).filterIsInstance<Item>()
            for (item in items) {
                val stack = item.itemStack
                val available = sfItemInstance.capacity - contents.values.sum()
                val allowed = stack.amount.coerceAtMost(available)
                if (allowed == 0) continue
                val chemical = Chemical.getByIngotOrDust(stack) ?: continue
                contents.merge(chemical, allowed, Int::plus)
                stack.subtract(allowed)
                if (stack.amount == 0) {
                    item.remove()
                } else {
                    item.itemStack = stack
                }
                temperature -= temperature * (1 / (contents.values.sum() + 1))
            }

            // Perform smelting
            for (recipe in recipes) {
                recipe.performRecipe(temperature, contents)
            }

            contents = contents.filterValues { it > 0 }.toMutableMap()

            // Handle gases
            if (!block.getRelative(BlockFace.UP).isSolid) {
                val escaping = contents.filterKeys { it.getState(temperature) == Chemical.State.GAS }
                for (chemical in escaping) {
                    contents.merge(chemical.key, 1, Int::minus)
                }
                if (escaping.isNotEmpty()) {
                    ParticleBuilder(Particle.CAMPFIRE_COSY_SMOKE)
                        .count(10)
                        .location(block.location.toCenterLocation().add(0.5, 1.5, 0.5))
                        .spawn()
                    val entities = block.world.getNearbyEntities(block.location, 5.0, 5.0, 5.0)
                        .filterIsInstance<LivingEntity>()
                    if (temperature >= 800) {
                        for (entity in entities) {
                            entity.fireTicks = escaping.values.sum() * 20
                        }
                    }
                    val mercury = Chemical[MERCURY]?.let(escaping::get)
                    if (mercury != null) {
                        for (entity in entities) {
                            entity.addPotionEffect(
                                PotionEffect(
                                    PotionEffectType.WITHER,
                                    20 * mercury,
                                    mercury / 10 + 1
                                )
                            )
                            entity.sendMessage(NamedTextColor.RED + "You have been poisoned by mercury!")
                        }
                    }
                }
            }

            // Cool down
            if (temperature > Chemical.ROOM_TEMPERATURE) {
                var solid = -1 // -1 to account for the crucible itself
                for (x in -1..1) {
                    for (y in -1..1) {
                        for (z in -1..1) {
                            val block = block.getRelative(x, y, z)
                            if (block.type.isSolid) solid++
                        }
                    }
                }
                val rate = 0.02 / (contents.values.sum() + solid + 1)
                temperature = temperature.moveAsymptoticallyTo(Chemical.ROOM_TEMPERATURE, rate)
            }
        }

        override fun onInteract(e: PlayerRightClickEvent) {
            e.setUseItem(Event.Result.DENY)
            e.setUseBlock(Event.Result.DENY)
            val item = e.item
            val p = e.player
            val sfItem = getByItem(item)
            when {
                item.type == Material.WATER_BUCKET -> {
                    temperature = temperature.moveAsymptoticallyTo(Chemical.ROOM_TEMPERATURE, 0.5)
                    if (ThreadLocalRandom.current().nextFloat() < 0.1) {
                        block.location.createExplosion(4f, false, false)
                        if (p.gameMode != GameMode.CREATIVE) {
                            p.inventory.setItem(e.hand, ItemStack(Material.BUCKET))
                        }
                    }
                }
                item.type == Material.TINTED_GLASS -> {
                    if (contents.isEmpty()) {
                        p.sendMessage("The crucible is empty")
                    } else {
                        for ((chemical, amount) in sortedContents) {
                            val unit = if (amount == 1) "unit" else "units"
                            val state = chemical.getState(temperature).adjective
                            p.sendMessage("$amount $unit of $state ${chemical.name}")
                        }
                    }
                }
                sfItem is Thermometer -> {
                    p.sendActionBar(
                        Component.text("The crucible's temperature is %.2f°C".format(temperature))
                    )
                }
                temperature <= 100 && "SHOVEL" in item.type.name -> {
                    val removedContents = contents.filterKeys { it.getState(temperature) == Chemical.State.SOLID }
                    if (removedContents.isNotEmpty()) {
                        val slag = Slag.create(removedContents)
                        block.world.dropItem(block.location.toCenterLocation(), slag)
                        contents.keys.removeAll(removedContents.keys)
                    }
                }
                sfItem is LiquidChemicalHolder -> {
                    val chemical = sfItem.chemical
                    contents.merge(chemical, 1, Int::plus)
                    p.inventory.setItem(e.hand, ItemStack(sfItem.emptyMaterial))
                }
                item.type == Material.BUCKET || item.type == Material.GLASS_BOTTLE -> {
                    val liquid = sortedContents.firstOrNull {
                        it.first.getState(temperature) == Chemical.State.LIQUID
                    }?.first
                    if (liquid != null) {
                        val holder = LiquidChemicalHolder.getHolder(liquid)
                        if (holder != null && holder.emptyMaterial == item.type) {
                            item.subtract()
                            p.inventory.addItem(holder.item.clone())
                            contents.merge(liquid, 1, Int::minus)
                        }
                    }
                }
            }
        }

        val sortedContents: List<Pair<Chemical, Int>>
            get() {
                val comparator = compareByDescending<Pair<Chemical, Int>> {
                    it.first.getState(temperature).ordinal
                }.thenByDescending { it.first.meltingPoint }
                return contents.toList().sortedWith(comparator)
            }
    }

    override fun getDisplayRecipes(): MutableList<ItemStack> {
        val list = mutableListOf<ItemStack>()
        repeat(2) { list += chemicalsItem }
        for (chemical in Chemical.all) {
            list += chemical.displayItem
        }
        if (Chemical.all.size % 2 != 0) {
            list += ChestMenuUtils.getBackground()
        }
        repeat(2) { list += recipesItem }
        for (recipe in recipes) {
            val (output, amount) = recipe.outputs.entries.first()
            val displayItem = output.displayItem.clone()
            displayItem.amount = amount
            displayItem.editMeta {
                val lore = mutableListOf("", "<red>Inputs:")
                for ((input, amount) in recipe.inputs) {
                    lore += "<red>$amount ${input.name}"
                }
                lore += ""
                lore += "<#ffa200>Temperature: <white>${recipe.temperature}°C"
                lore += ""
                lore += "<green>Output: $amount ${output.name}"
                it.lore(lore.miniMessage())
            }
            list += displayItem
        }
        return list
    }
}

private val chemicalsItem = CustomItemStack(
    Material.ORANGE_STAINED_GLASS_PANE,
    "&6Chemicals",
    "",
    "&7Information about chemicals ->",
    "&7Scroll past for recipes"
)

private val recipesItem = CustomItemStack(
    Material.LIME_STAINED_GLASS_PANE,
    "&aRecipes",
    "",
    "&7Information about recipes ->",
    "&7<- Scroll back for chemcials"
)

private val MERCURY = "mercury".key()