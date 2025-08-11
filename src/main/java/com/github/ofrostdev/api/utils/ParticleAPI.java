package com.github.ofrostdev.api.utils;

import net.minecraft.server.v1_8_R3.EnumParticle;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldParticles;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Collection;

public final class ParticleAPI {

    private EnumParticle particle;
    private Location location;
    private float offsetX, offsetY, offsetZ;
    private float speed;
    private int count;

    private ParticleAPI(EnumParticle particle) {
        this.particle = particle;
        this.offsetX = 0f;
        this.offsetY = 0f;
        this.offsetZ = 0f;
        this.speed = 0f;
        this.count = 1;
    }

    public static ParticleAPI of(EnumParticle particle) {
        return new ParticleAPI(particle);
    }

    public ParticleAPI location(Location location) {
        this.location = location;
        return this;
    }

    public ParticleAPI offset(float x, float y, float z) {
        this.offsetX = x;
        this.offsetY = y;
        this.offsetZ = z;
        return this;
    }

    public ParticleAPI speed(float speed) {
        this.speed = speed;
        return this;
    }

    public ParticleAPI count(int count) {
        this.count = count;
        return this;
    }

    public void send(Player player) {
        if (player == null || location == null) return;
        PacketUtils.sendPacket(player, buildPacket());
    }

    public void sendToAll() {
        if (location == null) return;
        PacketUtils.sendPacketToAll(buildPacket());
    }

    public void send(Collection<? extends Player> players) {
        if (location == null) return;
        PacketUtils.sendPacket(players, buildPacket());
    }

    public void send(double radius) {
        if (location == null) return;
        double r2 = radius * radius;
        Collection<? extends Player> players = location.getWorld().getPlayers();
        for (Player player : players) {
            if (player.getLocation().distanceSquared(location) <= r2) {
                send(player);
            }
        }
    }

    public <T extends Entity> void send(Class<T> clazz) {
        if (location == null) return;
        for (Entity entity : location.getWorld().getEntities()) {
            if (clazz.isInstance(entity) && entity instanceof Player) {
                send((Player) entity);
            }
        }
    }

    private PacketPlayOutWorldParticles buildPacket() {
        return new PacketPlayOutWorldParticles(
                particle,
                true,
                (float) location.getX(),
                (float) location.getY(),
                (float) location.getZ(),
                offsetX,
                offsetY,
                offsetZ,
                speed,
                count
        );
    }
}
