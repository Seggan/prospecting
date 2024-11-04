package io.github.seggan.prospecting.items

import io.github.seggan.prospecting.Prospecting
import io.github.seggan.prospecting.items.smelting.items.Slag
import io.github.seggan.prospecting.registries.Ore
import io.github.seggan.prospecting.registries.ProspectingItems
import io.github.seggan.sf4k.extensions.getSlimefun
import io.github.seggan.sf4k.extensions.plus
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.core.attributes.RecipeDisplayItem
import io.github.thebusybiscuit.slimefun4.implementation.SlimefunItems
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Item
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.BoundingBox
import java.util.concurrent.ThreadLocalRandom

class Mallet(
    itemGroup: ItemGroup,
    item: SlimefunItemStack,
    recipeType: RecipeType,
    recipe: Array<out ItemStack?>
) : SlimefunItem(itemGroup, item, recipeType, recipe), RecipeDisplayItem, Listener {

    init {
        Bukkit.getPluginManager().registerEvents(this, Prospecting())
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

        val fortune = mallet.getEnchantmentLevel(Enchantment.FORTUNE)
        val floatingItems = block.world.getNearbyEntities(BoundingBox.of(block.getRelative(BlockFace.UP)))
            .filterIsInstance<Item>()
        val top = block.location.add(0.5, 1.0, 0.5)
        for (item in floatingItems) {
            val stack = item.itemStack
            val output = getRecipeOutput(stack, fortune)
            stack.subtract()
            if (stack.amount == 0) {
                item.remove()
            } else {
                item.itemStack = stack
            }
            for (new in output) {
                top.world.dropItem(top, new)
            }
        }
    }

    private fun getRecipeOutput(stack: ItemStack, fortune: Int): List<ItemStack> {
        val id = getByItem(stack)?.id
        return when {
            stack.type == Material.COPPER_INGOT -> listOf(SlimefunItems.COPPER_INGOT.clone())
            id == SlimefunItems.COPPER_INGOT.itemId -> listOf(ItemStack(Material.COPPER_INGOT))
            id == ProspectingItems.SLAG.itemId -> Slag.getContents(stack)
            else -> {
                val ore = id?.let(Ore::getById) ?: return emptyList()
                (1..ore.crushAmount.random() + fortune).map {
                    ore.crushResult.getRandom(ThreadLocalRandom.current())
                }
            }
        }
    }

    override fun getDisplayRecipes(): MutableList<ItemStack> {
        val list = mutableListOf(
            SlimefunItems.COPPER_INGOT, ItemStack(Material.COPPER_INGOT),
            ItemStack(Material.COPPER_INGOT), SlimefunItems.COPPER_INGOT
        )
        list += Ore.entries.flatMap { ore ->
            ore.crushResult.flatMap { listOf(ore.oreItem, it.clone().add(ore.crushAmount.first)) }
        }
        return list
    }
}