package io.github.seggan.prospecting.items.smelting

import io.github.seggan.prospecting.Prospecting
import io.github.seggan.prospecting.items.smelting.tools.Thermometer
import io.github.seggan.prospecting.util.SlimefunBlock
import io.github.seggan.prospecting.util.miniMessage
import io.github.seggan.prospecting.util.moveAsymptoticallyTo
import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils
import net.kyori.adventure.text.Component
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Item
import org.bukkit.event.Event
import org.bukkit.inventory.ItemStack
import org.bukkit.util.BoundingBox
import java.util.TreeSet
import java.util.concurrent.ThreadLocalRandom

class Crucible(
    itemGroup: ItemGroup,
    item: SlimefunItemStack,
    recipeType: RecipeType,
    recipe: Array<out ItemStack?>,
    private val capacity: Int,
) : SlimefunItem(itemGroup, item, recipeType, recipe), RecipeDisplayItem {

    companion object {
        private const val ROOM_TEMPERATURE = 20.0

        private val recipes = TreeSet<SmeltingRecipe>(
            compareByDescending<SmeltingRecipe> { it.temperature }
                .thenComparingInt(SmeltingRecipe::hashCode)
        )

        fun addRecipe(recipe: SmeltingRecipe) {
            recipes += recipe
        }

        internal fun initRecipes(config: String) {
            val loaded = Prospecting.json.decodeFromString<List<SmeltingRecipe>>(config)
            recipes += loaded
        }
    }

    init {
        SlimefunBlock.applyBlock(this@Crucible, ::CrucibleBlock)
    }

    fun cast(block: Block): Smeltable? {
        CrucibleBlock(block).use { crucible ->
            val top = crucible.sortedContents.firstOrNull()?.first ?: return null
            if (top.getState(crucible.temperature) == Smeltable.State.LIQUID) {
                crucible.contents.merge(top, 1, Int::minus)
                crucible.contents = crucible.contents.filterValues { it > 0 }.toMutableMap()
                return top
            } else {
                return null
            }
        }
    }

    inner class CrucibleBlock(block: Block) : SlimefunBlock(block) {

        var contents: MutableMap<Smeltable, Int> by blockStorage { mutableMapOf() }
        var temperature: Double by blockStorage { ROOM_TEMPERATURE }

        override fun tick() {
            // Add new items
            val items = block.world.getNearbyEntities(BoundingBox.of(block))
                .filterIsInstance<Item>()
            for (item in items) {
                val stack = item.itemStack
                val available = capacity - contents.values.sum()
                val allowed = stack.amount.coerceAtMost(available)
                if (allowed == 0) continue
                val smeltable = Smeltable.getByIngotOrDust(stack) ?: continue
                contents.merge(smeltable, allowed, Int::plus)
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
                if (temperature >= recipe.temperature &&
                    recipe.inputs.all { (input, amount) -> contents.getOrDefault(input, 0) >= amount }
                ) {
                    for ((input, amount) in recipe.inputs) {
                        contents.merge(input, amount, Int::minus)
                    }
                    contents.merge(recipe.output, recipe.outputAmount, Int::plus)
                }
            }

            contents = contents.filterValues { it > 0 }.toMutableMap()

            if (temperature > ROOM_TEMPERATURE) {
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
                temperature = temperature.moveAsymptoticallyTo(ROOM_TEMPERATURE, rate)
            }
        }

        override fun onInteract(e: PlayerRightClickEvent) {
            val item = e.item
            val p = e.player
            if (item.type == Material.WATER_BUCKET) {
                temperature = temperature.moveAsymptoticallyTo(ROOM_TEMPERATURE, 0.5)
                if (ThreadLocalRandom.current().nextFloat() < 0.1) {
                    block.location.createExplosion(4f, false, false)
                    if (p.gameMode != GameMode.CREATIVE) {
                        p.inventory.setItem(e.hand, ItemStack(Material.BUCKET))
                    }
                }
            } else if (item.type == Material.TINTED_GLASS) {
                e.setUseItem(Event.Result.DENY)
                if (contents.isEmpty()) {
                    p.sendMessage("The crucible is empty")
                } else {
                    for ((smeltable, amount) in sortedContents) {
                        val unit = if (amount == 1) "unit" else "units"
                        val state = smeltable.getState(temperature).name.lowercase()
                        p.sendMessage("$amount $unit of $state ${smeltable.name}")
                    }
                }
            } else if (getByItem(item) is Thermometer) {
                p.sendActionBar(
                    Component.text("The crucible's temperature is %.2f°C".format(temperature))
                )
            } else if ("SHOVEL" in item.type.name && temperature <= 100) {
                val removedContents = contents.filterKeys { it.getState(temperature) == Smeltable.State.SOLID }
                if (removedContents.isNotEmpty()) {
                    val slag = Slag.create(removedContents)
                    block.world.dropItem(block.location.toCenterLocation(), slag)
                    contents.keys.removeAll(removedContents.keys)
                }
            }
        }

        val sortedContents: List<Pair<Smeltable, Int>>
            get() {
                val comparator = compareByDescending<Pair<Smeltable, Int>> {
                    it.first.getState(temperature) == Smeltable.State.LIQUID
                }.thenByDescending { it.first.meltingPoint }
                return contents.toList().sortedWith(comparator)
            }
    }

    override fun getDisplayRecipes(): MutableList<ItemStack> {
        val list = mutableListOf<ItemStack>()
        repeat(2) { list += smeltablesItem }
        for (smeltable in Smeltable.all) {
            list += smeltable.displayItem
        }
        if (Smeltable.all.size % 2 != 0) {
            list += ChestMenuUtils.getBackground()
        }
        repeat(2) { list += recipesItem }
        for (recipe in recipes) {
            val output = recipe.output
            val outputAmount = recipe.outputAmount
            val displayItem = output.displayItem.clone()
            displayItem.amount = outputAmount
            displayItem.editMeta {
                val lore = mutableListOf("", "<red>Inputs:")
                for ((input, amount) in recipe.inputs) {
                    lore += "<red>$amount ${input.name}"
                }
                lore += ""
                lore += "<#ffa200>Temperature: <white>${recipe.temperature}°C"
                lore += ""
                lore += "<green>Output: $outputAmount ${output.name}"
                it.lore(lore.miniMessage())
            }
            list += displayItem
        }
        return list
    }
}

private val smeltablesItem = CustomItemStack(
    Material.ORANGE_STAINED_GLASS_PANE,
    "&6Smeltables",
    "",
    "&7Information about smeltables ->",
    "&7Scroll past for recipes"
)

private val recipesItem = CustomItemStack(
    Material.LIME_STAINED_GLASS_PANE,
    "&aRecipes",
    "",
    "&7Information about recipes ->",
    "&7<- Scroll back for smeltables"
)