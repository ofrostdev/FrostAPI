package com.github.ofrostdev.api.event.multi;

import org.bukkit.event.Event;
import java.util.Set;

public interface MultiEventHandler {
    void handle(Event event);
    Set<Class<? extends Event>> getEventTypes();
}
