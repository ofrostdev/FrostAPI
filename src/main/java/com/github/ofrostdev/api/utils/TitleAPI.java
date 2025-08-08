package com.github.ofrostdev.api.utils;

import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class TitleAPI {

    public static void send(Player player, String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        try {
            String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            Class<?> chatComponentText = Class.forName("net.minecraft.server." + version + ".ChatComponentText");
            Class<?> iChatBaseComponent = Class.forName("net.minecraft.server." + version + ".IChatBaseComponent");

            Object titleComponent = chatComponentText.getConstructor(String.class).newInstance(title);
            Object subtitleComponent = chatComponentText.getConstructor(String.class).newInstance(subtitle);

            Class<?> packetPlayOutTitle = Class.forName("net.minecraft.server." + version + ".PacketPlayOutTitle");
            Class<?> enumTitleAction = Class.forName("net.minecraft.server." + version + ".PacketPlayOutTitle$EnumTitleAction");

            Object titlePacket = packetPlayOutTitle.getConstructor(enumTitleAction, iChatBaseComponent)
                    .newInstance(Enum.valueOf((Class<Enum>) enumTitleAction, "TITLE"), titleComponent);

            Object subtitlePacket = packetPlayOutTitle.getConstructor(enumTitleAction, iChatBaseComponent)
                    .newInstance(Enum.valueOf((Class<Enum>) enumTitleAction, "SUBTITLE"), subtitleComponent);

            Constructor<?> timingConstructor = packetPlayOutTitle.getConstructor(int.class, int.class, int.class);
            Object timingPacket = timingConstructor.newInstance(fadeIn, stay, fadeOut);

            sendPacket(player, timingPacket);
            sendPacket(player, titlePacket);
            sendPacket(player, subtitlePacket);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void send(Player player, String title, int fadeIn, int stay, int fadeOut) {
        send(player, title, "", fadeIn, stay, fadeOut);
    }

    private static void sendPacket(Player player, Object packet) {
        try {
            String version = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            Class<?> craftPlayerClass = Class.forName("org.bukkit.craftbukkit." + version + ".entity.CraftPlayer");
            Object craftPlayer = craftPlayerClass.cast(player);
            Object handle = craftPlayerClass.getMethod("getHandle").invoke(craftPlayer);
            Object playerConnection = handle.getClass().getField("playerConnection").get(handle);
            Method sendPacket = playerConnection.getClass().getMethod("sendPacket", Class.forName("net.minecraft.server." + version + ".Packet"));
            sendPacket.invoke(playerConnection, packet);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
