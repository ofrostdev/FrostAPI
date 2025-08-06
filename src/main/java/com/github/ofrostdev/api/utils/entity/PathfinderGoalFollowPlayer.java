package com.github.ofrostdev.api.utils.entity;

import net.minecraft.server.v1_8_R3.EntityCreature;
import net.minecraft.server.v1_8_R3.PathfinderGoal;
import org.bukkit.entity.Player;

public class PathfinderGoalFollowPlayer extends PathfinderGoal {

    private final EntityCreature entity;
    private final Player player;
    private final double speed;
    private final float maxDist;
    private final float minDist;

    public PathfinderGoalFollowPlayer(EntityCreature entity, Player player, double speed, float maxDist, float minDist) {
        this.entity = entity;
        this.player = player;
        this.speed = speed;
        this.maxDist = maxDist;
        this.minDist = minDist;
        a(1); // Control flags
    }

    @Override
    public boolean a() {
        if (player == null || player.isDead()) return false;
        double dist = entity.getBukkitEntity().getLocation().distance(player.getLocation());
        return dist > minDist;
    }

    @Override
    public boolean b() {
        if (player == null || player.isDead()) return false;
        double dist = entity.getBukkitEntity().getLocation().distance(player.getLocation());
        return dist > maxDist;
    }

    @Override
    public void c() {
        // Start following
    }

    @Override
    public void d() {
        // Stop following
        entity.getNavigation().n();
    }

    @Override
    public void e() {
        entity.getNavigation().a(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), speed);
    }
}
