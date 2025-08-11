package com.github.ofrostdev.api.task;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class TaskController {

    public enum Type {
        RUN_ONCE,
        RUN_TIMER
    }

    private static Plugin plugin;
    private final boolean async;
    private final Type type;

    private long delayTicks;
    private long intervalTicks;

    private BukkitTask task;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public TaskController(boolean async, Type type, long delayTicks, long intervalTicks) {
        this.async = async;
        this.type = type;
        this.delayTicks = delayTicks;
        this.intervalTicks = intervalTicks;
    }

    public static void init(Plugin plugin){
        if(TaskController.plugin != null)return;
        TaskController.plugin=plugin;
    }

    public abstract void handle();

    public void start() {

        if (plugin == null) {
            throw new IllegalArgumentException("[FrostAPI] TaskController -> Registre com.github.ofrostdev.api.FrostAPI.enable(Plugin plugin) na main!");
        }

        if (running.get()) return;
        running.set(true);

        switch (type) {
            case RUN_ONCE:
                if (async) {
                    task = Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                        if (!running.get()) return;
                        handle();
                        running.set(false);
                    }, delayTicks);
                } else {
                    task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (!running.get()) return;
                        handle();
                        running.set(false);
                    }, delayTicks);
                }
                break;
            case RUN_TIMER:
                if (async) {
                    task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
                        if (!running.get()) return;
                        handle();
                    }, delayTicks, intervalTicks);
                } else {
                    task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                        if (!running.get()) return;
                        handle();
                    }, delayTicks, intervalTicks);
                }
                break;
        }
    }

    public void stop() {
        running.set(false);
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public void restart() {
        stop();
        start();
    }

    public long getDelayTicks() {
        return delayTicks;
    }

    public void setDelayTicks(long delayTicks) {
        this.delayTicks = delayTicks;
        if (running.get()) restart();
    }

    public long getIntervalTicks() {
        return intervalTicks;
    }

    public void setIntervalTicks(long intervalTicks) {
        this.intervalTicks = intervalTicks;
        if (running.get() && type == Type.RUN_TIMER) restart();
    }

    public boolean isAsync() {
        return async;
    }

    public boolean isRunning() {
        return running.get();
    }

    public Type getType() {
        return type;
    }
}
