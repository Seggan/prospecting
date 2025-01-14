package io.github.seggan.prospecting

import co.aikar.commands.PaperCommandManager
import com.github.shynixn.mccoroutine.bukkit.launch
import io.github.seggan.prospecting.core.Chemical
import io.github.seggan.prospecting.items.LiquidChemicalHolder
import io.github.seggan.prospecting.items.smelting.Crucible
import io.github.seggan.prospecting.items.smelting.Kiln
import io.github.seggan.prospecting.ore.Ore
import io.github.seggan.prospecting.ore.gen.OreSpawnerThingy
import io.github.seggan.prospecting.registries.ProspectingItems
import io.github.seggan.prospecting.util.ArrayDequeSerializer
import io.github.seggan.prospecting.util.serial.InventorySerializer
import io.github.seggan.sf4k.AbstractAddon
import io.github.seggan.sf4k.extensions.plus
import io.github.seggan.sf4k.serial.serializers.BukkitSerializerRegistry
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.plus
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.WorldCreator
import org.bukkit.event.Listener
import org.bukkit.inventory.Inventory
import org.bukkit.plugin.java.JavaPlugin
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.outputStream

class Prospecting : AbstractAddon(), Listener {

    private val generators = mutableListOf<OreSpawnerThingy>()

    private lateinit var resourceList: Set<String>

    override suspend fun onLoadAsync() {
        BukkitSerializerRegistry.edit {
            contextual(ArrayDeque::class) { ArrayDequeSerializer(it.first()) }
            contextual(Inventory::class, InventorySerializer)
        }

        resourceList = Prospecting::class.java.getResource("/resources.txt")!!.readText().lines().toSet()
    }

    override suspend fun onEnableAsync() {
        instance_ = this

        saveDefaultConfig()

        ProspectingItems.initExtra()

        Chemical.loadFromConfigs(copyConfig("chemicals"))
        Ore.loadFromConfigs(copyConfig("ores"))

        Crucible.initRecipes(copyConfig("smelting"))
        Kiln.initFuels()

        for (ore in Ore.entries) {
            ore.registerItems(this)
        }

        for (chemical in Chemical.all) {
            if (chemical.meltingPoint < Int.MAX_VALUE) {
                LiquidChemicalHolder(chemical).register(this)
            }
        }

        val oregenWorlds = config.getStringList("oregen.worlds").toSet()
        for (world in oregenWorlds) {
            WorldCreator(world).createWorld()
            generators += OreSpawnerThingy(world)
        }

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
        for (generator in generators) {
            generator.disable()
        }
        instance_ = null
    }

    private fun copyConfig(path: String): Path {
        logger.info("Copying $path")
        val dir = pluginInstance.dataFolder.toPath().resolve(path)
        dir.createDirectories()
        for (resource in resourceList) {
            if (resource.startsWith("$path/")) {
                val resUrl = Prospecting::class.java.getResource("/$resource")!!
                val resPath = dir.resolve(resource.substringAfter('/'))
                if (!resPath.exists()) {
                    resUrl.openStream().use { it.copyTo(resPath.outputStream()) }
                }
            }
        }
        return dir
    }

    override fun getJavaPlugin(): JavaPlugin = this
    override fun getBugTrackerURL(): String = "https://github.com/Seggan/Prospecting"

    companion object {
        private var instance_: Prospecting? = null

        val instance: Prospecting
            get() = instance_ ?: error("Plugin not enabled")

        @OptIn(ExperimentalSerializationApi::class)
        internal val json = Json {
            serializersModule += BukkitSerializerRegistry.serializersModule
            ignoreUnknownKeys = true
            decodeEnumsCaseInsensitive = true
            allowComments = true
            isLenient = true
            allowTrailingComma = true
            allowSpecialFloatingPointValues = true
        }
    }
}

internal inline val pluginInstance: Prospecting
    get() = Prospecting.instance