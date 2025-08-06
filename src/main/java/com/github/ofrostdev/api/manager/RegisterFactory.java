package com.github.ofrostdev.api.manager;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;

import static org.bukkit.Bukkit.getName;

public class RegisterFactory {

    private static Plugin plugin;

    public static void init(Plugin plugin){
        if (RegisterFactory.plugin != null) return;
        RegisterFactory.plugin = plugin;
    }

    public static void register(Listener... listeners) {
        if (plugin == null) {
            throw new IllegalArgumentException("[FrostAPI] RegisterFactory -> Registre com.github.ofrostdev.api.FrostAPI.enable(Plugin plugin) na main!");
        }
        for (Listener listener : listeners) {
            Bukkit.getServer().getPluginManager().registerEvents(listener, plugin);
        }
    }

    public static void register(Command... commands) {
        if (plugin == null) {
            throw new IllegalArgumentException("[FrostAPI] RegisterFactory -> Registre com.github.ofrostdev.api.FrostAPI.enable(Plugin plugin) na main!");
        }
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
