package com.github.ofrostdev.api.event.multi;

import org.bukkit.event.Event;
import java.util.List;

public interface MultiEventHandler {
    void handle(Event event);
    List<Class<? extends Event>> getEventTypes();
}
