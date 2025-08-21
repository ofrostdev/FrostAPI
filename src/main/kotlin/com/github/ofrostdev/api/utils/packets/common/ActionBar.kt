package com.github.ofrostdev.api.utils.packets.common

import net.minecraft.server.v1_8_R3.ChatComponentText
import net.minecraft.server.v1_8_R3.IChatBaseComponent
import net.minecraft.server.v1_8_R3.PacketPlayOutChat
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import com.github.ofrostdev.api.utils.packets.PacketUtils

@DslMarker
annotation class ActionBarDSL

@ActionBarDSL
class ActionBar(private val plugin: Plugin) {
    var message: String = ""
    private var sound: Sound? = null
    private val placeholders: MutableMap<String, String> = mutableMapOf()

    fun replace(target: String, replacement: String) = apply { placeholders[target] = replacement }
    fun replaceAll(map: Map<String, String>) = apply { placeholders.putAll(map) }
    fun colorize() = apply { message = ChatColor.translateAlternateColorCodes('&', message) }
    fun sound(sound: Sound) = apply { this.sound = sound }

    private fun applyPlaceholders(): String {
        var result = message
        placeholders.forEach { (k, v) -> result = result.replace(k, v) }
        return result
    }

    fun send(player: Player) {
        if (!player.isOnline) return
        val msg = applyPlaceholders()
        val component: IChatBaseComponent = ChatComponentText(msg)
        val packet = PacketPlayOutChat(component, 2.toByte())
        PacketUtils.sendPacket(player, packet)
        sound?.let { player.playSound(player.location, it, 1f, 1f) }
    }

    fun send(players: Collection<Player>) = players.forEach { send(it) }
    fun broadcast() = send(Bukkit.getOnlinePlayers())
    fun broadcast(seconds: Int) {
        if (seconds <= 0) return
        repeatSend(seconds) { broadcast() }
    }

    fun send(player: Player, seconds: Int) {
        if (seconds <= 0) return
        repeatSend(seconds) { send(player) }
    }

    fun sendTypewriter(player: Player, delay: Int) {
        if (delay <= 0) return
        val uuid = player.uniqueId
        activeTasks[uuid]?.cancel()
        activeTasks.remove(uuid)

        val text = applyPlaceholders()
        var index = 0

        val task = object : BukkitRunnable() {
            override fun run() {
                if (!player.isOnline) { cancel(); activeTasks.remove(uuid); return }
                if (index < text.length) {
                    val partial = text.substring(0, index + 1)
                    ActionBar.of(plugin, partial).colorize().sound(sound ?: return).send(player)
                    index++
                } else {
                    cancel()
                    activeTasks.remove(uuid)
                }
            }
        }.runTaskTimer(plugin, 0L, delay.toLong())

        activeTasks[uuid] = task
    }

    private fun repeatSend(seconds: Int, action: () -> Unit) {
        object : BukkitRunnable() {
            var count = seconds
            override fun run() {
                if (count-- <= 0) cancel() else action()
            }
        }.runTaskTimer(plugin, 0L, 20L)
    }

    companion object {
        private lateinit var pluginInstance: Plugin
        val activeTasks: MutableMap<UUID, BukkitTask> = ConcurrentHashMap()

        fun enable(plugin: Plugin) { pluginInstance = plugin }
        fun of(message: String): ActionBar {
            check(::pluginInstance.isInitialized) { "[FrostAPI] ActionBar instance is null!" }
            return ActionBar(pluginInstance).apply { this.message = message }
        }
        fun of(plugin: Plugin, message: String) = ActionBar(plugin).apply { this.message = message }
    }
}

fun actionBar(plugin: Plugin, block: ActionBar.() -> Unit) {
    val bar = ActionBar(plugin)
    bar.block()
}
