package com.github.ofrostdev.api.utils.common

import com.google.common.collect.ImmutableMap
import java.util.*
import java.util.concurrent.ConcurrentHashMap

abstract class ConcurrentCache<K, V> {
    private val elements: MutableMap<K, V> = ConcurrentHashMap()

    fun getElements(): Map<K, V> {
        return this.elements
    }

    fun contains(element: K): Boolean {
        return (elements[element] != null)
    }

    fun addElement(key: K, value: V) {
        elements[key] = value
    }

    fun removeElement(toRemove: K) {
        elements.remove(toRemove)
    }

    fun get(key: K): V? {
        return elements[key]
    }

    val all: Collection<V>
        get() = getElements().values

    fun find(key: K): Optional<V & Any> {
        return Optional.ofNullable(get(key))
    }

    fun toImmutable(): ImmutableMap<K, V> {
        return ImmutableMap.copyOf(this.elements)
    }

    fun iterator(): Iterator<V> {
        return elements.values.iterator()
    }

    fun iteratorKeySet(): Iterator<K> {
        return elements.keys.iterator()
    }

    fun size(): Int {
        return elements.size
    }
}