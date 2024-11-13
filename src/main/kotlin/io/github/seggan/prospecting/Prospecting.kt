package io.github.seggan.prospecting

import co.aikar.commands.PaperCommandManager
import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.seggan.prospecting.ores.Ore
import io.github.seggan.prospecting.ores.gen.OreSpawnerThingy
import io.github.seggan.prospecting.registries.ProspectingItems
import io.github.seggan.prospecting.util.ArrayDequeSerializer
import io.github.seggan.sf4k.AbstractAddon
import io.github.seggan.sf4k.extensions.plus
import io.github.seggan.sf4k.serial.serializers.BukkitSerializerRegistry
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.WorldCreator
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import kotlin.collections.ArrayDeque

class Prospecting : AbstractAddon(), Listener {

    private lateinit var generator: OreSpawnerThingy

    override suspend fun onLoadAsync() {
        BukkitSerializerRegistry.edit {
            contextual(ArrayDeque::class) { ArrayDequeSerializer(it.first()) }
        }
    }

    override suspend fun onEnableAsync() {
        instance_ = this

        saveDefaultConfig()

        Ore.loadFromConfig(getConfigOrCopy("ores.json"))

        ProspectingItems.initExtra()

        val oregenWorlds = config.getStringList("oregen.worlds").toSet()
        for (world in oregenWorlds) {
            WorldCreator(world).createWorld()
        }

        generator = OreSpawnerThingy(oregenWorlds)

        val manager = PaperCommandManager(this)
        manager.enableUnstableAPI("help")
        manager.registerCommand(PropsectingCommand)

        launch {
            Bukkit.getConsoleSender().sendMessage(
                NamedTextColor.GREEN + """
                    ################# $name $pluginVersion #################
                    
                    $name is open source, you can contribute or report bugs at $bugTrackerURL
                    Join the Slimefun Addon Community Discord: discord.gg/SqD3gg5SAU
                    
                    ###################################################
                """.trimIndent()
            )
        }
    }

    override suspend fun onDisableAsync() {
        generator.disable()
        instance_ = null
    }

    override fun getJavaPlugin(): JavaPlugin = this
    override fun getBugTrackerURL(): String = "https://github.com/Seggan/Prospecting"

    companion object {
        private var instance_: Prospecting? = null

        val instance: Prospecting
            get() = instance_ ?: error("Plugin not enabled")
    }
}

internal val pluginInstance: Prospecting
    get() = Prospecting.instance

private fun getConfigOrCopy(path: String): String {
    val file = pluginInstance.dataFolder.resolve(path)
    if (!file.exists()) {
        pluginInstance.saveResource(path, false)
    }
    return file.readText()
}