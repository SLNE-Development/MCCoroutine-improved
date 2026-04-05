package com.github.shynixn.mccoroutine.folia.dispatcher

import kotlinx.coroutines.CoroutineDispatcher
import org.bukkit.plugin.Plugin
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

class MainDispatcher(
    private val plugin: Plugin,
    tickRateMs: Long,
) : CoroutineDispatcher(), AutoCloseable {
    private val executor = Executors.newScheduledThreadPool(1) { r ->
        Thread(r, "MCCoroutine-${plugin.name}-MainThread")
    }
    private val actionQueue = ConcurrentLinkedQueue<Runnable>()

    /**
     * Thread id of the dedicated main thread.
     */
    @Volatile
    var threadId = -1L

    /**
     * Pre-allocated drain list reused every tick to avoid repeated ArrayList allocation.
     */
    private val drainList = ArrayList<Runnable>(16)

    init {
        executor.submit {
            threadId = Thread.currentThread().threadId()
        }
        executor.scheduleAtFixedRate({
            drainList.clear()
            while (true) {
                val action = actionQueue.poll() ?: break
                drainList.add(action)
            }
            for (action in drainList) {
                action.run()
            }
        }, 1L, tickRateMs, TimeUnit.MILLISECONDS)
    }

    /**
     * Returns `true` if the execution of the coroutine should be performed with [dispatch] method.
     * Returns false if we are already on the main dispatcher thread, allowing the framework to
     * skip dispatch entirely and run the block inline.
     */
    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        return Thread.currentThread().threadId() != threadId
    }

    /**
     * Handles dispatching the coroutine on the correct thread.
     */
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (!plugin.isEnabled) {
            return
        }

        if (Thread.currentThread().threadId() != threadId) {
            actionQueue.add(block)
        } else {
            block.run()
        }
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     * Drains remaining queued actions and shuts down the executor immediately.
     *
     * @throws Exception if this resource cannot be closed
     */
    override fun close() {
        actionQueue.clear()
        executor.shutdownNow()
    }
}
