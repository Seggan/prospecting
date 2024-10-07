package io.github.seggan.prospecting

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import kotlinx.coroutines.delay
import org.bukkit.Material
import org.bukkit.entity.Player

@Suppress("unused")
@CommandAlias("prospecting")
object PropsectingCommand : BaseCommand() {

    @Subcommand("nuke")
    @Description("Clears the chunk except for ores for testing purposes")
    fun clearChunk(p: Player) {
        val chunk = p.getTargetBlockExact(256)?.chunk ?: return
        Prospecting.launch {
            for (x in 0..15) {
                for (z in 0..15) {
                    for (y in chunk.world.minHeight until chunk.world.maxHeight) {
                        val block = chunk.getBlock(x, y, z)
                        if (block.type.isAir) continue
                        val type = block.type.name
                        if (!("ORE" in type || name.endsWith("SAND") || name.endsWith("GRAVEL"))) {
                            block.setType(Material.AIR, false)
                        }
                    }
                }
                delay(1.ticks)
            }
        }
    }
}