package io.github.seggan.prospecting.util.sfb

import com.google.common.collect.Sets
import io.github.seggan.sf4k.extensions.position
import io.github.seggan.sf4k.serial.blockstorage.BlockStorageDecoder
import io.github.seggan.sf4k.serial.blockstorage.BlockStorageEncoder
import io.github.seggan.sf4k.serial.blockstorage.BlockStorageSettings
import io.github.seggan.sf4k.serial.serializers.BukkitSerializerRegistry
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockBreakHandler
import io.github.thebusybiscuit.slimefun4.core.handlers.BlockPlaceHandler
import io.github.thebusybiscuit.slimefun4.libraries.dough.blocks.BlockPosition
import kotlinx.serialization.KSerializer
import me.mrCookieSlime.Slimefun.api.BlockStorage
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.companionObjectInstance
import kotlin.reflect.full.isSubclassOf

abstract class SlimefunBlock(val block: Block) : AutoCloseable {

    @PublishedApi
    internal val bsValues = mutableListOf<BlockStorageValue<*>>()

    protected open val blockStorageSettings = BlockStorageSettings()

    open fun onPlace(p: Player) {}

    open fun onBreak(p: Player?, drops: MutableList<ItemStack>) {}

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
                    BlockStorageDecoder.Companion.decode(serializer, encoded, blockStorageSettings)
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
                @Suppress("UNCHECKED_CAST")
                val encoded = BlockStorageEncoder.Companion.encode(serializer, value as T, blockStorageSettings)
                BlockStorage.addBlockInfo(block, key, encoded)
            }
            // Forge GC
            value = EMPTY
        }
    }

    fun saveData() {
        for (value in bsValues) {
            value.save()
        }
    }

    override fun close() = saveData()

    companion object {
        inline fun <reified T : SlimefunBlock> applyBlock(
            item: SlimefunItem,
            crossinline blockCons: (Block) -> T
        ) {
            val blocks = mutableMapOf<BlockPosition, SlimefunBlock>()
            val getBlock = { b: Block -> blocks.getOrPut(b.position) { blockCons(b) } }

            val alreadySetUp = Sets.newIdentityHashSet<SlimefunBlockModule>()
            for (superType in T::class.allSuperclasses) {
                if (
                    superType.java.isInterface
                    &&
                    superType.isSubclassOf(SlimefunBlockModule.ProvidingInterface::class)
                    &&
                    superType != SlimefunBlockModule.ProvidingInterface::class
                ) {
                    val companion = superType.companionObjectInstance
                    check(companion is SlimefunBlockModule) {
                        "$superType companion object must implement SlimefunBlockModule"
                    }
                    if (alreadySetUp.add(companion)) {
                        companion.setUp(item, getBlock)
                    }
                }
            }

            item.addItemHandler(object : BlockPlaceHandler(false) {
                override fun onPlayerPlace(e: BlockPlaceEvent) {
                    val block = e.blockPlaced
                    val sfBlock = blocks.getOrPut(block.position) { blockCons(block) }
                    sfBlock.onPlace(e.player)
                    sfBlock.saveData()
                }
            })

            item.addItemHandler(object : BlockBreakHandler(false, true) {
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
        }
    }
}

private object EMPTY