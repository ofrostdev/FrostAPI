package com.github.ofrostdev.api.utils.common.inventory

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.math.max
import kotlin.math.min

object InventoryHelper {
    fun isInventoryFull(player: Player): Boolean {
        return player.inventory.firstEmpty() == -1
    }

    fun getEmptySlots(player: Player): Int {
        var empty = 0
        for (item in player.inventory.contents) {
            if (item == null || item.type == Material.AIR) empty++
        }
        return empty
    }

    fun addItem(player: Player, item: ItemStack): Boolean {
        if (!hasSpace(player, item)) return false

        for (stack in player.inventory.contents) {
            if (stack != null && stack.isSimilar(item) && stack.amount < stack.maxStackSize) {
                val space = stack.maxStackSize - stack.amount
                val toAdd = min(space.toDouble(), item.amount.toDouble()).toInt()
                stack.amount += toAdd
                item.amount -= toAdd
            }
        }

        if (item.amount > 0) player.inventory.addItem(item)

        return true
    }

    fun removeItem(player: Player, material: Material, amount: Int): Boolean {
        var remaining = amount
        for (item in player.inventory.contents) {
            if (item != null && item.type == material) {
                val toRemove = min(item.amount.toDouble(), remaining.toDouble()).toInt()
                item.amount -= toRemove
                remaining -= toRemove
                if (item.amount <= 0) player.inventory.remove(item)
                if (remaining <= 0) break
            }
        }
        return remaining <= 0
    }

    fun countItem(player: Player, material: Material): Int {
        var count = 0
        for (item in player.inventory.contents) {
            if (item != null && item.type == material) count += item.amount
        }
        return count
    }

    fun hasItem(player: Player, material: Material, amount: Int): Boolean {
        return countItem(player, material) >= amount
    }

    fun clearInventory(player: Player) {
        player.inventory.clear()
        player.inventory.armorContents = arrayOfNulls(4)
    }

    fun containsAny(player: Player, vararg materials: Material): Boolean {
        for (mat in materials) {
            if (hasItem(player, mat, 1)) return true
        }
        return false
    }

    fun firstSlotOf(player: Player, material: Material): Int {
        val contents = player.inventory.contents
        for (i in contents.indices) {
            if (contents[i] != null && contents[i]!!.type == material) return i
        }
        return -1
    }

    fun moveItem(player: Player, fromSlot: Int, toSlot: Int) {
        val contents = player.inventory.contents
        val temp = contents[fromSlot]
        contents[fromSlot] = contents[toSlot]
        contents[toSlot] = temp
        player.inventory.contents = contents
    }

    fun hasSpace(player: Player, item: ItemStack): Boolean {
        val maxStack = item.maxStackSize
        var remaining = item.amount

        for (stack in player.inventory.contents) {
            if (stack == null || stack.type == Material.AIR) remaining -= maxStack
            else if (stack.isSimilar(item)) remaining -= (maxStack - stack.amount)

            if (remaining <= 0) return true
        }
        return false
    }

    fun getAvailableSpace(player: Player, item: ItemStack): Int {
        val maxStack = item.maxStackSize
        var remaining = item.amount

        for (stack in player.inventory.contents) {
            if (stack == null || stack.type == Material.AIR) remaining -= maxStack
            else if (stack.isSimilar(item)) remaining -= (maxStack - stack.amount)

            if (remaining <= 0) return item.amount
        }
        return max(0.0, (item.amount - remaining).toDouble()).toInt()
    }
}
