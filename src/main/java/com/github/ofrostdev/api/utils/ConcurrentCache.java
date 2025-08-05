package com.github.ofrostdev.api.utils;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ConcurrentCache<K, V> {
    private Map<K, V> elements = new ConcurrentHashMap<>();

    public Map<K, V> getElements() {
        return this.elements;
    }

    public boolean contains(K element) {
        return (this.elements.get(element) != null);
    }

    public void addElement(K key, V value) {
        this.elements.put(key, value);
    }

    public void removeElement(K toRemove) {
        this.elements.remove(toRemove);
    }

    public V get(K key) {
        return this.elements.get(key);
    }

    public Collection<V> getAll() {
        return getElements().values();
    }

    public Optional<V> find(K key) {
        return Optional.ofNullable(get(key));
    }

    public ImmutableMap<K, V> toImmutable() {
        return ImmutableMap.copyOf(this.elements);
    }

    public Iterator<V> iterator() {
        return this.elements.values().iterator();
    }

    public Iterator<K> iteratorKeySet() {
        return this.elements.keySet().iterator();
    }

    public int size() {
        return this.elements.size();
    }
}