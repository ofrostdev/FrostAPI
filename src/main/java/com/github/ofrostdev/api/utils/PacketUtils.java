package com.github.ofrostdev.api.utils;

import net.minecraft.server.v1_8_R3.Packet;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.Collection;

public final class PacketUtils {

    private PacketUtils() {}

    public static void sendPacket(Player player, Packet<?> packet) {
        if (player != null && player.isOnline() && packet != null) {
            ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
        }
    }

    public static void sendPacket(Collection<? extends Player> players, Packet<?> packet) {
        if (players == null || packet == null) return;
        for (Player player : players) {
            sendPacket(player, packet);
        }
    }

    public static void sendPacketToAll(Packet<?> packet) {
        sendPacket(Bukkit.getOnlinePlayers(), packet);
    }
}
