package com.github.ofrostdev.api.manager;

import org.bukkit.plugin.Plugin;

public class FrostManager {

    private static Plugin plugin;

    public static void init(Plugin plugin) {
        if (FrostManager.plugin != null) return;
        FrostManager.plugin = plugin;
    }
}
