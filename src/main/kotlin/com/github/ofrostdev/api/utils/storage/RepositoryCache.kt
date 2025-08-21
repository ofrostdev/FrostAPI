package com.github.ofrostdev.api.utils.storage

import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function
import java.util.function.Supplier


class RepositoryCache<K, V>(
    private val loadFunction: Function<K, V>,
    private val loadAllFunction: Supplier<Map<K, V>>?
) {
    private val cache: MutableMap<K, V> = ConcurrentHashMap()

    fun get(key: K): CompletableFuture<V?> {
        val value = cache[key]
        if (value != null) {
            return CompletableFuture.completedFuture(value)
        }

        return CompletableFuture.supplyAsync {
            val loaded: V? = loadFunction.apply(key)
            if (loaded != null) cache[key] = loaded
            loaded
        }
    }

    fun getIfPresent(key: K): Optional<V & Any> {
        return Optional.ofNullable(cache[key])
    }

    fun reload(key: K): CompletableFuture<V?> {
        return CompletableFuture.supplyAsync {
            val value: V? = loadFunction.apply(key)
            if (value != null) cache[key] = value
            value
        }
    }

    fun loadAll(): CompletableFuture<Void?> {
        if (loadAllFunction == null) return CompletableFuture.completedFuture(null)

        return CompletableFuture.runAsync {
            cache.clear()
            cache.putAll(loadAllFunction.get())
        }
    }

    fun put(key: K, value: V) {
        cache[key] = value
    }

    fun remove(key: K) {
        cache.remove(key)
    }

    fun asMap(): Map<K, V> {
        return cache
    }

    fun clear() {
        cache.clear()
    }
}