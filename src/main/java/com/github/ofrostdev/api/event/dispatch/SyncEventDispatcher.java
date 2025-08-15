package com.github.ofrostdev.api.event.dispatch;

import com.github.ofrostdev.api.event.multi.MultiEventHandler;
import com.github.ofrostdev.api.event.single.EventHandler;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.*;

public class SyncEventDispatcher implements Listener {

    private static Plugin plugin;

    private static final Map<Class<? extends Event>, List<EventHandler<?>>> handlers = new HashMap<>();
    private static final Set<Class<? extends Event>> registeredEvents = new HashSet<>();
    private static final List<MultiEventHandler> multiHandlers = new ArrayList<>();
    private static final Map<Class<? extends Event>, List<MultiEventHandler>> multiEventMap = new HashMap<>();
    private static final Listener EMPTY_LISTENER = new Listener() {};

    public static void init(Plugin p) {
        if (plugin != null) return;
        if (p == null) throw new IllegalArgumentException("[FrostAPI] SyncEventDispatcher -> Plugin não pode ser nulo!");
        plugin = p;
    }

    public static void registerHandlers(EventHandler<? extends Event>... eventHandlers) {
        for (EventHandler<? extends Event> handler : eventHandlers) register(handler);
    }

    public static void registerMultiHandlers(MultiEventHandler... multiEventHandlers) {
        for (MultiEventHandler handler : multiEventHandlers) register(handler);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Event> void register(EventHandler<T> handler) {
        if (plugin == null) throw new IllegalStateException("[FrostAPI] SyncEventDispatcher -> Registre FrostAPI.enable(Plugin plugin) na main!");

        Class<T> eventClass = handler.getEventType();
        if (eventClass == null) throw new IllegalArgumentException("[FrostAPI] EventHandler.getEventType() retornou null.");

        handlers.computeIfAbsent(eventClass, k -> new ArrayList<>()).add(handler);

        if (registeredEvents.add(eventClass)) {
            validateEventClass(eventClass);

            Bukkit.getPluginManager().registerEvent(
                    eventClass,
                    EMPTY_LISTENER,
                    EventPriority.NORMAL,
                    (listener, event) -> {
                        if (eventClass.isInstance(event)) {
                            dispatch(event);
                            List<MultiEventHandler> multiList = multiEventMap.get(eventClass);
                            if (multiList != null) {
                                for (MultiEventHandler h : multiList) h.handle(event);
                            }
                        }
                    },
                    plugin
            );
        }
    }

    public static void register(MultiEventHandler multiHandler) {
        if (plugin == null) throw new IllegalStateException("[FrostAPI] SyncEventDispatcher -> Registre FrostAPI.enable(Plugin plugin) na main!");

        multiHandlers.add(multiHandler);

        for (Class<? extends Event> eventClass : multiHandler.getEventTypes()) {
            multiEventMap.computeIfAbsent(eventClass, k -> new ArrayList<>()).add(multiHandler);

            if (registeredEvents.add(eventClass)) {
                validateEventClass(eventClass);

                Bukkit.getPluginManager().registerEvent(
                        eventClass,
                        EMPTY_LISTENER,
                        EventPriority.NORMAL,
                        (listener, event) -> {
                            if (eventClass.isInstance(event)) {
                                dispatch(event);
                                List<MultiEventHandler> multiList = multiEventMap.get(eventClass);
                                if (multiList != null) {
                                    for (MultiEventHandler h : multiList) h.handle(event);
                                }
                            }
                        },
                        plugin
                );
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void dispatch(Event event) {
        for (Map.Entry<Class<? extends Event>, List<EventHandler<?>>> entry : handlers.entrySet()) {
            if (entry.getKey().isInstance(event)) {
                for (EventHandler<?> handler : entry.getValue()) {
                    try {
                        ((EventHandler<Event>) handler).handle(event);
                    } catch (ClassCastException e) {
                        Bukkit.getLogger().warning("[FrostAPI] Erro ao despachar evento: " + e.getMessage());
                    }
                }
            }
        }

        for (Map.Entry<Class<? extends Event>, List<MultiEventHandler>> entry : multiEventMap.entrySet()) {
            if (entry.getKey().isInstance(event)) {
                for (MultiEventHandler handler : entry.getValue()) {
                    handler.handle(event);
                }
            }
        }
    }

    private static void validateEventClass(Class<? extends Event> eventClass) {
        try {
            Method method = eventClass.getMethod("getHandlerList");
            if (!java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
                throw new IllegalArgumentException("[FrostAPI] getHandlerList não é estático em " + eventClass.getName());
            }
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("[FrostAPI] Evento " + eventClass.getName() + " não possui getHandlerList()");
        }
    }
}
