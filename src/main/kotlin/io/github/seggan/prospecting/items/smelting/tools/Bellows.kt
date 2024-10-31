package io.github.seggan.prospecting.items.smelting.tools

import com.destroystokyo.paper.ParticleBuilder
import io.github.seggan.prospecting.items.smelting.Kiln
import io.github.seggan.sf4k.extensions.plus
import io.github.seggan.sf4k.item.BetterSlimefunItem
import io.github.seggan.sf4k.item.ItemHandler
import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent
import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockUseHandler
import me.mrCookieSlime.Slimefun.api.BlockStorage
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Particle
import org.bukkit.block.data.Rotatable
import org.bukkit.inventory.ItemStack
import kotlin.jvm.optionals.getOrNull

class Bellows(
    itemGroup: ItemGroup,
    item: SlimefunItemStack,
    recipeType: RecipeType,
    recipe: Array<out ItemStack?>
) : BetterSlimefunItem(itemGroup, item, recipeType, recipe) {

    @ItemHandler(BlockUseHandler::class)
    private fun onUse(e: PlayerRightClickEvent) {
        val p = e.player
        val block = e.clickedBlock.getOrNull() ?: return
        val kilnBlock = (block.blockData as? Rotatable)?.rotation?.oppositeFace?.let {
            block.getRelative(it)
        }
        val kiln = kilnBlock?.let { BlockStorage.check(it) } as? Kiln
        if (kilnBlock == null || kiln == null) {
            p.sendMessage(NamedTextColor.RED + "The bellows must be facing a kiln")
            return
        }
        kiln.useBellowsOn(kilnBlock)
        ParticleBuilder(Particle.SMALL_FLAME)
            .count(10)
            .location(kilnBlock.location.toCenterLocation())
            .spawn()
        e.cancel()
    }
}