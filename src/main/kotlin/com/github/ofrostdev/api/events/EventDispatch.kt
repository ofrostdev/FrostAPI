package com.github.ofrostdev.api.events

import com.github.ofrostdev.api.events.handler.EventHandler
import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import java.lang.reflect.Method
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class EventDispatch private constructor(private val plugin: Plugin) : Listener {

    companion object {
        private val instances = ConcurrentHashMap<Plugin, EventDispatch>()

        fun enable(plugin: Plugin): EventDispatch {
            return instances.computeIfAbsent(plugin) { EventDispatch(it) }
        }

        fun get(plugin: Plugin): EventDispatch {
            return instances[plugin] ?: throw IllegalStateException("EventDispatch for ${plugin.name} not enabled")
        }
    }

    private val handlers: ConcurrentHashMap<Class<out Event>, CopyOnWriteArrayList<EventHandler>> = ConcurrentHashMap()
    private val registeredEvents: MutableSet<Class<out Event>> = ConcurrentHashMap.newKeySet()
    private val EMPTY_LISTENER: Listener = object : Listener {}

    fun register(handler: EventHandler) {
        for (eventClass in handler.eventTypes) {
            handlers.computeIfAbsent(eventClass) { CopyOnWriteArrayList() }.add(handler)

            if (registeredEvents.add(eventClass)) {
                validateEventClass(eventClass)
                Bukkit.getPluginManager().registerEvent(
                    eventClass,
                    EMPTY_LISTENER,
                    EventPriority.NORMAL,
                    { _, event -> dispatch(event) },
                    plugin
                )
            }
        }
    }

    fun register(vararg handlers: EventHandler) {
        handlers.forEach { register(it) }
    }

    private fun dispatch(event: Event) {
        handlers[event::class.java]?.forEach { handler ->
            try {
                handler.handle(event)
            } catch (e: Exception) {
                Bukkit.getLogger().warning("[FrostAPI] error dispatching event: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun validateEventClass(eventClass: Class<out Event>) {
        try {
            val method: Method = eventClass.getMethod("getHandlerList")
            if (!java.lang.reflect.Modifier.isStatic(method.modifiers)) {
                throw IllegalArgumentException("[FrostAPI] getHandlerList is not static in: ${eventClass.name}")
            }
        } catch (e: NoSuchMethodException) {
            throw IllegalArgumentException("[FrostAPI] event ${eventClass.name} does not have getHandlerList()")
        }
    }
}
