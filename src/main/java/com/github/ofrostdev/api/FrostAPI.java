package com.github.ofrostdev.api;

import com.github.ofrostdev.api.listener.*;
import com.github.ofrostdev.api.manager.FrostManager;
import com.github.ofrostdev.api.manager.RegisterFactory;
import com.github.ofrostdev.api.manager.rLoader;
import com.github.ofrostdev.api.utils.Config;
import org.bukkit.command.Command;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class FrostAPI {
    public static void enable(JavaPlugin plugin) {
        FrostManager.init(plugin);
        Event.init(plugin);
        Config.init(plugin);
        RegisterFactory.init(plugin);
    }

}
