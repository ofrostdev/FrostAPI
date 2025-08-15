package com.github.ofrostdev.api.manager;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class FrostManager {

    private static Plugin plugin;

    public static void init(Plugin p) {
        if (plugin != null) return;
        if (p == null) throw new IllegalArgumentException("[FrostAPI] FrostManager -> Plugin não pode ser nulo!");
        plugin = p;
    }

    public static boolean hasPlugin(Plugin p){
        if (p == null) return false;
        Plugin found = Bukkit.getPluginManager().getPlugin(p.getName());
        return found != null && found.isEnabled();
    }

    public static boolean hasPlugin(String name){
        if (name == null || name.isEmpty()) return false;
        Plugin found = Bukkit.getPluginManager().getPlugin(name);
        return found != null && found.isEnabled();
    }

    public static boolean hasPluginClass(String clazz){
        try {
            Class.forName(clazz);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    public static Plugin getPluginInstance() {
        return plugin;
    }
}
