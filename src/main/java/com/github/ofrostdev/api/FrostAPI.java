package com.github.ofrostdev.api;

import com.github.ofrostdev.api.event.*;
import com.github.ofrostdev.api.event.dispatch.AsyncEventDispatcher;
import com.github.ofrostdev.api.event.dispatch.SyncEventDispatcher;
import com.github.ofrostdev.api.manager.FrostManager;
import com.github.ofrostdev.api.manager.RegisterFactory;
import com.github.ofrostdev.api.utils.Config;
import org.bukkit.plugin.java.JavaPlugin;

public final class FrostAPI {
    public static void enable(JavaPlugin plugin) {
        FrostManager.init(plugin);
        Event.init(plugin);
        AsyncEventDispatcher.init(plugin);
        SyncEventDispatcher.init(plugin);
        Config.init(plugin);
        RegisterFactory.init(plugin);
    }

}
