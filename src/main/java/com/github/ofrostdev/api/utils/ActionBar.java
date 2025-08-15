package com.github.ofrostdev.api.utils;

import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ActionBar {

    private static Plugin plugin;

    private String message;
    private Sound sound = null;

    private ActionBar(String message) {
        this.message = message;
    }

    public static void init(Plugin p) {
        if (plugin != null) return;
        if (p == null) throw new IllegalArgumentException("[FrostAPI] ActionBar -> Plugin não pode ser nulo!");
        plugin = p;
    }

    public static ActionBar of(String message) {
        return new ActionBar(message);
    }

    public ActionBar replace(String target, String replacement) {
        if (target != null && replacement != null) {
            message = message.replace(target, replacement);
        }
        return this;
    }

    public ActionBar colorize() {
        message = message.replaceAll("(?i)&([0-9a-fk-or])", "§$1");
        return this;
    }

    public ActionBar sound(Sound sound) {
        this.sound = sound;
        return this;
    }

    public void send(Player player) {
        if (player == null || !player.isOnline()) return;

        IChatBaseComponent component = new ChatComponentText(message);
        PacketPlayOutChat packet = new PacketPlayOutChat(component, (byte) 2);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);

        if (sound != null) {
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        }
    }

    public void send(Player player, int seconds) {
        if (plugin == null) throw new IllegalArgumentException("[FrostAPI] ActionBar -> Registre FrostAPI.enable(Plugin plugin) na main!");
        if (player == null || seconds <= 0) return;
        runRepeatedly(plugin, seconds, () -> send(player));
    }

    public void send(Collection<? extends Player> players) {
        players.forEach(this::send);
    }

    public void broadcast() {
        send(Bukkit.getOnlinePlayers());
    }

    public void broadcast(int seconds) {
        if (plugin == null || seconds <= 0) return;
        runRepeatedly(plugin, seconds, this::broadcast);
    }

    public static final Map<UUID, BukkitTask> activeTasks = new ConcurrentHashMap<>();

    public void sendTypewriter(Player player, String text, int delay) {
        if (plugin == null) throw new IllegalArgumentException("[FrostAPI] ActionBar -> Registre FrostAPI.enable(Plugin plugin) na main!");
        if (player == null || text == null || text.isEmpty()) return;

        UUID uuid = player.getUniqueId();

        if (activeTasks.containsKey(uuid)) {
            BukkitTask previousTask = activeTasks.remove(uuid);
            if (previousTask != null) previousTask.cancel();
        }

        String translatedText = ChatColor.translateAlternateColorCodes('&', text);

        BukkitTask task = new BukkitRunnable() {
            int index = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel();
                    activeTasks.remove(uuid);
                    return;
                }

                if (index < translatedText.length()) {
                    String partial = translatedText.substring(0, index + 1);
                    ActionBar.of(partial).send(player);
                    index++;
                } else {
                    this.cancel();
                    activeTasks.remove(uuid);
                }
            }
        }.runTaskTimer(plugin, 0L, delay);

        activeTasks.put(uuid, task);
    }

    private void runRepeatedly(Plugin p, int seconds, Runnable action) {
        new BukkitRunnable() {
            int count = seconds;
            @Override
            public void run() {
                if (count-- <= 0) cancel();
                else action.run();
            }
        }.runTaskTimer(p, 0L, 20L);
    }
}
