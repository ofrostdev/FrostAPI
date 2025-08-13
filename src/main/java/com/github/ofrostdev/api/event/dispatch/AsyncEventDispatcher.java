package com.github.ofrostdev.api.event.dispatch;

import com.github.ofrostdev.api.event.multi.MultiEventHandler;
import com.github.ofrostdev.api.event.single.EventHandler;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class AsyncEventDispatcher implements Listener {

    private static final ConcurrentHashMap<Class<? extends Event>, CopyOnWriteArrayList<EventHandler<?>>> handlers = new ConcurrentHashMap<>();
    private static final Set<Class<? extends Event>> registeredEvents = ConcurrentHashMap.newKeySet();
    private static final CopyOnWriteArrayList<MultiEventHandler> multiHandlers = new CopyOnWriteArrayList<>();
    private static final ConcurrentHashMap<Class<? extends Event>, CopyOnWriteArrayList<MultiEventHandler>> multiEventMap = new ConcurrentHashMap<>();

    private static final Listener EMPTY_LISTENER = new Listener() {};
    private static Plugin plugin;

    public static void init(Plugin plugin) {
        if (AsyncEventDispatcher.plugin != null) return;
        AsyncEventDispatcher.plugin = plugin;
    }

    public static void registerHandlers(EventHandler<? extends Event>... eventHandlers) {
        for (EventHandler<? extends Event> handler : eventHandlers) {
            register(handler);
        }
    }

    public static void registerMultiHandlers(MultiEventHandler... multiEventHandlers) {
        for (MultiEventHandler handler : multiEventHandlers) {
            register(handler);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Event> void register(EventHandler<T> handler) {
        if (plugin == null) {
            throw new IllegalStateException("[FrostAPI] AsyncEventDispatcher -> Registre FrostAPI.enable(Plugin plugin) na main!");
        }

        Class<T> eventClass = handler.getEventType();
        if (eventClass == null) {
            throw new IllegalArgumentException("EventHandler.getEventType() retornou null.");
        }

        handlers.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>()).add(handler);

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
                                for (MultiEventHandler multiHandler : multiList) {
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
            throw new IllegalStateException("[FrostAPI] AsyncEventDispatcher -> Registre FrostAPI.enable(Plugin plugin) na main!");
        }

        multiHandlers.add(multiHandler);

        for (Class<? extends Event> eventClass : multiHandler.getEventTypes()) {
            multiEventMap.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>()).add(multiHandler);

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
                                    for (MultiEventHandler handler1 : multiList) {
                                        handler1.handle(event);
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
                Bukkit.getLogger().warning("[FrostAPI] Erro ao despachar evento: " + e.getMessage());
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
