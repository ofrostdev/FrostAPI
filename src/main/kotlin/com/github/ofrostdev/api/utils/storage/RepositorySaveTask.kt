package com.github.ofrostdev.api.utils.storage

import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

abstract class RepositorySaveTask<K, V>(private val cache: RepositoryCache<K, V>, private val intervalTicks: Long) :
    Runnable {
    fun start() {
        checkNotNull(plugin) { "[FrostAPI] RepositorySaveTask instance is null!" }
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this, intervalTicks, intervalTicks)
    }

    override fun run() {
        for ((key, value) in cache.asMap()) {
            try {
                save(key, value)
            } catch (e: Exception) {
                plugin!!.logger.severe("error saving cache " + key + ": " + e.message)
            }
        }
    }

    protected abstract fun save(key: K, value: V)

    abstract fun flush()

    companion object {
        private var plugin: Plugin? = null
        fun init(pl: Plugin) {
            if (plugin != null) return
            plugin = pl
        }
    }
}