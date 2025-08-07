package com.github.ofrostdev.api;

import com.github.ofrostdev.api.event.*;
import com.github.ofrostdev.api.event.EventDispatcher;
import com.github.ofrostdev.api.manager.FrostManager;
import com.github.ofrostdev.api.manager.RegisterFactory;
import com.github.ofrostdev.api.utils.Config;
import org.bukkit.plugin.java.JavaPlugin;

public final class FrostAPI {
    public static void enable(JavaPlugin plugin) {
        FrostManager.init(plugin);
        Event.init(plugin);
        EventDispatcher.init(plugin);
        Config.init(plugin);
        RegisterFactory.init(plugin);
    }

}
