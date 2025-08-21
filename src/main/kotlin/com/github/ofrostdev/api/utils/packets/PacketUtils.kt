package com.github.ofrostdev.api.utils.packets

import net.minecraft.server.v1_8_R3.Packet
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer
import org.bukkit.entity.Player

object PacketUtils {
    fun sendPacket(player: Player?, packet: Packet<*>?) {
        if (player != null && player.isOnline && packet != null) {
            (player as CraftPlayer).handle.playerConnection.sendPacket(packet)
        }
    }

    fun sendPacket(players: Collection<Player?>?, packet: Packet<*>?) {
        if (players == null || packet == null) return
        for (player in players) {
            sendPacket(player, packet)
        }
    }

    fun sendPacketToAll(packet: Packet<*>?) {
        sendPacket(Bukkit.getOnlinePlayers(), packet)
    }
}