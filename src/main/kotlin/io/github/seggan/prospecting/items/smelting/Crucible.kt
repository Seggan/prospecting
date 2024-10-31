package io.github.seggan.prospecting.items.smelting

import io.github.seggan.prospecting.items.smelting.items.Slag
import io.github.seggan.prospecting.items.smelting.tools.Thermometer
import io.github.seggan.prospecting.registries.ProspectingItems
import io.github.seggan.prospecting.util.SlimefunBlock
import io.github.seggan.prospecting.util.moveAsymptoticallyTo
import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems
import net.kyori.adventure.text.Component
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Item
import org.bukkit.event.Event
import org.bukkit.inventory.ItemStack
import org.bukkit.util.BoundingBox
import java.util.concurrent.ThreadLocalRandom

class Crucible(
    itemGroup: ItemGroup,
    item: SlimefunItemStack,
    recipeType: RecipeType,
    recipe: Array<out ItemStack?>,
    private val capacity: Int,
) : SlimefunItem(itemGroup, item, recipeType, recipe) {

    companion object {
        private const val ROOM_TEMPERATURE = 20.0

        private val recipes = mutableSetOf<SmeltingRecipe>()

        fun registerRecipe(recipe: SmeltingRecipe) {
            recipes.add(recipe)
        }

        fun registerRecipe(
            vararg inputs: Pair<Smeltable, Int>,
            temperature: Int,
            output: Smeltable,
        ) = registerRecipe(SmeltingRecipe(inputs.toList(), output, temperature))

        internal fun initRecipes() {
            val coal = Smeltable.register(ItemStack(Material.COAL))
            val charcoal = Smeltable.register(ItemStack(Material.CHARCOAL))
            registerRecipe(
                Smeltable[ProspectingItems.COPPER_CARBONATE]!! to 1,
                temperature = 300,
                output = Smeltable[ProspectingItems.COPPER_OXIDE]!!,
            )
            registerRecipe(
                Smeltable[ProspectingItems.COPPER_OXIDE]!! to 1,
                temperature = 1200,
                output = Smeltable[SlimefunItems.COPPER_INGOT]!!,
            )
            registerRecipe(
                coal to 1,
                temperature = 670,
                output = charcoal
            )
            registerRecipe(
                charcoal to 9,
                temperature = 1000,
                output = Smeltable[ProspectingItems.COKE]!!
            )
        }
    }

    init {
        SlimefunBlock.applyBlock(this@Crucible, ::CrucibleBlock)
    }

    fun cast(block: Block): Smeltable? {
        CrucibleBlock(block).use { crucible ->
            val top = crucible.contents.keys.maxByOrNull { it.meltingPoint ?: 0 } ?: return null
            if (top.getState(crucible.temperature) == Smeltable.State.LIQUID) {
                crucible.contents.merge(top, 1, Int::minus)
                crucible.contents = crucible.contents.filterValues { it > 0 }.toMutableMap()
                return top
            } else {
                return null
            }
        }
    }

    private inner class CrucibleBlock(block: Block) : SlimefunBlock(block) {

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
                val smeltable = Smeltable[stack] ?: continue
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
                if (recipe.canSmelt(temperature) &&
                    recipe.inputs.all { (input, amount) -> contents.getOrDefault(input, 0) >= amount }
                ) {
                    for ((input, amount) in recipe.inputs) {
                        contents.merge(input, amount, Int::minus)
                    }
                    contents.merge(recipe.output, 1, Int::plus)
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
                    val sorted = contents.toList().sortedByDescending { it.first.meltingPoint ?: 0 }
                    for ((smeltable, amount) in sorted) {
                        val unit = if (amount == 1) "unit" else "units"
                        val state = smeltable.getState(temperature).name.lowercase()
                        p.sendMessage("$amount $unit of $state ${smeltable.name}")
                    }
                }
            } else if (getByItem(item) is Thermometer) {
                p.sendActionBar(
                    Component.text("The crucible's temperature is %.2f°C".format(temperature))
                )
            } else if ("SHOVEL" in item.type.name && temperature <= 100 && contents.isNotEmpty()) {
                val slag = Slag.create(contents)
                block.world.dropItem(block.location.toCenterLocation(), slag)
                contents.clear()
            }
        }
    }
}