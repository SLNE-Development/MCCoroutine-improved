package com.github.shynixn.mccoroutine.folia.dispatcher

import com.github.shynixn.mccoroutine.folia.service.WakeUpBlockServiceImpl
import kotlinx.coroutines.CoroutineDispatcher
import org.bukkit.World
import org.bukkit.plugin.Plugin
import kotlin.coroutines.CoroutineContext

internal open class RegionDispatcher(
    private val plugin: Plugin,
    private val wakeUpBlockService: WakeUpBlockServiceImpl,
    private val world: World,
    private val chunkX: Int,
    private val chunkZ: Int
) : CoroutineDispatcher() {
    /**
     * Returns false if the current thread already owns this region, allowing inline execution.
     */
    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        wakeUpBlockService.ensureWakeup()
        return !plugin.server.isOwnedByCurrentRegion(world, chunkX, chunkZ)
    }

    /**
     * Handles dispatching the coroutine on the correct thread.
     */
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        plugin.server.regionScheduler.execute(plugin, world, chunkX, chunkZ, block)
    }
}
