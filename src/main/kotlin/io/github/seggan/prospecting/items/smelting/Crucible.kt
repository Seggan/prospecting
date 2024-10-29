package io.github.seggan.prospecting.items.smelting

import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.seggan.prospecting.Prospecting
import io.github.seggan.prospecting.registries.ProspectingItems
import io.github.seggan.prospecting.util.SlimefunBlock
import io.github.seggan.prospecting.util.SlimefunBlock.Companion.applySlimefunBlock
import io.github.seggan.prospecting.util.text
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
            output: Smeltable,
            temperature: Int,
            vararg inputs: Pair<Smeltable, Int>
        ) = registerRecipe(SmeltingRecipe(inputs.toList(), output, temperature))

        init {
            registerRecipe(
                Smeltable[ProspectingItems.COPPER_OXIDE]!!,
                300,
                Smeltable[ProspectingItems.COPPER_CARBONATE]!! to 1
            )
            registerRecipe(
                Smeltable[SlimefunItems.COPPER_INGOT]!!,
                1200,
                Smeltable[ProspectingItems.COPPER_OXIDE]!! to 1
            )
        }
    }

    init {
        applySlimefunBlock(::CrucibleBlock)
    }

    private inner class CrucibleBlock(block: Block) : SlimefunBlock(block) {

        val contents: MutableMap<Smeltable, Int> by blockStorage { mutableMapOf() }
        var temperature: Double by blockStorage { ROOM_TEMPERATURE }

        override fun tick() {
            // Add new items
            val items = block.world.getNearbyEntities(
                block.location.add(0.5, 0.5, 0.5),
                0.5,
                0.5,
                0.5
            ).filterIsInstance<Item>()
            for (item in items) {
                val stack = item.itemStack
                if (contents.values.sum() + stack.amount > capacity) continue
                val smeltable = Smeltable[stack] ?: continue
                contents.merge(smeltable, stack.amount, Int::plus)
                item.remove()
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

            if (temperature > ROOM_TEMPERATURE) temperature *= 0.99
        }

        override fun onInteract(e: PlayerRightClickEvent) {
            val item = e.item
            val p = e.player
            if (item.type == Material.WATER_BUCKET) {
                temperature /= 2
                if (ThreadLocalRandom.current().nextFloat() < 0.1 && p.gameMode != GameMode.CREATIVE) {
                    Prospecting.launch {
                        block.location.createExplosion(2f, true)
                    }
                }
                item.type = Material.BUCKET
            } else if (item.type == Material.TINTED_GLASS) {
                e.setUseItem(Event.Result.DENY)
                val sorted = contents.toList().sortedByDescending { it.first.meltingPoint ?: 0 }
                for ((smeltable, amount) in sorted) {
                    val unit = if (amount == 1) "unit" else "units"
                    val state = smeltable.getState(temperature).name.lowercase()
                    p.sendMessage("$amount $unit of $state ${smeltable.ingot.displayName().text}")
                }
            } else if (getByItem(item) is Thermometer) {
                p.sendActionBar(
                    Component.text("The crucible's temperature is %.2f°C".format(temperature))
                )
            }
        }
    }
}