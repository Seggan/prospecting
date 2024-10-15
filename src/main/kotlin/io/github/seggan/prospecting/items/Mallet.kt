package io.github.seggan.prospecting.items

import com.destroystokyo.paper.ParticleBuilder
import io.github.seggan.prospecting.Prospecting
import io.github.seggan.sf4k.extensions.getSlimefun
import io.github.seggan.sf4k.extensions.plus
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem
import io.github.thebusybiscuit.slimefun4.utils.SlimefunUtils
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.BlockFace
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Item
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.BoundingBox

class Mallet(
    itemGroup: ItemGroup,
    item: SlimefunItemStack,
    recipeType: RecipeType,
    recipe: Array<out ItemStack>
) : SlimefunItem(itemGroup, item, recipeType, recipe), RecipeDisplayItem, Listener {

    init {
        Bukkit.getPluginManager().registerEvents(this, Prospecting)
    }

    companion object {

        private val recipes = mutableListOf<Pair<List<ItemStack>, ItemStack>>()

        fun registerRecipe(inputs: List<ItemStack>, output: ItemStack) {
            recipes.add(inputs to output)
        }
    }

    @EventHandler
    private fun onMalletUse(e: PlayerInteractEvent) {
        val block = e.clickedBlock ?: return
        val mallet = e.item
        if (mallet == null || mallet.getSlimefun<Mallet>() == null) return
        e.setUseItemInHand(Event.Result.DENY)
        e.setUseInteractedBlock(Event.Result.DENY)
        if (e.blockFace != BlockFace.UP) return

        if (block.type.hardness < 1.5 && block.blockData.isPreferredTool(ItemStack(Material.NETHERITE_PICKAXE))) {
            e.player.sendMessage(NamedTextColor.RED + "You need to use a harder block for the mallet!")
            return
        }

        val floatingItems = block.world.getNearbyEntities(BoundingBox.of(block.getRelative(BlockFace.UP)))
            .filterIsInstance<Item>()
        val top = block.location.add(0.5, 1.0, 0.5)
        recipeLoop@for ((inputs, output) in recipes) {
            val nonMatching = floatingItems.toMutableList()
            val matching = mutableListOf<Item>()
            for (item in inputs) {
                val found = nonMatching.firstOrNull { SlimefunUtils.isItemSimilar(item, it.itemStack, false, false) }
                if (found != null) {
                    matching.add(found)
                    nonMatching.remove(found)
                } else {
                    continue@recipeLoop
                }
            }
            if (matching.size == inputs.size) {
                for (item in matching) {
                    val stack = item.itemStack.subtract()
                    if (stack.amount == 0) {
                        item.remove()
                    } else {
                        item.itemStack = stack
                    }
                }
                val dropped = output.clone()
                dropped.amount = (1..mallet.getEnchantmentLevel(Enchantment.FORTUNE) + 1).random()
                mallet.damage(1, e.player)
                block.world.dropItem(top, dropped)
                ParticleBuilder(Particle.BLOCK)
                    .location(top)
                    .data(block.blockData)
                    .count(10)
                    .spawn()
                return
            }
        }
    }

    override fun getDisplayRecipes(): MutableList<ItemStack> {
        return recipes.flatMap { (inputs, output) -> listOf(inputs.first(), output) }.toMutableList()
    }
}