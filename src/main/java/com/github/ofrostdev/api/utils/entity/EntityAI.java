package com.github.ofrostdev.api.utils.entity;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.util.UnsafeList;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

public class EntityAI {

    private final EntityCreature creature;

    private Class<?> targetClass = null;
    private Player targetPlayer = null;
    private boolean move = false;
    private boolean lookPlayer = false;
    private boolean lookAround = false;

    private double moveSpeed = 1.0D;
    private int meleePriority = 2;
    private int rangedPriority = 3;

    public EntityAI(LivingEntity entity) {
        this.creature = (EntityCreature) ((EntityInsentient) ((CraftEntity) entity).getHandle());
    }

    public EntityAI setTarget(Class<?> targetClass) {
        this.targetClass = targetClass;
        this.targetPlayer = null;
        return this;
    }

    public EntityAI setTarget(Player player) {
        this.targetPlayer = player;
        this.targetClass = null;
        return this;
    }

    public EntityAI canMove(boolean move) {
        this.move = move;
        return this;
    }

    public EntityAI setLookPlayer(boolean lookPlayer) {
        this.lookPlayer = lookPlayer;
        return this;
    }

    public EntityAI setLookAround(boolean lookAround) {
        this.lookAround = lookAround;
        return this;
    }

    public EntityAI setMoveSpeed(double speed) {
        this.moveSpeed = speed;
        return this;
    }

    public EntityAI setMeleePriority(int priority) {
        this.meleePriority = priority;
        return this;
    }

    public EntityAI setRangedPriority(int priority) {
        this.rangedPriority = priority;
        return this;
    }

    public void build() {
        try {
            Field bField = PathfinderGoalSelector.class.getDeclaredField("b");
            Field cField = PathfinderGoalSelector.class.getDeclaredField("c");
            bField.setAccessible(true);
            cField.setAccessible(true);

            bField.set(creature.goalSelector, new UnsafeList<>());
            bField.set(creature.targetSelector, new UnsafeList<>());
            cField.set(creature.goalSelector, new UnsafeList<>());
            cField.set(creature.targetSelector, new UnsafeList<>());

            creature.goalSelector.a(0, new PathfinderGoalFloat(creature));

            if (move) {
                creature.goalSelector.a(5, new PathfinderGoalMoveTowardsRestriction(creature, moveSpeed));
                creature.goalSelector.a(7, new PathfinderGoalRandomStroll(creature, moveSpeed));
            }

            if (lookAround) {
                creature.goalSelector.a(9, new PathfinderGoalRandomLookaround(creature));
            }

            if (targetPlayer != null) {
                creature.goalSelector.a(meleePriority, new PathfinderGoalMeleeAttack(creature, moveSpeed, true));
                creature.targetSelector.a(meleePriority, new PathfinderGoalNearestAttackableTarget(creature, targetPlayer.getClass(), true));

                if (lookPlayer) {
                    creature.goalSelector.a(rangedPriority, new PathfinderGoalFollowPlayer(creature, targetPlayer, moveSpeed, 2.0F, 1.0F, 3));
                } else {
                    creature.goalSelector.a(rangedPriority, new PathfinderGoalFollowPlayer(creature, targetPlayer, moveSpeed, 2.0F, 1.0F, 1));
                    creature.goalSelector.a(8, new PathfinderGoalLookAtPlayer(creature, EntityHuman.class, 8.0F));
                }

            } else if (targetClass != null) {
                creature.goalSelector.a(meleePriority, new PathfinderGoalMeleeAttack(creature, moveSpeed, true));
                creature.targetSelector.a(meleePriority, new PathfinderGoalNearestAttackableTarget(creature, targetClass, 0, true, false, null));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
