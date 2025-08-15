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

    private static Plugin plugin;

    private static final ConcurrentHashMap<Class<? extends Event>, CopyOnWriteArrayList<EventHandler<?>>> handlers = new ConcurrentHashMap<>();
    private static final Set<Class<? extends Event>> registeredEvents = ConcurrentHashMap.newKeySet();
    private static final CopyOnWriteArrayList<MultiEventHandler> multiHandlers = new CopyOnWriteArrayList<>();
    private static final ConcurrentHashMap<Class<? extends Event>, CopyOnWriteArrayList<MultiEventHandler>> multiEventMap = new ConcurrentHashMap<>();
    private static final Listener EMPTY_LISTENER = new Listener() {};

    public static void init(Plugin p) {
        if (plugin != null) return;
        if (p == null) throw new IllegalArgumentException("[FrostAPI] AsyncEventDispatcher -> Plugin não pode ser nulo!");
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
        if (plugin == null)
            throw new IllegalStateException("[FrostAPI] AsyncEventDispatcher -> Registre FrostAPI.enable(Plugin plugin) na main!");

        Class<T> eventClass = handler.getEventType();
        if (eventClass == null)
            throw new IllegalArgumentException("[FrostAPI] EventHandler.getEventType() retornou null.");

        handlers.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>()).add(handler);

        if (registeredEvents.add(eventClass)) {
            validateEventClass(eventClass);

            Bukkit.getPluginManager().registerEvent(
                    eventClass,
                    EMPTY_LISTENER,
                    EventPriority.NORMAL,
                    (listener, event) -> dispatch(event),
                    plugin
            );
        }
    }

    public static void register(MultiEventHandler multiHandler) {
        if (plugin == null)
            throw new IllegalStateException("[FrostAPI] AsyncEventDispatcher -> Registre FrostAPI.enable(Plugin plugin) na main!");

        multiHandlers.add(multiHandler);

        for (Class<? extends Event> eventClass : multiHandler.getEventTypes()) {
            multiEventMap.computeIfAbsent(eventClass, k -> new CopyOnWriteArrayList<>()).add(multiHandler);

            if (registeredEvents.add(eventClass)) {
                validateEventClass(eventClass);

                Bukkit.getPluginManager().registerEvent(
                        eventClass,
                        EMPTY_LISTENER,
                        EventPriority.NORMAL,
                        (listener, event) -> dispatch(event),
                        plugin
                );
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void dispatch(Event event) {
        for (CopyOnWriteArrayList<EventHandler<?>> list : handlers.values()) {
            for (EventHandler<?> handler : list) {
                if (handler.getEventType().isInstance(event)) {
                    try {
                        ((EventHandler<Event>) handler).handle(event.getClass().cast(event));
                    } catch (ClassCastException e) {
                        Bukkit.getLogger().warning("[FrostAPI] Erro ao despachar evento: " + e.getMessage());
                    }
                }
            }
        }

        for (CopyOnWriteArrayList<MultiEventHandler> list : multiEventMap.values()) {
            for (MultiEventHandler handler : list) {
                for (Class<? extends Event> type : handler.getEventTypes()) {
                    if (type.isInstance(event)) handler.handle(event);
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
