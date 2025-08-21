package com.github.ofrostdev.api.utils.common

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.concurrent.CompletableFuture

@DslMarker
annotation class TeleportDSL

object TeleportUtils {

    class Builder(
        private val player: Player?,
        private val location: Location?,
        private val plugin: Plugin?
    ) {
        var async: Boolean = false
        var delay: Long = 0L
        var loadChunk: Boolean = false

        fun execute(): CompletableFuture<Boolean> {
            if (player == null || location == null || location.world == null) {
                return CompletableFuture.completedFuture(false)
            }

            val future = CompletableFuture<Boolean>()

            val task = Runnable {
                if (loadChunk) {
                    val chunk = location.world.getChunkAt(location)
                    if (!chunk.isLoaded) chunk.load()
                }
                val success = safeTeleport(player, location)
                future.complete(success)
            }

            if (async) {
                CompletableFuture.runAsync(task)
            } else if (delay > 0) {
                Bukkit.getScheduler().runTaskLater(plugin, task, delay)
            } else {
                task.run()
            }

            return future
        }
    }

    fun safeTeleport(player: Player?, location: Location?): Boolean {
        if (player == null || location == null || location.world == null) return false
        return try {
            player.teleport(location)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun dsl(player: Player?, location: Location?, plugin: Plugin?, block: Builder.() -> Unit): CompletableFuture<Boolean> {
        val builder = Builder(player, location, plugin)
        builder.block()
        return builder.execute()
    }
}
