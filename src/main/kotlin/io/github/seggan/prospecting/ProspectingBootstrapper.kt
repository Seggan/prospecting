package io.github.seggan.prospecting

import io.papermc.paper.plugin.bootstrap.BootstrapContext
import io.papermc.paper.plugin.bootstrap.PluginBootstrap
import io.papermc.paper.plugin.bootstrap.PluginProviderContext
import org.bukkit.plugin.java.JavaPlugin

@Suppress("UnstableApiUsage")
class ProspectingBootstrapper : PluginBootstrap {
    override fun bootstrap(context: BootstrapContext) {
        // Nothing to see here
    }

    override fun createPlugin(context: PluginProviderContext): JavaPlugin {
        return Prospecting
    }
}