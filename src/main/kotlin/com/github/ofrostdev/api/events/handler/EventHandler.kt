package com.github.ofrostdev.api.events.handler

import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import kotlin.reflect.KClass

abstract class EventHandler {

    val eventPriorities: MutableMap<KClass<out Event>, EventPriority> = mutableMapOf()

    protected fun registerEvents(vararg events: Pair<KClass<out Event>, EventPriority>) {
        for ((kClass, priority) in events) {
            eventPriorities[kClass] = priority
        }
    }

    val eventTypes: Set<Class<out Event>>
        get() = eventPriorities.keys.map { it.java }.toSet()

    abstract fun handle(event: Event)
}
