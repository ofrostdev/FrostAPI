package com.github.ofrostdev.api.manager;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

public class FrostManager {

    private static Plugin plugin;

    public static void init(Plugin plugin) {
        if (FrostManager.plugin != null) return;
        FrostManager.plugin = plugin;
    }

    public static boolean hasPlugin(Plugin plugin){
        if (plugin == null) return false;
        Plugin found = Bukkit.getPluginManager().getPlugin(plugin.getName());
        return found != null && found.isEnabled();
    }

    public static boolean hasPlugin(String name){
        if (name == null || name.isEmpty()) return false;
        Plugin plugin = Bukkit.getPluginManager().getPlugin(name);
        return plugin != null && plugin.isEnabled();
    }

    public static boolean hasPluginClass(String clazz){
        try {
            Class.forName("br.com.ystoreplugins.product.ypesca.event.PescaFishEvent");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

}
