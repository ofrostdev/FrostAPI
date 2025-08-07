package com.github.ofrostdev.api.event;

import com.github.ofrostdev.api.event.multi.MultiEventHandler;
import com.github.ofrostdev.api.event.single.EventHandler;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.event.EventPriority;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.*;

public class EventDispatcher implements Listener {

    private static final Map<Class<? extends Event>, List<EventHandler<?>>> handlers = new HashMap<>();
    private static final Set<Class<? extends Event>> registeredEvents = new HashSet<>();

    private static final List<MultiEventHandler> multiHandlers = new ArrayList<>();

    private static Plugin plugin;

    public static void init(Plugin plugin) {
        if (EventDispatcher.plugin != null) return;
        EventDispatcher.plugin = plugin;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Event> void register(EventHandler<T> handler) {
        if (plugin == null) {
            throw new IllegalArgumentException("[FrostAPI] EventDispatcher -> Registre com.github.ofrostdev.api.FrostAPI.enable(Plugin plugin) na main!");
        }

        Class<T> eventClass = handler.getEventType();
        handlers.computeIfAbsent(eventClass, k -> new ArrayList<>()).add(handler);

        if (!registeredEvents.contains(eventClass)) {
            registeredEvents.add(eventClass);
            validateEventClass(eventClass);

            Bukkit.getPluginManager().registerEvent(
                    eventClass,
                    new Listener() {},
                    EventPriority.NORMAL,
                    (listener, event) -> {
                        if (eventClass.isInstance(event)) {
                            dispatch(event);
                            // Chamar MultiEventHandlers também
                            for (MultiEventHandler multiHandler : multiHandlers) {
                                if (multiHandler.getEventTypes().contains(eventClass)) {
                                    multiHandler.handle(event);
                                }
                            }
                        }
                    },
                    plugin
            );
        }
    }

    public static void register(MultiEventHandler multiHandler) {
        if (plugin == null) {
            throw new IllegalArgumentException("[FrostAPI] EventDispatcher -> Registre com.github.ofrostdev.api.FrostAPI.enable(Plugin plugin) na main!");
        }

        multiHandlers.add(multiHandler);

        for (Class<? extends Event> eventClass : multiHandler.getEventTypes()) {
            validateEventClass(eventClass);

            if (!registeredEvents.contains(eventClass)) {
                registeredEvents.add(eventClass);

                Bukkit.getPluginManager().registerEvent(
                        eventClass,
                        new Listener() {},
                        EventPriority.NORMAL,
                        (listener, event) -> {
                            if (eventClass.isInstance(event)) {
                                dispatch(event);
                                for (MultiEventHandler handler : multiHandlers) {
                                    if (handler.getEventTypes().contains(eventClass)) {
                                        handler.handle(event);
                                    }
                                }
                            }
                        },
                        plugin
                );
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Event> void dispatch(T event) {
        List<EventHandler<?>> eventHandlers = handlers.get(event.getClass());
        if (eventHandlers == null) return;

        for (EventHandler<?> handler : eventHandlers) {
            try {
                ((EventHandler<T>) handler).handle(event);
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }
    }

    private static void validateEventClass(Class<? extends Event> eventClass) {
        try {
            Method method = eventClass.getMethod("getHandlerList");
            if (!java.lang.reflect.Modifier.isStatic(method.getModifiers())) {
                throw new IllegalArgumentException("getHandlerList não é estático em " + eventClass.getName());
            }
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Evento " + eventClass.getName() + " não possui getHandlerList()");
        }
    }
}
