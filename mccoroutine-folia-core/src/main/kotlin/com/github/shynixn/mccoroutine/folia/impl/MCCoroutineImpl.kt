package com.github.shynixn.mccoroutine.folia.impl

import com.github.shynixn.mccoroutine.folia.CoroutineSession
import com.github.shynixn.mccoroutine.folia.MCCoroutine
import com.github.shynixn.mccoroutine.folia.listener.PluginListener
import org.bukkit.plugin.Plugin
import java.util.concurrent.ConcurrentHashMap

/**
 * A singleton implementation which keeps all coroutine sessions of all plugins.
 */
class MCCoroutineImpl : MCCoroutine {
    private val items = ConcurrentHashMap<Plugin, CoroutineSessionImpl>()

    /**
     * Get coroutine session for the given plugin.
     */
    override fun getCoroutineSession(plugin: Plugin): CoroutineSession {
        return items.computeIfAbsent(plugin) { createCoroutineSession(plugin) }
    }

    /**
     * Disables coroutine for the given plugin.
     */
    override fun disable(plugin: Plugin) {
        val session = items.remove(plugin) ?: return
        session.dispose()
    }

    /**
     * Starts a new coroutine session.
     */
    private fun createCoroutineSession(plugin: Plugin): CoroutineSessionImpl {
        if (!plugin.isEnabled) {
            throw RuntimeException("Plugin ${plugin.name} attempted to start a new coroutine session while being disabled. Dispatchers such as plugin.minecraftDispatcher and plugin.asyncDispatcher are using the BukkitScheduler, which is already disposed at this point of time. If you are starting a coroutine in onDisable, consider using runBlocking or a different plugin.mcCoroutineConfiguration.shutdownStrategy. See https://shynixn.github.io/MCCoroutine/wiki/site/plugindisable for details.")
        }

        val pluginListener = PluginListener(this, plugin)
        val coroutineFacade = MCCoroutineConfigurationImpl(plugin, this)
        plugin.server.pluginManager.registerEvents(pluginListener, plugin)

        return CoroutineSessionImpl(plugin, coroutineFacade)
    }
}
