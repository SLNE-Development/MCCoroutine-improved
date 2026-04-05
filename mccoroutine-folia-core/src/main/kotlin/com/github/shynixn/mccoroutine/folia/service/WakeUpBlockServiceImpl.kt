package com.github.shynixn.mccoroutine.folia.service

import com.github.shynixn.mccoroutine.folia.extension.findClazz
import org.bukkit.plugin.Plugin
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.locks.LockSupport

/**
 * This implementation is only active during plugin startup. Does not affect the server when running.
 */
internal class WakeUpBlockServiceImpl(private val plugin: Plugin) {
    private var threadSupport: ExecutorService? = null
    private val craftSchedulerClazz by lazy {
        plugin.findClazz("org.bukkit.craftbukkit.VERSION.scheduler.CraftScheduler")
    }
    private val craftSchedulerTickField by lazy {
        val field = craftSchedulerClazz.getDeclaredField("currentTick")
        field.isAccessible = true
        field
    }
    private val craftSchedulerHeartBeatMethod by lazy {
        craftSchedulerClazz.getDeclaredMethod("mainThreadHeartbeat", Int::class.java)
    }

    /**
     * Fast-path flag: once startup is complete, ensureWakeup() returns immediately
     * with a single volatile read instead of checking multiple fields.
     */
    @Volatile
    private var startupComplete: Boolean = false

    /**
     * Enables or disables the server heartbeat hack.
     */
    @Volatile
    var isManipulatedServerHeartBeatEnabled: Boolean = false

    /**
     * Reference to the primary server thread.
     */
    @Volatile
    var primaryThread: Thread? = null

    /**
     * Calls scheduler management implementations to ensure the
     * is not sleeping if a run is scheduled by blocking.
     */
    fun ensureWakeup() {
        // Fast-path: after startup is done, return immediately with a single volatile read.
        if (startupComplete) {
            return
        }

        if (!isManipulatedServerHeartBeatEnabled) {
            val support = threadSupport
            if (support != null) {
                support.shutdown()
                threadSupport = null
            }
            // Mark startup as complete so future calls take the fast-path.
            startupComplete = true
            return
        }

        if (primaryThread == null && plugin.server.isPrimaryThread) {
            primaryThread = Thread.currentThread()
        }

        val thread = primaryThread ?: return

        if (threadSupport == null) {
            threadSupport = Executors.newFixedThreadPool(1)
        }

        threadSupport!!.submit {
            val blockingCoroutine = LockSupport.getBlocker(thread)

            if (blockingCoroutine != null) {
                val currentTick = craftSchedulerTickField.get(plugin.server.scheduler)
                craftSchedulerHeartBeatMethod.invoke(plugin.server.scheduler, currentTick)
            }
        }
    }

    /**
     * Disposes the service.
     */
    fun dispose() {
        threadSupport?.shutdown()
        startupComplete = true
    }
}
