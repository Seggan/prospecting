package io.github.seggan.prospecting.util.sfb.modules

import io.github.seggan.prospecting.util.sfb.SlimefunBlock
import io.github.seggan.prospecting.util.sfb.SlimefunBlockModule
import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockUseHandler
import org.bukkit.block.Block

interface Useable : SlimefunBlockModule.ProvidingInterface {
    fun onInteract(e: PlayerRightClickEvent)

    companion object : SlimefunBlockModule {
        override fun setUp(sfi: SlimefunItem, getBlock: (Block) -> SlimefunBlock) {
            sfi.addItemHandler(object : BlockUseHandler {
                override fun onRightClick(e: PlayerRightClickEvent) {
                    val block = getBlock(e.clickedBlock.get())
                    block as Useable
                    block.onInteract(e)
                    block.saveData()
                }
            })
        }
    }
}