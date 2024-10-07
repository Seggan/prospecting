package io.github.seggan.prospecting

import co.aikar.commands.PaperCommandManager
import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.seggan.prospecting.gen.ProspectingGenerator
import io.github.seggan.prospecting.registries.ProspectingItems
import io.github.seggan.sf4k.AbstractAddon
import io.github.seggan.sf4k.extensions.plus
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.WorldCreator
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin

object Prospecting : AbstractAddon(), Listener {

    override suspend fun onEnableAsync() {
        WorldCreator("a")
            .generator(ProspectingGenerator)
            .createWorld()

        Bukkit.getPluginManager().registerEvents(this, this)

        val manager = PaperCommandManager(this)
        manager.enableUnstableAPI("help")
        manager.registerCommand(PropsectingCommand)

        ProspectingItems // Initialize the items

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