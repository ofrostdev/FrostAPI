package com.github.ofrostdev.api.events

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

object EventCallback {

    private var plugin: Plugin? = null

    private val globalCallbacks: MutableMap<Class<out Event>, CopyOnWriteArrayList<CallbackWrapper<out Event>>> =
        ConcurrentHashMap()
    private val playerCallbacks: MutableMap<UUID, MutableMap<Class<out Event>, CopyOnWriteArrayList<CallbackWrapper<out Event>>>> =
        ConcurrentHashMap()

    private val registeredListeners: MutableMap<Class<out Event>, Listener> =
        ConcurrentHashMap()

    private data class CallbackWrapper<E : Event>(
        val consumer: (E) -> Unit,
        val keepListener: Boolean
    )

    fun enable(p: Plugin) {
        require(plugin == null)
        plugin = p
    }

    fun <E : Event> handle(
        eventClass: Class<E>,
        callback: (E) -> Unit,
        keepListener: Boolean = false,
        priority: EventPriority = EventPriority.NORMAL
    ) {
        val pl = plugin ?: throw IllegalStateException("[FrostAPI] EventCallback instance is null!")

        globalCallbacks.computeIfAbsent(eventClass) { CopyOnWriteArrayList() }
            .add(CallbackWrapper(callback, keepListener))

        registeredListeners.computeIfAbsent(eventClass) {
            val listener = object : Listener {}
            Bukkit.getPluginManager().registerEvent(
                eventClass,
                listener,
                priority,
                { _, event ->
                    if (eventClass.isInstance(event)) {
                        val callbacks = globalCallbacks[eventClass] ?: return@registerEvent
                        if (callbacks.isNotEmpty()) {
                            val wrapper = callbacks[0]
                            try {
                                @Suppress("UNCHECKED_CAST")
                                (wrapper as CallbackWrapper<E>).consumer(eventClass.cast(event))
                                if (!wrapper.keepListener) {
                                    callbacks.remove(wrapper)
                                }
                            } catch (ex: Exception) {
                                Bukkit.getLogger().warning("[FrostAPI] EventCallback exception in callback global: ${ex.message}")
                                ex.printStackTrace()
                            }
                            if (callbacks.isEmpty()) {
                                globalCallbacks.remove(eventClass)
                                registeredListeners.remove(eventClass)
                            }
                        }
                    }
                },
                pl
            )
            listener
        }
    }

    fun <E : Event> handle(
        eventClass: Class<E>,
        player: Player,
        callback: (E) -> Unit,
        keepListener: Boolean = false,
        priority: EventPriority = EventPriority.NORMAL
    ) {
        val pl = plugin ?: throw IllegalStateException("[FrostAPI] [FrostAPI] EventCallback instance is null!")

        val uuid = player.uniqueId
        playerCallbacks
            .computeIfAbsent(uuid) { ConcurrentHashMap() }
            .computeIfAbsent(eventClass) { CopyOnWriteArrayList() }
            .add(CallbackWrapper(callback, keepListener))

        registeredListeners.computeIfAbsent(eventClass) {
            val listener = object : Listener {}
            Bukkit.getPluginManager().registerEvent(
                eventClass,
                listener,
                priority,
                { _, event ->
                    if (eventClass.isInstance(event)) {
                        globalCallbacks[eventClass]?.let { global ->
                            if (global.isNotEmpty()) {
                                val globalCopy = ArrayList(global)
                                for (wrapper in globalCopy) {
                                    try {
                                        @Suppress("UNCHECKED_CAST")
                                        (wrapper as CallbackWrapper<E>).consumer(eventClass.cast(event))
                                        if (!wrapper.keepListener) global.remove(wrapper)
                                    } catch (ex: Exception) {
                                        Bukkit.getLogger().warning("[FrostAPI] EventCallback exception in global: ${ex.message}")
                                        ex.printStackTrace()
                                    }
                                }
                                if (global.isEmpty()) {
                                    globalCallbacks.remove(eventClass)
                                    registeredListeners.remove(eventClass)?.let { HandlerList.unregisterAll(it) }
                                }
                            }
                        }

                        val it1 = playerCallbacks.entries.iterator()
                        while (it1.hasNext()) {
                            val (uuidKey, map) = it1.next()
                            val list = map[eventClass] ?: continue
                            if (list.isEmpty()) continue

                            val copy = ArrayList(list)
                            for (wrapper in copy) {
                                try {
                                    @Suppress("UNCHECKED_CAST")
                                    (wrapper as CallbackWrapper<E>).consumer(eventClass.cast(event))
                                    if (!wrapper.keepListener) list.remove(wrapper)
                                } catch (ex: Exception) {
                                    Bukkit.getLogger().warning("[FrostAPI] EventCallback exception in player callback: ${ex.message}")
                                    ex.printStackTrace()
                                }
                            }

                            if (list.isEmpty()) map.remove(eventClass)
                            if (map.isEmpty()) it1.remove()
                        }
                    }

                },
                pl
            )
            listener
        }
    }

    fun cancel(player: Player) {
        val uuid = player.uniqueId
        playerCallbacks.remove(uuid)
    }

    fun cancel(eventClass: Class<out Event>) {
        globalCallbacks.remove(eventClass)
        playerCallbacks.values.forEach { it.remove(eventClass) }
        registeredListeners.remove(eventClass)?.let { HandlerList.unregisterAll(it) }
    }

    fun cancel(player: Player, eventClass: Class<out Event>) {
        val uuid = player.uniqueId
        val events = playerCallbacks[uuid] ?: return

        events.remove(eventClass)
        if (events.isEmpty()) {
            playerCallbacks.remove(uuid)
        }
    }

}
