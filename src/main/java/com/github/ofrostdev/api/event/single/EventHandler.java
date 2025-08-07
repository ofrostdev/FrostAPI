package com.github.ofrostdev.api.event.single;

import org.bukkit.event.Event;

public interface EventHandler<T extends Event> {
    void handle(T event);

    Class<T> getEventType();
}
