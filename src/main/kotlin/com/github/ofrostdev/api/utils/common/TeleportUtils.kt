package com.github.ofrostdev.api.utils.common

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.concurrent.CompletableFuture

object TeleportUtils {
    fun safeTeleport(player: Player?, location: Location?): Boolean {
        if (player == null || location == null || location.world == null) return false
        try {
            player.teleport(location)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    fun safeTeleportAsync(player: Player?, location: Location?, plugin: Plugin?): CompletableFuture<Boolean> {
        val future = CompletableFuture<Boolean>()
        if (player == null || location == null || location.world == null) {
            future.complete(false)
            return future
        }
        Bukkit.getScheduler().runTask(plugin) { future.complete(safeTeleport(player, location)) }
        return future
    }

    fun teleportWithDelay(player: Player?, location: Location?, ticks: Long, plugin: Plugin?) {
        if (player == null || location == null || location.world == null) return
        Bukkit.getScheduler().runTaskLater(plugin, { safeTeleport(player, location) }, ticks)
    }

    fun teleportIfChunkLoaded(player: Player?, location: Location?, plugin: Plugin?) {
        if (player == null || location == null || location.world == null) return
        val chunk = location.world.getChunkAt(location)
        if (!chunk.isLoaded) chunk.load()
        safeTeleport(player, location)
    }

    fun teleportIfChunkLoadedAsync(player: Player?, location: Location?): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync {
            if (player == null || location == null || location.world == null) return@supplyAsync false
            val chunk = location.world.getChunkAt(location)
            if (!chunk.isLoaded) chunk.load()
            safeTeleport(player, location)
        }
    }

    fun teleportWithDelayIfChunkLoaded(player: Player?, location: Location?, ticks: Long, plugin: Plugin?) {
        Bukkit.getScheduler().runTaskLater(plugin, { teleportIfChunkLoaded(player, location, plugin) }, ticks)
    }
}