package com.github.ofrostdev.api.utils.packets.common

import com.github.ofrostdev.api.FrostAPI
import com.github.ofrostdev.api.utils.packets.PacketUtils
import me.clip.placeholderapi.PlaceholderAPI
import net.minecraft.server.v1_8_R3.IChatBaseComponent
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerListHeaderFooter
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player

@DslMarker
annotation class TablistDSL

@TablistDSL
class Tablist(private val player: Player) {

    private var headerLines: List<String> = emptyList()
    private var footerLines: List<String> = emptyList()
    private var usePlaceholderAPI: Boolean = FrostAPI.getManager().hasPlugin("PlaceholderAPI")

    fun header(vararg lines: String) = apply { headerLines = lines.toList() }
    fun footer(vararg lines: String) = apply { footerLines = lines.toList() }

    private fun applyPlaceholders(text: String): String {
        var result = text
        if (usePlaceholderAPI) result = PlaceholderAPI.setPlaceholders(player, result)
        return ChatColor.translateAlternateColorCodes('&', result)
    }

    private fun buildString(lines: List<String>): String {
        return lines.joinToString("\n") { applyPlaceholders(it) }
    }

    fun send() {
        if (!player.isOnline) return

        val packet = PacketPlayOutPlayerListHeaderFooter()
        try {
            val headerField = packet.javaClass.getDeclaredField("a")
            val footerField = packet.javaClass.getDeclaredField("b")
            headerField.isAccessible = true
            footerField.isAccessible = true

            headerField.set(packet, IChatBaseComponent.ChatSerializer.a("{\"text\":\"${buildString(headerLines)}\"}"))
            footerField.set(packet, IChatBaseComponent.ChatSerializer.a("{\"text\":\"${buildString(footerLines)}\"}"))

            headerField.isAccessible = false
            footerField.isAccessible = false
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        PacketUtils.sendPacket(player, packet)
    }

    private fun convertListToString(list: List<String>) =
        list.joinToString("\n") { ChatColor.translateAlternateColorCodes('&', it) }
}

fun tablist(player: Player, block: Tablist.() -> Unit) {
    Tablist(player).apply(block).send()
}
