package com.github.ofrostdev.api.event.multi;

import org.bukkit.event.Event;
import java.util.List;

public interface MultiEventHandler {
    List<Class<? extends Event>> getEventTypes();
    void handle(Event event);
}
