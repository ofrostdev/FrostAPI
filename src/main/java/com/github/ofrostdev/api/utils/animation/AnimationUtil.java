package com.github.ofrostdev.api.utils.animation;

import com.github.ofrostdev.api.task.TaskController;
import com.github.ofrostdev.api.utils.Config;
import com.github.ofrostdev.api.utils.PacketUtils;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityHeadRotation;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntity.PacketPlayOutEntityLook;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

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

    public static Packet<?> createHeadRotationPacket(ArmorStand armorStand, float headYawDegrees) {
        EntityArmorStand nmsArmorStand = ((CraftArmorStand) armorStand).getHandle();
        byte headYaw = (byte) (headYawDegrees * 256.0F / 360.0F);
        return new PacketPlayOutEntityHeadRotation(nmsArmorStand, headYaw);
    }

    public static Packet<?> createHeadRotationPacket(Entity nmsEntity, float headYawDegrees) {
        byte headYaw = (byte) (headYawDegrees * 256.0F / 360.0F);
        return new PacketPlayOutEntityHeadRotation(nmsEntity, headYaw);
    }

    public static Packet<?> createEntityLookPacket(int entityId, float yawDegrees, float pitchDegrees) {
        byte y = (byte) (yawDegrees * 256.0F / 360.0F);
        byte p = (byte) (pitchDegrees * 256.0F / 360.0F);
        return new PacketPlayOutEntityLook(entityId, y, p, true);
    }

    public static void sendHeadRotationToPlayers(Entity nmsEntity, float headYaw, Collection<? extends Player> players) {
        PacketUtils.sendPacket(players, createHeadRotationPacket(nmsEntity, headYaw));
    }

    public static void sendHeadRotationToPlayers(ArmorStand armorStand, float headYaw, Collection<? extends Player> players) {
        PacketUtils.sendPacket(players, createHeadRotationPacket(armorStand, headYaw));
    }

    public static void sendEntityLookToPlayers(int entityId, float yaw, float pitch, Collection<? extends Player> players) {
        PacketUtils.sendPacket(players, createEntityLookPacket(entityId, yaw, pitch));
    }

}
