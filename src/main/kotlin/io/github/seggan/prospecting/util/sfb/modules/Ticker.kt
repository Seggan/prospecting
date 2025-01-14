package io.github.seggan.prospecting.util.sfb.modules

import io.github.seggan.prospecting.util.sfb.SlimefunBlock
import io.github.seggan.prospecting.util.sfb.SlimefunBlockModule
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker
import org.bukkit.block.Block

interface Ticker : SlimefunBlockModule.ProvidingInterface {
    fun tick()

    companion object : SlimefunBlockModule {
        override fun <I : SlimefunItem> setUp(sfi: I, getBlock: (Block) -> SlimefunBlock<I>) {
            sfi.addItemHandler(object : BlockTicker() {
                override fun tick(b: Block, item: SlimefunItem, data: Config) {
                    val sfBlock = getBlock(b)
                    sfBlock as Ticker
                    sfBlock.tick()
                    sfBlock.saveData()
                }

                override fun isSynchronized(): Boolean = true
            })
        }
    }
}