package io.github.seggan.prospecting.util

import io.github.seggan.sf4k.extensions.position
import io.github.seggan.sf4k.serial.blockstorage.BlockStorageDecoder
import io.github.seggan.sf4k.serial.blockstorage.BlockStorageEncoder
import io.github.seggan.sf4k.serial.serializers.BukkitSerializerRegistry
import io.github.thebusybiscuit.slimefun4.api.events.PlayerRightClickEvent
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockUseHandler
import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition
import kotlinx.serialization.KSerializer
import me.mrCookieSlime.CSCoreLibPlugin.Configuration.Config
import me.mrCookieSlime.Slimefun.Objects.handlers.BlockTicker
import me.mrCookieSlime.Slimefun.api.BlockStorage
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack
import kotlin.jvm.optionals.getOrNull
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

abstract class SlimefunBlock(val block: Block) {

    @PublishedApi
    internal val bsValues = mutableListOf<BlockStorageValue<*>>()

    open fun onPlace(p: Player) {}

    open fun onBreak(p: Player?, drops: MutableList<ItemStack>) {}

    open fun onInteract(e: PlayerRightClickEvent) {}

    open fun tick() {}

    protected inline fun <reified T> blockStorage(noinline default: () -> T) =
        PropertyDelegateProvider<Any?, BlockStorageValue<T>> { _, prop ->
            BlockStorageValue(prop.name, BukkitSerializerRegistry.serializer<T>(), default)
                .also(bsValues::add)
        }

    inner class BlockStorageValue<T>(
        private val key: String,
        private val serializer: KSerializer<T>,
        private val default: () -> T
    ) : ReadWriteProperty<Any?, T> {

        private var value: Any? = EMPTY

        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            if (value == EMPTY) {
                val encoded = BlockStorage.getLocationInfo(block.location, key)
                value = if (!encoded.isNullOrEmpty()) {
                    BlockStorageDecoder.decode(serializer, encoded)
                } else {
                    default()
                }
            }
            @Suppress("UNCHECKED_CAST")
            return value as T
        }

        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            this.value = value
        }

        fun save() {
            if (value != EMPTY) {
                val encoded = if (value != null) {
                    @Suppress("UNCHECKED_CAST")
                    BlockStorageEncoder.encode(serializer, value as T)
                } else {
                    ""
                }
                BlockStorage.addBlockInfo(block, key, encoded)
            }
            value = EMPTY
        }
    }

    fun saveData() {
        for (value in bsValues) {
            value.save()
        }
    }

    companion object {
        inline fun SlimefunItem.applySlimefunBlock(crossinline blockCons: (Block) -> SlimefunBlock) {
            val blocks = mutableMapOf<BlockPosition, SlimefunBlock>()
            addItemHandler(object : BlockPlaceHandler(false) {
                override fun onPlayerPlace(e: BlockPlaceEvent) {
                    val block = e.blockPlaced
                    val sfBlock = blocks.getOrPut(block.position) { blockCons(block) }
                    sfBlock.onPlace(e.player)
                    sfBlock.saveData()
                }
            })

            addItemHandler(object : BlockBreakHandler(false, true) {
                override fun onPlayerBreak(e: BlockBreakEvent, item: ItemStack, drops: MutableList<ItemStack>) {
                    val block = e.block
                    val sfBlock = blocks.getOrPut(block.position) { blockCons(block) }
                    sfBlock.onBreak(e.player, drops)
                    if (e.isCancelled) {
                        sfBlock.saveData()
                    } else {
                        blocks.remove(block.position)
                    }
                }

                override fun onExplode(b: Block, drops: MutableList<ItemStack>) {
                    blocks.remove(b.position)?.onBreak(null, drops)
                }
            })

            addItemHandler(object : BlockUseHandler {
                override fun onRightClick(e: PlayerRightClickEvent) {
                    val block = e.clickedBlock.getOrNull() ?: return
                    val sfBlock = blocks.getOrPut(block.position) { blockCons(block) }
                    sfBlock.onInteract(e)
                    sfBlock.saveData()
                }
            })

            addItemHandler(object : BlockTicker() {
                override fun tick(b: Block, item: SlimefunItem, data: Config) {
                    val sfBlock = blocks.getOrPut(b.position) { blockCons(b) }
                    sfBlock.tick()
                    sfBlock.saveData()
                }

                override fun isSynchronized(): Boolean = true
            })
        }
    }
}

private object EMPTY