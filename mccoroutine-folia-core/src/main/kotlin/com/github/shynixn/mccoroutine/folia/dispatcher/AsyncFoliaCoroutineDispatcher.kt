package com.github.shynixn.mccoroutine.folia.dispatcher

import com.github.shynixn.mccoroutine.folia.service.WakeUpBlockServiceImpl
import kotlinx.coroutines.CoroutineDispatcher
import org.bukkit.plugin.Plugin
import kotlin.coroutines.CoroutineContext

/**
 * CraftBukkit Async ThreadPool Dispatcher. Dispatches in all cases.
 */
internal open class AsyncFoliaCoroutineDispatcher(
    private val plugin: Plugin,
    private val wakeUpBlockService: WakeUpBlockServiceImpl
) : CoroutineDispatcher() {
    /**
     * Always returns true: async tasks must always be dispatched to Folia's async scheduler,
     * regardless of the current thread.
     */
    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        wakeUpBlockService.ensureWakeup()
        return true
    }

    /**
     * Handles dispatching the coroutine on the correct thread.
     */
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        plugin.server.asyncScheduler.runNow(plugin) { block.run() }
    }
}
