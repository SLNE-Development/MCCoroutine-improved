package com.github.shynixn.mccoroutine.velocity.dispatcher

import com.github.shynixn.mccoroutine.velocity.SuspendingPluginContainer
import com.velocitypowered.api.plugin.PluginContainer
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Runnable
import kotlin.coroutines.CoroutineContext

internal class VelocityCoroutineDispatcher(
    private val pluginContainer: PluginContainer,
    private val suspendingPluginContainer: SuspendingPluginContainer
) : CoroutineDispatcher() {
    /**
     * Returns `true` if the execution of the coroutine should be performed with [dispatch] method.
     * Velocity uses thread pools where there is no reliable way to determine if the current thread
     * belongs to the Velocity scheduler. Therefore, we always dispatch to ensure correct execution.
     * This is the safest approach and avoids potential thread-affinity issues.
     */
    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        return true
    }

    /**
     * Handles dispatching the coroutine on the correct thread.
     * Uses Velocity's scheduler API to schedule the task for execution.
     */
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        suspendingPluginContainer.server.scheduler
            .buildTask(pluginContainer, block)
            .schedule()
    }
}
