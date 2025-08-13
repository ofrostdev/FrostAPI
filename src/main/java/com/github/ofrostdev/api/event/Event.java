package com.github.ofrostdev.api.event;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public final class Event {

    private static Plugin plugin;

    private static final Map<Class<? extends org.bukkit.event.Event>, CopyOnWriteArrayList<CallbackWrapper<? extends org.bukkit.event.Event>>> globalCallbacks = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<Class<? extends org.bukkit.event.Event>, CopyOnWriteArrayList<CallbackWrapper<? extends org.bukkit.event.Event>>>> playerCallbacks = new ConcurrentHashMap<>();
    private static final Map<Class<? extends org.bukkit.event.Event>, Listener> registeredListeners = new ConcurrentHashMap<>();

    private Event() {}

    private static class CallbackWrapper<E extends org.bukkit.event.Event> {
        final Consumer<E> consumer;
        final boolean keepListener;

        CallbackWrapper(Consumer<E> consumer, boolean keepListener) {
            this.consumer = consumer;
            this.keepListener = keepListener;
        }
    }

    public static void init(Plugin plugin) {
        if (Event.plugin != null) return;
        Event.plugin = plugin;
    }

    public static <E extends org.bukkit.event.Event> void handle(Class<E> eventClass, Consumer<E> callback) {
        handle(eventClass, callback, false, EventPriority.NORMAL);
    }

    public static <E extends org.bukkit.event.Event> void handle(Class<E> eventClass, Consumer<E> callback, boolean keepListener) {
        handle(eventClass, callback, keepListener, EventPriority.NORMAL);
    }

    public static <E extends org.bukkit.event.Event> void handle(Class<E> eventClass, Consumer<E> callback, EventPriority priority) {
        handle(eventClass, callback, false, priority);
    }

    public static <E extends org.bukkit.event.Event> void handle(Class<E> eventClass, Consumer<E> callback, boolean keepListener, EventPriority priority) {
        if (plugin == null) throw new IllegalStateException("[FrostAPI] Event -> Registre FrostAPI.enable(Plugin plugin) na main!");

        globalCallbacks.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>()).add(new CallbackWrapper<>(callback, keepListener));

        registeredListeners.computeIfAbsent(eventClass, ec -> {
            Listener listener = new Listener() {};
            Bukkit.getPluginManager().registerEvent(
                    eventClass,
                    listener,
                    priority,
                    (l, event) -> {
                        if (eventClass.isInstance(event)) {
                            CopyOnWriteArrayList<CallbackWrapper<? extends org.bukkit.event.Event>> callbacks = globalCallbacks.get(eventClass);
                            if (callbacks != null && !callbacks.isEmpty()) {
                                CallbackWrapper<? extends org.bukkit.event.Event> wrapper = callbacks.get(0);
                                try {
                                    @SuppressWarnings("unchecked")
                                    CallbackWrapper<E> castedWrapper = (CallbackWrapper<E>) wrapper;
                                    castedWrapper.consumer.accept(eventClass.cast(event));
                                    if (!castedWrapper.keepListener) {
                                        callbacks.remove(wrapper);
                                    }
                                } catch (Exception ex) {
                                    Bukkit.getLogger().warning("[FrostAPI] Exceção em callback global de evento: " + ex.getMessage());
                                    ex.printStackTrace();
                                }
                                if (callbacks.isEmpty()) {
                                    globalCallbacks.remove(eventClass);
                                    registeredListeners.remove(eventClass);
                                }
                            }
                        }
                    },
                    plugin
            );
            return listener;
        });
    }

    public static <E extends org.bukkit.event.Event> void handle(Class<E> eventClass, Player player, Consumer<E> callback) {
        handle(eventClass, player, callback, false, EventPriority.NORMAL);
    }

    public static <E extends org.bukkit.event.Event> void handle(Class<E> eventClass, Player player, Consumer<E> callback, boolean keepListener) {
        handle(eventClass, player, callback, keepListener, EventPriority.NORMAL);
    }

    public static <E extends org.bukkit.event.Event> void handle(Class<E> eventClass, Player player, Consumer<E> callback, EventPriority priority) {
        handle(eventClass, player, callback, false, priority);
    }

    public static <E extends org.bukkit.event.Event> void handle(Class<E> eventClass, Player player, Consumer<E> callback, boolean keepListener, EventPriority priority) {
        if (plugin == null) throw new IllegalStateException("[FrostAPI] Event -> Registre FrostAPI.enable(Plugin plugin) na main!");

        UUID uuid = player.getUniqueId();

        playerCallbacks
                .computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
                .computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>())
                .add(new CallbackWrapper<>(callback, keepListener));

        registeredListeners.computeIfAbsent(eventClass, ec -> {
            Listener listener = new Listener() {};
            Bukkit.getPluginManager().registerEvent(
                    eventClass,
                    listener,
                    priority,
                    (l, event) -> {
                        if (eventClass.isInstance(event)) {
                            CopyOnWriteArrayList<CallbackWrapper<? extends org.bukkit.event.Event>> global = globalCallbacks.get(eventClass);
                            if (global != null && !global.isEmpty()) {
                                CallbackWrapper<? extends org.bukkit.event.Event> wrapper = global.get(0);
                                try {
                                    @SuppressWarnings("unchecked")
                                    CallbackWrapper<E> castedWrapper = (CallbackWrapper<E>) wrapper;
                                    castedWrapper.consumer.accept(eventClass.cast(event));
                                    if (!castedWrapper.keepListener) {
                                        global.remove(wrapper);
                                    }
                                } catch (Exception ex) {
                                    Bukkit.getLogger().warning("[FrostAPI] Exceção em callback global de evento: " + ex.getMessage());
                                    ex.printStackTrace();
                                }
                                if (global.isEmpty()) {
                                    globalCallbacks.remove(eventClass);
                                    registeredListeners.remove(eventClass);
                                }
                            }

                            for (Map.Entry<UUID, Map<Class<? extends org.bukkit.event.Event>, CopyOnWriteArrayList<CallbackWrapper<? extends org.bukkit.event.Event>>>> entry : playerCallbacks.entrySet()) {
                                Map<Class<? extends org.bukkit.event.Event>, CopyOnWriteArrayList<CallbackWrapper<? extends org.bukkit.event.Event>>> map = entry.getValue();
                                if (map != null) {
                                    CopyOnWriteArrayList<CallbackWrapper<? extends org.bukkit.event.Event>> list = map.get(eventClass);
                                    if (list != null && !list.isEmpty()) {
                                        CallbackWrapper<? extends org.bukkit.event.Event> wrapper = list.get(0);
                                        try {
                                            @SuppressWarnings("unchecked")
                                            CallbackWrapper<E> castedWrapper = (CallbackWrapper<E>) wrapper;
                                            castedWrapper.consumer.accept(eventClass.cast(event));
                                            if (!castedWrapper.keepListener) {
                                                list.remove(wrapper);
                                            }
                                        } catch (Exception ex) {
                                            Bukkit.getLogger().warning("[FrostAPI] Exceção em callback por jogador: " + ex.getMessage());
                                            ex.printStackTrace();
                                        }
                                        if (list.isEmpty()) {
                                            map.remove(eventClass);
                                        }
                                    }
                                    if (map.isEmpty()) {
                                        playerCallbacks.remove(entry.getKey());
                                    }
                                }
                            }
                        }
                    },
                    plugin
            );
            return listener;
        });
    }

    public static void cancel(Player player) {
        playerCallbacks.remove(player.getUniqueId());
    }

    public static void cancel(Class<? extends org.bukkit.event.Event> eventClass) {
        globalCallbacks.remove(eventClass);
        registeredListeners.remove(eventClass);
    }

    public static <E extends org.bukkit.event.Event> void cancel(Player player, Class<E> eventClass, E event) {
        UUID uuid = player.getUniqueId();
        Map<Class<? extends org.bukkit.event.Event>, CopyOnWriteArrayList<CallbackWrapper<? extends org.bukkit.event.Event>>> map = playerCallbacks.get(uuid);
        if (map == null) return;
        CopyOnWriteArrayList<CallbackWrapper<? extends org.bukkit.event.Event>> list = map.get(eventClass);
        if (list == null || list.isEmpty()) return;

        CallbackWrapper<? extends org.bukkit.event.Event> wrapper = list.remove(list.size() - 1);
        try {
            @SuppressWarnings("unchecked")
            CallbackWrapper<E> castedWrapper = (CallbackWrapper<E>) wrapper;
            castedWrapper.consumer.accept(eventClass.cast(event));
        } catch (Exception ex) {
            Bukkit.getLogger().warning("[FrostAPI] Exceção ao cancelar último callback: " + ex.getMessage());
            ex.printStackTrace();
        }

        if (list.isEmpty()) map.remove(eventClass);
        if (map.isEmpty()) playerCallbacks.remove(uuid);
    }

}
