package com.github.ofrostdev.api.utils.common

import java.util.*
import java.util.concurrent.ConcurrentHashMap

class CooldownAPI {
    private val cooldowns: MutableMap<UUID, Long> = ConcurrentHashMap()

    fun setCooldown(id: UUID, duration: String) {
        val millis = TimeUtils.parseDuration(duration)
        cooldowns[id] = System.currentTimeMillis() + millis
    }

    fun setCooldown(id: UUID, millis: Long) {
        cooldowns[id] = System.currentTimeMillis() + millis
    }

    fun hasCooldown(id: UUID): Boolean = getRemaining(id).millis > 0

    fun getRemaining(id: UUID): RemainingTime {
        val expireTime = cooldowns[id]
        var remaining = (expireTime ?: 0) - System.currentTimeMillis()
        if (remaining <= 0) {
            cooldowns.remove(id)
            remaining = 0
        }
        return RemainingTime(remaining)
    }

    fun remove(id: UUID) {
        cooldowns.remove(id)
    }

    fun clear() {
        cooldowns.clear()
    }

    class RemainingTime(val millis: Long) {
        val seconds: Long get() = millis / 1000
        val minutes: Long get() = millis / 1000 / 60
        val hours: Long get() = millis / 1000 / 60 / 60
    }

    class DSL(private val api: CooldownAPI) {
        infix fun UUID.set(duration: String) = api.setCooldown(this, duration)
        infix fun UUID.setMillis(millis: Long) = api.setCooldown(this, millis)
        fun UUID.has() = api.hasCooldown(this)
        fun UUID.remaining() = api.getRemaining(this)
        fun UUID.remove() = api.remove(this)
        fun clear() = api.clear()
    }

    fun dsl(block: DSL.() -> Unit) = DSL(this).apply(block)
}
