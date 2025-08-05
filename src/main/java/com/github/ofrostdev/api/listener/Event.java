package com.github.ofrostdev.api.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public final class Event {

    private static Plugin plugin;
    private static final Map<UUID, Consumer<org.bukkit.event.Event>> playerCallbacks = new ConcurrentHashMap<>();
    private static final Map<Class<? extends org.bukkit.event.Event>, Consumer<? extends org.bukkit.event.Event>> globalCallbacks = new ConcurrentHashMap<>();

    private Event() {}

    public static void init(Plugin plugin) {
        if (Event.plugin != null) return;
        Event.plugin = plugin;
    }

    @SuppressWarnings("unchecked")
    public static <E extends org.bukkit.event.Event> void listen(Class<E> eventClass, Consumer<E> callback) {
        if (plugin == null) throw new IllegalStateException("[FrostAPI] Event -> Registre com.github.ofrostdev.api.FrostAPI.enable(Plugin plugin) na main!");

        globalCallbacks.put(eventClass, callback);

        Bukkit.getPluginManager().registerEvent(
                eventClass,
                new Listener() {},
                org.bukkit.event.EventPriority.NORMAL,
                (listener, event) -> {
                    if (eventClass.isInstance(event)) {
                        ((Consumer<E>) globalCallbacks.get(eventClass)).accept((E) event);
                    }
                },
                plugin
        );
    }
    @SuppressWarnings("unchecked")
    public static <E extends org.bukkit.event.Event> void listen(Class<E> eventClass, Player player, Consumer<E> callback) {
        if (plugin == null) throw new IllegalStateException("[FrostAPI] Event -> Registre com.github.ofrostdev.api.FrostAPI.enable(Plugin plugin) na main!");

        UUID uuid = player.getUniqueId();
        playerCallbacks.put(uuid, (Consumer<org.bukkit.event.Event>) callback);

        Bukkit.getPluginManager().registerEvent(
                eventClass,
                new Listener() {},
                org.bukkit.event.EventPriority.NORMAL,
                (listener, event) -> {
                    if (eventClass.isInstance(event)) {
                        Consumer<org.bukkit.event.Event> action = playerCallbacks.get(uuid);
                        if (action != null) {
                            action.accept(event);
                        }
                    }
                },
                plugin
        );
    }

    public static void cancel(Player player) {
        playerCallbacks.remove(player.getUniqueId());
    }

    public static void cancel(Class<? extends org.bukkit.event.Event> eventClass) {
        globalCallbacks.remove(eventClass);
    }
}
