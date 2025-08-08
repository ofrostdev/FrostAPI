package com.github.ofrostdev.api.event;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class Event {

    private static Plugin plugin;

    private static final Map<Class<? extends org.bukkit.event.Event>, CopyOnWriteArrayList<Consumer<? extends org.bukkit.event.Event>>> globalCallbacks = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<Class<? extends org.bukkit.event.Event>, CopyOnWriteArrayList<Consumer<? extends org.bukkit.event.Event>>>> playerCallbacks = new ConcurrentHashMap<>();
    private static final Map<Class<? extends org.bukkit.event.Event>, Listener> registeredListeners = new ConcurrentHashMap<>();

    private Event() {}

    public static void init(Plugin plugin) {
        if (Event.plugin != null) return;
        Event.plugin = plugin;
    }

    public static <E extends org.bukkit.event.Event> void handle(Class<E> eventClass, Consumer<E> callback) {
        handle(eventClass, callback, false);
    }

    public static <E extends org.bukkit.event.Event> void handle(Class<E> eventClass, Consumer<E> callback, boolean keepListener) {
        if (plugin == null) throw new IllegalStateException("[FrostAPI] Event -> Registre FrostAPI.enable(Plugin plugin) na main!");

        globalCallbacks.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>()).add(callback);

        registeredListeners.computeIfAbsent(eventClass, ec -> {
            Listener listener = new Listener() {};
            Bukkit.getPluginManager().registerEvent(
                    eventClass,
                    listener,
                    org.bukkit.event.EventPriority.NORMAL,
                    (l, event) -> {
                        if (eventClass.isInstance(event)) {
                            List<Consumer<? extends org.bukkit.event.Event>> callbacks = globalCallbacks.get(eventClass);
                            if (callbacks != null) {
                                for (Consumer consumer : callbacks) {
                                    try {
                                        consumer.accept(event);
                                    } catch (Exception ex) {
                                        Bukkit.getLogger().warning("[FrostAPI] Exceção em callback global de evento: " + ex.getMessage());
                                        ex.printStackTrace();
                                    }
                                }
                            }
                        }
                    },
                    plugin
            );
            return listener;
        });

        if (!keepListener) {
            CopyOnWriteArrayList<Consumer<? extends org.bukkit.event.Event>> callbacks = globalCallbacks.get(eventClass);
            if (callbacks != null) {
                callbacks.remove(callback);
                if (callbacks.isEmpty()) {
                    globalCallbacks.remove(eventClass);
                    registeredListeners.remove(eventClass);
                }
            }
        }
    }

    public static <E extends org.bukkit.event.Event> void handle(Class<E> eventClass, Player player, Consumer<E> callback) {
        handle(eventClass, player, callback, false);
    }

    public static <E extends org.bukkit.event.Event> void handle(Class<E> eventClass, Player player, Consumer<E> callback, boolean keepListener) {
        if (plugin == null) throw new IllegalStateException("[FrostAPI] Event -> Registre FrostAPI.enable(Plugin plugin) na main!");

        UUID uuid = player.getUniqueId();

        playerCallbacks
                .computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>())
                .add(callback);

        registeredListeners.computeIfAbsent(eventClass, ec -> {
            Listener listener = new Listener() {};
            Bukkit.getPluginManager().registerEvent(
                    eventClass,
                    listener,
                    org.bukkit.event.EventPriority.NORMAL,
                    (l, event) -> {
                        if (eventClass.isInstance(event)) {
                            List<Consumer<? extends org.bukkit.event.Event>> global = globalCallbacks.get(eventClass);
                            if (global != null) {
                                for (Consumer consumer : global) {
                                    try {
                                        consumer.accept(event);
                                    } catch (Exception ex) {
                                        Bukkit.getLogger().warning("[FrostAPI] Exceção em callback global de evento: " + ex.getMessage());
                                        ex.printStackTrace();
                                    }
                                }
                            }

                            for (Map.Entry<UUID, Map<Class<? extends org.bukkit.event.Event>, CopyOnWriteArrayList<Consumer<? extends org.bukkit.event.Event>>>> entry : playerCallbacks.entrySet()) {
                                Map<Class<? extends org.bukkit.event.Event>, CopyOnWriteArrayList<Consumer<? extends org.bukkit.event.Event>>> map = entry.getValue();
                                if (map != null) {
                                    List<Consumer<? extends org.bukkit.event.Event>> list = map.get(eventClass);
                                    if (list != null) {
                                        for (Consumer consumer : list) {
                                            try {
                                                consumer.accept(event);
                                            } catch (Exception ex) {
                                                Bukkit.getLogger().warning("[FrostAPI] Exceção em callback por jogador: " + ex.getMessage());
                                                ex.printStackTrace();
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    plugin
            );
            return listener;
        });

        if (!keepListener) {
            Map<Class<? extends org.bukkit.event.Event>, CopyOnWriteArrayList<Consumer<? extends org.bukkit.event.Event>>> map = playerCallbacks.get(uuid);
            if (map != null) {
                List<Consumer<? extends org.bukkit.event.Event>> callbacks = map.get(eventClass);
                if (callbacks != null) {
                    callbacks.remove(callback);
                    if (callbacks.isEmpty()) {
                        map.remove(eventClass);
                    }
                }
                if (map.isEmpty()) {
                    playerCallbacks.remove(uuid);
                }
            }
        }
    }

    public static void cancel(Player player) {
        playerCallbacks.remove(player.getUniqueId());
    }

    public static void cancel(Class<? extends org.bukkit.event.Event> eventClass) {
        globalCallbacks.remove(eventClass);
        registeredListeners.remove(eventClass);
    }
}
