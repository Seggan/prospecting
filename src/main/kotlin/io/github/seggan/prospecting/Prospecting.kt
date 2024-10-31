package io.github.seggan.prospecting

import co.aikar.commands.PaperCommandManager
import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.seggan.prospecting.gen.OreGenerator
import io.github.seggan.prospecting.registries.ProspectingItems
import io.github.seggan.prospecting.util.ArrayDequeSerializer
import io.github.seggan.sf4k.AbstractAddon
import io.github.seggan.sf4k.extensions.plus
import io.github.seggan.sf4k.serial.serializers.BukkitSerializerRegistry
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import kotlin.collections.ArrayDeque

object Prospecting : AbstractAddon(), Listener {

    override suspend fun onLoadAsync() {
        BukkitSerializerRegistry.edit {
            contextual(ArrayDeque::class) { ArrayDequeSerializer(it.first()) }
        }
    }

    override suspend fun onEnableAsync() {
        ProspectingItems.initExtra()

//        OreGenerator(setOf("a"))
//        WorldCreator("a").createWorld()
        OreGenerator(setOf("world"))

        val manager = PaperCommandManager(this)
        manager.enableUnstableAPI("help")
        manager.registerCommand(PropsectingCommand)

        launch {
            Bukkit.getConsoleSender().sendMessage(
                NamedTextColor.GREEN + """################# $name $pluginVersion #################
                
                $name is open source, you can contribute or report bugs at $bugTrackerURL
                Join the Slimefun Addon Community Discord: discord.gg/SqD3gg5SAU
                
                ###################################################""".trimIndent()
            )
        }
    }

    override fun getJavaPlugin(): JavaPlugin = this
    override fun getBugTrackerURL(): String = "https://github.com/Seggan/Prospecting"
}