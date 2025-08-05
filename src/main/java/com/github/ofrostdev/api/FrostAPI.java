package com.github.ofrostdev.api;

import com.github.ofrostdev.api.listener.*;
import com.github.ofrostdev.api.manager.FrostManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class FrostAPI {
    public static void enable(JavaPlugin plugin) {
        FrostManager.init(plugin);
        Event.init(plugin);
    }

}
