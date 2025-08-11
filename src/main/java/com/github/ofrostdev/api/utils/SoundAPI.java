package com.github.ofrostdev.api.utils;

import net.minecraft.server.v1_8_R3.PacketPlayOutNamedSoundEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.Collection;

public final class SoundAPI {

    private final String soundName;
    private float volume = 1.0f;
    private float pitch = 1.0f;
    private Location location = null;

    private SoundAPI(String soundName) {
        this.soundName = soundName;
    }

    private SoundAPI(Sound bukkitSound) {
        this.soundName = bukkitSound.name().toLowerCase().replace('_', '.');
    }

    public static SoundAPI of(String soundName) {
        return new SoundAPI(soundName);
    }

    public static SoundAPI of(Sound sound) {
        return new SoundAPI(sound);
    }

    public SoundAPI volume(float volume) {
        this.volume = volume;
        return this;
    }

    public SoundAPI pitch(float pitch) {
        this.pitch = pitch;
        return this;
    }

    public SoundAPI location(Location location) {
        this.location = location;
        return this;
    }

    public void send(Player player) {
        if (player == null || !player.isOnline()) return;
        Location loc = location != null ? location : player.getLocation();
        PacketUtils.sendPacket(player, buildPacket(loc));
    }

    public void sendTo(Collection<? extends Player> players) {
        if (players == null || players.isEmpty()) return;
        if (location != null) {
            PacketUtils.sendPacket(players, buildPacket(location));
        } else {
            for (Player p : players) {
                PacketUtils.sendPacket(p, buildPacket(p.getLocation()));
            }
        }
    }

    private PacketPlayOutNamedSoundEffect buildPacket(Location loc) {
        return new PacketPlayOutNamedSoundEffect(
                soundName,
                loc.getX(),
                loc.getY(),
                loc.getZ(),
                volume,
                pitch
        );
    }
}
