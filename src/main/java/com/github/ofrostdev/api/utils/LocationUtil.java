package com.github.ofrostdev.api.utils;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class LocationUtil {

    private static final SectionBuilder.LocationAdapter LOCATION_ADAPTER = new SectionBuilder.LocationAdapter();

    public static String serialize(Location location) {
        if (location == null || location.getWorld() == null) return null;
        return location.getWorld().getName() + ":" +
                location.getX() + ":" +
                location.getY() + ":" +
                location.getZ() + ":" +
                location.getYaw() + ":" +
                location.getPitch();
    }

    public static Location deserialize(String input) {
        if (input == null || input.isEmpty()) return null;
        return LOCATION_ADAPTER.supply(input);
    }

    public static boolean isSameBlock(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) return false;
        if (!loc1.getWorld().equals(loc2.getWorld())) return false;

        return loc1.getBlockX() == loc2.getBlockX()
                && loc1.getBlockY() == loc2.getBlockY()
                && loc1.getBlockZ() == loc2.getBlockZ();
    }

    public static boolean isBetween(Location loc, Location corner1, Location corner2) {
        if (loc == null || corner1 == null || corner2 == null) return false;
        if (!loc.getWorld().equals(corner1.getWorld()) || !loc.getWorld().equals(corner2.getWorld())) return false;

        double x = loc.getX(), y = loc.getY(), z = loc.getZ();
        double x1 = Math.min(corner1.getX(), corner2.getX());
        double x2 = Math.max(corner1.getX(), corner2.getX());
        double y1 = Math.min(corner1.getY(), corner2.getY());
        double y2 = Math.max(corner1.getY(), corner2.getY());
        double z1 = Math.min(corner1.getZ(), corner2.getZ());
        double z2 = Math.max(corner1.getZ(), corner2.getZ());

        return x >= x1 && x <= x2 && y >= y1 && y <= y2 && z >= z1 && z <= z2;
    }

    public static boolean safeTeleport(Player player, Location location) {
        if (player == null || location == null || location.getWorld() == null) return false;

        try {
            player.teleport(location);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static double getDistance(Location loc1, Location loc2) {
        if (loc1 == null || loc2 == null) return -1;
        if (!loc1.getWorld().equals(loc2.getWorld())) return -1;

        return loc1.distance(loc2);
    }
}
