package com.github.ofrostdev.api.manager;

import com.github.ofrostdev.api.listener.Event;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;

import static org.bukkit.Bukkit.getName;

public class FrostManager {

    private static Plugin plugin;

    public FrostManager() {}

    public static void init(Plugin plugin) {
        if (FrostManager.plugin != null) return;
        FrostManager.plugin = plugin;
    }

    public static void registerEvents(Listener... listeners) {
        for (Listener listener : listeners) {
            Bukkit.getServer().getPluginManager().registerEvents(listener, plugin);
        }
    }

    public static void registerCommands(Command... commands) {
        for (Command command : commands) {
            commandMap(getName(), command);
        }
    }

    private static void commandMap(String prefix, Command command) {
        try {
            Field field = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            CommandMap commandMap = (CommandMap) field.get(Bukkit.getServer());

            if (commandMap != null) {
                commandMap.register(prefix, command);
            }
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
