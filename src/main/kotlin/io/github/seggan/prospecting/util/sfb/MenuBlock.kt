package io.github.seggan.prospecting.util.sfb

import io.github.seggan.prospecting.util.sfb.modules.Useable
import io.github.seggan.prospecting.util.text
import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import net.kyori.adventure.text.Component
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.AbstractGui
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.util.InventoryUtils
import xyz.xenondevs.invui.window.AbstractSingleWindow

abstract class MenuBlock<I : SlimefunItem>(block: Block, item: I) : SlimefunBlock<I>(block, item), Useable {

    abstract val gui: Gui

    private val name = sfItemInstance.item.displayName().text.drop(1).dropLast(1)

    private var invUi: Inventory by blockStorage { InventoryUtils.createMatchingInventory(gui, name) }

    override fun onInteract(e: PlayerRightClickEvent) {
        e.cancel()
        val window = BlockWindow(e.player)
        window.open()
    }

    private inner class BlockWindow(player: Player) : AbstractSingleWindow(
        player,
        AdventureComponentWrapper(Component.text(name)),
        gui as AbstractGui,
        invUi,
        true
    ) {
        override fun handleClosed() {
            super.handleClosed()
            saveData()
        }
    }
}