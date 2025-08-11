package com.github.ofrostdev.api.utils.animation;

import com.github.ofrostdev.api.task.TaskController;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class Animation {

    private final List<AnimationStep> steps = new CopyOnWriteArrayList<>();
    private Consumer<Collection<Player>> everyTickAction = null;
    private long durationTicks = 0;
    private long interval = 1;
    private boolean loop = false;
    private Predicate<Player> targetPredicate = null;
    private Collection<Player> explicitTargets = null;

    private BukkitTask task;
    private long currentTick;
    private boolean paused = false;
    private Runnable onFinish = null;

    public Animation setInteraval(long ticks) {
        if (ticks < 1) ticks = 1;
        this.interval = ticks;
        return this;
    }

    public Animation setDuration(long ticks) {
        this.durationTicks = Math.max(0, ticks);
        return this;
    }

    public Animation setLoop(boolean loop) {
        this.loop = loop;
        return this;
    }

    public Animation everyTick(Consumer<Collection<Player>> action) {
        this.everyTickAction = action;
        return this;
    }

    public Animation step(long tick, Consumer<Collection<Player>> action) {
        if (tick < 0) throw new IllegalArgumentException("tick < 0");
        steps.add(new AnimationStep(tick, action));
        return this;
    }

    public Animation setTarget(Player player) {
        this.explicitTargets = Collections.singletonList(player);
        return this;
    }

    public Animation setTarget(Collection<Player> players) {
        this.explicitTargets = players;
        return this;
    }

    public Collection<Player> getTargets() {
        if (explicitTargets != null) return new ArrayList<>(explicitTargets);
        return Collections.emptyList();
    }


    public Animation setTargetPredicate(Predicate<Player> predicate) {
        this.targetPredicate = predicate;
        return this;
    }

    public Animation onFinish(Runnable callback) {
        this.onFinish = callback;
        return this;
    }

    private TaskController taskController;

    public void start() {
        stop();
        this.currentTick = 0;
        this.paused = false;

        taskController = new TaskController(false, TaskController.Type.RUN_TIMER, 0L, interval) {
            @Override
            public void handle() {
                if (paused) return;
                Collection<Player> targets = resolveTargets();
                targets.removeIf(p -> !p.isOnline());

                if (everyTickAction != null) {
                    try { everyTickAction.accept(targets); } catch (Throwable t) { t.printStackTrace(); }
                }

                for (AnimationStep step : steps) {
                    if (step.getTick() == currentTick) {
                        try { step.getAction().accept(targets); } catch (Throwable t) { t.printStackTrace(); }
                    }
                }

                currentTick += interval;
                if (durationTicks > 0 && currentTick >= durationTicks) {
                    if (loop) {
                        currentTick = 0;
                    } else {
                        stop();
                        if (onFinish != null) {
                            try { onFinish.run(); } catch (Throwable t) { t.printStackTrace(); }
                        }
                    }
                }
            }
        };
        taskController.start();
    }

    public void stop() {
        if (taskController != null) {
            taskController.stop();
            taskController = null;
        }
        this.paused = false;
    }

    public boolean isRunning() {
        return taskController != null && taskController.isRunning();
    }

    public void pause() {
        this.paused = true;
    }

    public void resume() {
        this.paused = false;
    }

    public boolean isPaused() {
        return paused;
    }

    private Collection<Player> resolveTargets() {
        if (explicitTargets != null) return new ArrayList<>(explicitTargets);
        Collection<? extends Player> online = Bukkit.getOnlinePlayers();
        if (targetPredicate == null) return new ArrayList<>(online);
        List<Player> out = new ArrayList<>();
        for (Player p : online) if (targetPredicate.test(p)) out.add(p);
        return out;
    }

    private static class AnimationStep {
        private final long tick;
        private final Consumer<Collection<Player>> action;

        AnimationStep(long tick, Consumer<Collection<Player>> action) {
            this.tick = tick;
            this.action = action;
        }

        long getTick() { return tick; }
        Consumer<Collection<Player>> getAction() { return action; }
    }
}
