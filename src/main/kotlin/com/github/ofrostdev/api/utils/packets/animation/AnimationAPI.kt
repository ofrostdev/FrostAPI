package com.github.ofrostdev.api.utils.packets.animation

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.concurrent.CopyOnWriteArrayList
import java.util.function.Consumer
import java.util.function.Predicate

class Animation(private val plugin: Plugin) {

    private val steps = CopyOnWriteArrayList<AnimationStep>()
    private var everyTickAction: Consumer<Collection<Player>>? = null
    private var durationTicks: Long = 0
    private var interval: Long = 1
    private var loop: Boolean = false
    private var targetPredicate: Predicate<Player>? = null
    private var explicitTargets: Collection<Player>? = null
    private var onFinish: Runnable? = null

    private var currentTick: Long = 0
    private var paused: Boolean = false
    private var task: BukkitRunnable? = null

    fun setInterval(ticks: Long): Animation {
        interval = if (ticks < 1) 1 else ticks
        return this
    }

    fun setDuration(ticks: Long): Animation {
        durationTicks = ticks.coerceAtLeast(0)
        return this
    }

    fun setLoop(loop: Boolean): Animation {
        this.loop = loop
        return this
    }

    fun everyTick(action: Consumer<Collection<Player>>): Animation {
        everyTickAction = action
        return this
    }

    fun step(tick: Long, action: Consumer<Collection<Player>>): Animation {
        require(tick >= 0) { "tick < 0" }
        steps.add(AnimationStep(tick, action))
        return this
    }

    fun setTarget(player: Player): Animation {
        explicitTargets = listOf(player)
        return this
    }

    fun setTarget(players: Collection<Player>): Animation {
        explicitTargets = players
        return this
    }

    fun setTargetPredicate(predicate: Predicate<Player>): Animation {
        targetPredicate = predicate
        return this
    }

    fun onFinish(callback: Runnable): Animation {
        onFinish = callback
        return this
    }

    fun start() {
        stop()
        currentTick = 0
        paused = false

        task = object : BukkitRunnable() {
            override fun run() {
                if (paused) return

                val targets = resolveTargets().filter { it.isOnline }

                everyTickAction?.let {
                    try { it.accept(targets) } catch (t: Throwable) { t.printStackTrace() }
                }

                for (step in steps) {
                    if (step.tick == currentTick) {
                        try { step.action.accept(targets) } catch (t: Throwable) { t.printStackTrace() }
                    }
                }

                currentTick += interval
                if (durationTicks in 1..currentTick) {
                    if (loop) {
                        currentTick = 0
                    } else {
                        stop()
                        onFinish?.run()
                    }
                }
            }
        }
        task?.runTaskTimer(plugin, 0L, interval)
    }

    fun stop() {
        task?.cancel()
        task = null
        paused = false
    }

    fun pause() {
        paused = true
    }

    fun resume() {
        paused = false
    }

    fun isPaused(): Boolean = paused
    fun isRunning(): Boolean = task != null

    private fun resolveTargets(): List<Player> {
        explicitTargets?.let { return ArrayList(it) }
        val online = Bukkit.getOnlinePlayers()
        return targetPredicate?.let { online.filter(it::test) } ?: ArrayList(online)
    }

    private data class AnimationStep(val tick: Long, val action: Consumer<Collection<Player>>)
}
