package com.github.ofrostdev.api.utils.packets.common

import com.github.ofrostdev.api.utils.packets.PacketUtils
import net.minecraft.server.v1_8_R3.IChatBaseComponent
import net.minecraft.server.v1_8_R3.IChatBaseComponent.ChatSerializer
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@DslMarker
annotation class TitleDSL

@TitleDSL
class TitleAPI private constructor(
    private val plugin: Plugin,
    private var title: String,
    private var subtitle: String? = null
) {
    private var sound: Sound? = null
    private val placeholders: MutableMap<String, String> = mutableMapOf()

    fun replace(target: String, replacement: String) = apply { placeholders[target] = replacement }
    fun replaceAll(replacements: Map<String, String>) = apply { placeholders.putAll(replacements) }

    fun colorize() = apply {
        title = ChatColor.translateAlternateColorCodes('&', title)
        subtitle = subtitle?.let { ChatColor.translateAlternateColorCodes('&', it) }
    }

    fun sound(sound: Sound) = apply { this.sound = sound }

    private fun applyPlaceholders(text: String?): String? {
        var result = text
        placeholders.forEach { (key, value) -> result = result?.replace(key, value) }
        return result
    }

    fun send(player: Player, fadeIn: Int = 10, stay: Int = 70, fadeOut: Int = 20) {
        if (!player.isOnline) return
        val t = applyPlaceholders(title) ?: ""
        val s = applyPlaceholders(subtitle) ?: ""

        val titleComponent: IChatBaseComponent = ChatSerializer.a("{\"text\":\"$t\"}")
        val subtitleComponent: IChatBaseComponent = ChatSerializer.a("{\"text\":\"$s\"}")

        val packetTitle = PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, titleComponent, fadeIn, stay, fadeOut)
        val packetSubtitle = PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, subtitleComponent, fadeIn, stay, fadeOut)

        PacketUtils.sendPacket(player, packetTitle)
        PacketUtils.sendPacket(player, packetSubtitle)
        sound?.let { player.playSound(player.location, it, 1.0f, 1.0f) }
    }

    fun send(players: Collection<Player>, fadeIn: Int = 10, stay: Int = 70, fadeOut: Int = 20) {
        players.forEach { send(it, fadeIn, stay, fadeOut) }
    }

    fun broadcast(fadeIn: Int = 10, stay: Int = 70, fadeOut: Int = 20) {
        send(Bukkit.getOnlinePlayers(), fadeIn, stay, fadeOut)
    }

    companion object {
        private lateinit var pluginInstance: Plugin
        private val activeTasks: MutableMap<UUID, BukkitRunnable> = ConcurrentHashMap()

        fun enable(plugin: Plugin) {
            pluginInstance = plugin
        }

        fun of(title: String, subtitle: String? = null): TitleAPI {
            check(::pluginInstance.isInitialized) { "[FrostAPI] TitleAPI instance is null!" }
            return TitleAPI(pluginInstance, title, subtitle)
        }

        fun of(plugin: Plugin, title: String, subtitle: String? = null): TitleAPI {
            return TitleAPI(plugin, title, subtitle)
        }
    }
}

fun TitleAPI.dsl(block: TitleAPI.() -> Unit): TitleAPI {
    this.block()
    return this
}

fun title(title: String, subtitle: String? = null, block: TitleAPI.() -> Unit): TitleAPI {
    return TitleAPI.of(title, subtitle).dsl(block)
}

fun title(plugin: Plugin, title: String, subtitle: String? = null, block: TitleAPI.() -> Unit): TitleAPI {
    return TitleAPI.of(plugin, title, subtitle).dsl(block)
}
