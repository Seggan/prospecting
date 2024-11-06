package io.github.seggan.prospecting

import co.aikar.commands.PaperCommandManager
import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.seggan.prospecting.ores.OreSpawnerThingy
import io.github.seggan.prospecting.ores.OreWorld
import io.github.seggan.prospecting.registries.ProspectingItems
import io.github.seggan.prospecting.util.ArrayDequeSerializer
import io.github.seggan.sf4k.AbstractAddon
import io.github.seggan.sf4k.extensions.plus
import io.github.seggan.sf4k.serial.serializers.BukkitSerializerRegistry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.WorldCreator
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import kotlin.collections.ArrayDeque
import kotlin.time.Duration.Companion.minutes

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

        ProspectingItems.initExtra()

        val oregenWorlds = config.getStringList("oregen.worlds").toSet()
        for (world in oregenWorlds) {
            OreWorld.getWorld(WorldCreator(world).createWorld()!!)
        }

        generator = OreSpawnerThingy(oregenWorlds)

        val manager = PaperCommandManager(this)
        manager.enableUnstableAPI("help")
        manager.registerCommand(PropsectingCommand)

        launch(Dispatchers.IO) {
            while (isEnabled) {
                delay(5.minutes)
                OreWorld.saveAll()
            }
        }

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
        OreWorld.saveAll()
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