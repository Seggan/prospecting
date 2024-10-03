package io.github.seggan.prospecting

import io.github.seggan.sf4k.AbstractAddon
import org.bukkit.plugin.java.JavaPlugin

object Prospecting : AbstractAddon() {

    override fun getJavaPlugin(): JavaPlugin = this

    override fun getBugTrackerURL(): String = "https://github.com/Seggan/Prospecting"
}