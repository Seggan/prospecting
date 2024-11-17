package io.github.seggan.prospecting.items.smelting

import io.github.seggan.prospecting.util.SlimefunBlock
import io.github.seggan.prospecting.util.secondsToSfTicks
import io.github.seggan.sf4k.extensions.plus
import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import me.mrCookieSlime.Slimefun.api.BlockStorage
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.UUID

class Mold(
    itemGroup: ItemGroup,
    item: SlimefunItemStack,
    recipeType: RecipeType,
    recipe: Array<out ItemStack?>
) : SlimefunItem(itemGroup, item, recipeType, recipe) {

    init {
        SlimefunBlock.applyBlock(this, ::MoldBlock)
    }

    private inner class MoldBlock(block: Block) : SlimefunBlock(block) {

        private var chemical: Chemical? by blockStorage { null }
        private var time: Int by blockStorage { 0 }
        private var lavaDisplay: UUID? by blockStorage { null }

        override fun tick() {
            if (chemical != null) {
                time--
                if (time <= 0) {
                    block.world.dropItem(block.location.add(0.5, 0.5, 0.5), chemical!!.ingot)
                    lavaDisplay?.let(block.world::getEntity)?.remove()
                    chemical = null
                }
            }
        }

        override fun onInteract(e: PlayerRightClickEvent) {
            e.cancel()
            if (chemical != null) return
            for (face in orthogonal) {
                val crucibleBlock = block.getRelative(face)
                val crucible = BlockStorage.check(crucibleBlock) as? Crucible ?: continue
                val chemical = crucible.cast(crucibleBlock)
                if (chemical != null) {
                    this.chemical = chemical
                    time = secondsToSfTicks(10)

                    val display = block.world.spawn(
                        block.location.add(0.5 - 2.pixels, 0.25 - 2.pixels, 0.5 - 2.pixels),
                        BlockDisplay::class.java
                    )
                    display.block = Material.MAGMA_BLOCK.createBlockData()
                    val transform = display.transformation
                    transform.scale.set(4.pixels)
                    display.transformation = transform
                    lavaDisplay = display.uniqueId
                } else {
                    e.player.sendMessage(NamedTextColor.RED + "There is nothing that is able to be cast in the crucible.")
                }
                return
            }
            e.player.sendMessage(NamedTextColor.RED + "The mold needs to be next to a crucible.")
        }

        override fun onBreak(p: Player?, drops: MutableList<ItemStack>) {
            lavaDisplay?.let(block.world::getEntity)?.remove()
            if (chemical != null) {
                block.type = Material.LAVA
            }
        }
    }
}

private inline val Int.pixels: Float get() = this / 16f
private val orthogonal = arrayOf(BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST)