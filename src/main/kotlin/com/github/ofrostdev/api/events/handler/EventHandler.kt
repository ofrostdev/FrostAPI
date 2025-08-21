package com.github.ofrostdev.api.events.handler

import org.bukkit.event.Event
import kotlin.reflect.KClass

abstract class EventHandler(vararg types: KClass<out Event>) {

    val eventTypes: Set<Class<out Event>> = if (types.isNotEmpty()) {
        types.map { it.java }.toSet()
    } else {
        throw IllegalArgumentException("[FrostAPI] EventHandler has no more than 1 event!")
    }

    abstract fun handle(event: Event)
}
