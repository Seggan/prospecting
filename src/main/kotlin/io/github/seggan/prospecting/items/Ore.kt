package io.github.seggan.prospecting.items

import io.github.seggan.prospecting.util.key
import io.github.seggan.prospecting.util.spawn
import io.github.seggan.sf4k.extensions.position
import io.github.seggan.sf4k.item.BetterSlimefunItem
import io.github.seggan.sf4k.item.ItemHandler
import io.github.seggan.sf4k.serial.pdc.getData
import io.github.seggan.sf4k.serial.pdc.setData
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler
import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.MapMeta
import org.bukkit.map.MapCanvas
import org.bukkit.map.MapRenderer
import org.bukkit.map.MapView
import java.awt.Image
import javax.imageio.ImageIO

class Ore(
    itemGroup: ItemGroup,
    item: SlimefunItemStack,
    recipeType: RecipeType,
    recipe: Array<out ItemStack>,
    private val texture: String
) : BetterSlimefunItem(itemGroup, item, recipeType, recipe) {

    companion object : Listener {
        private val TEXTURE_KEY = "prospecting_ore_texture".key()
    }

    private lateinit var map: MapView

    @ItemHandler(BlockPlaceHandler::class)
    private fun onPlace(e: BlockPlaceEvent) {
        setTextures(e.block)
    }

    @ItemHandler(BlockBreakHandler::class)
    private fun onBreak(e: BlockBreakEvent, item: ItemStack, drops: List<ItemStack>) {
        val block = e.block
        val position = block.position
        val entities = block.world.getNearbyEntities(block.location, 1.1, 1.1, 1.1)
        for (entity in entities) {
            if (entity is ItemFrame) {
                val pos = entity.persistentDataContainer.getData<BlockPosition>(TEXTURE_KEY)
                if (pos == position) {
                    entity.remove()
                }
            }
        }
    }

    private fun setTextures(block: Block) {
        if (!::map.isInitialized) {
            map = Bukkit.createMap(block.world)
            map.renderers.clear()
            val image = ImageIO.read(javaClass.getResource("/textures/$texture.png"))
            map.addRenderer(ImageRenderer(image.getScaledInstance(128, 128, 0)))
            map.isTrackingPosition = false
            map.scale = MapView.Scale.CLOSEST
        }
        val pos = block.position
        block.breakNaturally()
        for (side in ADJACENT_BLOCKS) {
            val itemFrame = block.getRelative(side).location.spawn<ItemFrame>()
            itemFrame.persistentDataContainer.setData<BlockPosition>(TEXTURE_KEY, pos)
            itemFrame.isInvisible = true
            itemFrame.isInvulnerable = true
            itemFrame.setFacingDirection(side, true)

            val mapItem = ItemStack(Material.FILLED_MAP)
            mapItem.editMeta {
                val meta = it as MapMeta
                meta.mapView = map
            }
            itemFrame.setItem(mapItem)

            itemFrame.isFixed = true
        }
    }
}

private class ImageRenderer(private val image: Image) : MapRenderer() {
    override fun render(mapView: MapView, canvas: MapCanvas, player: Player) {
        canvas.drawImage(0, 0, image)
    }
}

private val ADJACENT_BLOCKS =
    arrayOf(BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST)