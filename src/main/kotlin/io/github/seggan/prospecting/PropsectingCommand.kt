package io.github.seggan.prospecting

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import io.github.seggan.prospecting.registries.Ore
import kotlinx.coroutines.delay
import me.mrCookieSlime.Slimefun.api.BlockStorage
import org.bukkit.Material
import org.bukkit.entity.Player
import java.util.EnumSet

@Suppress("unused")
@CommandAlias("prospecting")
object PropsectingCommand : BaseCommand() {

    @Subcommand("nuke")
    @Description("Clears the chunks except for ores for testing purposes")
    fun clearChunk(p: Player, @Default("0") range: Int) {
        val ores = Ore.entries.map { it.oreId }.toSet()
        val firstChunk = p.getTargetBlockExact(256)?.chunk ?: return
        for (cx in -range..range) {
            for (cz in -range..range) {
                val chunk = firstChunk.world.getChunkAt(firstChunk.x + cx, firstChunk.z + cz)
                Prospecting.launch {
                    for (x in 0..15) {
                        for (z in 0..15) {
                            for (y in chunk.world.minHeight until chunk.world.maxHeight) {
                                val block = chunk.getBlock(x, y, z)
                                if (block.isEmpty) continue
                                if (BlockStorage.checkID(block) !in ores && block.type !in keepBlocks) {
                                    block.setType(Material.AIR, false)
                                }
                            }
                            delay(1.ticks)
                        }
                    }
                }
            }
        }
    }
}

private val keepBlocks = EnumSet.of(
    Material.SUSPICIOUS_SAND,
    Material.SUSPICIOUS_GRAVEL,
)