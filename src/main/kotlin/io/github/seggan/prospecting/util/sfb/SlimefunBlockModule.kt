package io.github.seggan.prospecting.util.sfb

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import org.bukkit.block.Block

interface SlimefunBlockModule {

    interface ProvidingInterface

    fun <I : SlimefunItem> setUp(sfi: I, getBlock: (Block) -> SlimefunBlock<I>)
}