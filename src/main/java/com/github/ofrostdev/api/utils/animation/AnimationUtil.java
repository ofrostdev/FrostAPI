package com.github.ofrostdev.api.utils.animation;

import com.github.ofrostdev.api.utils.PacketUtils;
import net.minecraft.server.v1_8_R3.*;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntity.PacketPlayOutEntityLook;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;

public final class AnimationUtil {

    private AnimationUtil() {}

    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public static Location lerpLocation(Location a, Location b, double t) {
        Location out = a.clone();
        out.setX(a.getX() + (b.getX() - a.getX()) * t);
        out.setY(a.getY() + (b.getY() - a.getY()) * t);
        out.setZ(a.getZ() + (b.getZ() - a.getZ()) * t);
        out.setYaw((float) (a.getYaw() + (b.getYaw()-a.getYaw()) * t));
        out.setPitch((float) (a.getPitch() + (b.getPitch()-a.getPitch()) * t));
        return out;
    }

    private static Packet<?> createEntityTeleportPacket(int entityId, Location loc) {
        return new PacketPlayOutEntityTeleport(
                entityId,
                MathHelper.floor(loc.getX() * 32.0D),
                MathHelper.floor(loc.getY() * 32.0D),
                MathHelper.floor(loc.getZ() * 32.0D),
                (byte)(loc.getYaw() * 256.0F / 360.0F),
                (byte)(loc.getPitch() * 256.0F / 360.0F),
                false
        );
    }

    private static Packet<?> createEntityLookPacket(int entityId, float yawDegrees, float pitchDegrees) {
        byte y = (byte) (yawDegrees * 256.0F / 360.0F);
        byte p = (byte) (pitchDegrees * 256.0F / 360.0F);
        return new PacketPlayOutEntityLook(entityId, y, p, true);
    }

    private static Packet<?> createArmorStandSpawnPacket(EntityArmorStand armorStand) {
        return new PacketPlayOutSpawnEntityLiving(armorStand);
    }

    private static Packet<?> createEntityEquipmentPacket(int entityId, int slot, ItemStack item) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        return new PacketPlayOutEntityEquipment(entityId, slot, nmsItem);
    }

    private static Packet<?> createEntityPitchPacket(Entity entity, float pitchDegrees) {
        byte yawByte = (byte) (entity.yaw * 256.0F / 360.0F);
        byte pitchByte = (byte) (pitchDegrees * 256.0F / 360.0F);
        return new PacketPlayOutEntityLook(entity.getId(), yawByte, pitchByte, true);
    }

    public static void sendEntityPitchToPlayers(Entity entity, float pitchDegrees, Collection<? extends Player> players) {
        PacketUtils.sendPacket(players, createEntityPitchPacket(entity, pitchDegrees));
    }

    public static void sendArmorStandSpawn(EntityArmorStand armorStand, ItemStack helmet, Collection<? extends Player> players) {
        int entityId = armorStand.getId();
        PacketUtils.sendPacket(players, createArmorStandSpawnPacket(armorStand));
        PacketUtils.sendPacket(players, createEntityEquipmentPacket(entityId, 4, helmet));
    }

    public static void sendEntityLookToPlayers(int entityId, float yaw, float pitch, Collection<? extends Player> players) {
        PacketUtils.sendPacket(players, createEntityLookPacket(entityId, yaw, pitch));
    }

    public static void sendEntityTeleportToPlayers(int entityId, Location loc, Collection<? extends Player> players) {
        PacketUtils.sendPacket(players, createEntityTeleportPacket(entityId, loc));
    }


}
