package com.github.ofrostdev.api.utils;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.Location;

public class SoundAPI {

    public static void play(Player player, Sound sound, float volume, float pitch) {
        if (player == null || sound == null) return;
        player.playSound(player.getLocation(), sound, volume, pitch);
    }

    public static void play(Player player, Sound sound) {
        play(player, sound, 1.0f, 1.0f);
    }

    public static void play(Player player, String soundName, float volume, float pitch) {
        try {
            Sound sound = Sound.valueOf(soundName.toUpperCase());
            play(player, sound, volume, pitch);
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().severe("Som inválido: " + soundName);
        }
    }

    public static void playToAll(Sound sound, float volume, float pitch) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            play(player, sound, volume, pitch);
        }
    }

    public static void playAt(Location location, Sound sound, float volume, float pitch) {
        if (location == null || sound == null) return;
        location.getWorld().playSound(location, sound, volume, pitch);
    }
}
