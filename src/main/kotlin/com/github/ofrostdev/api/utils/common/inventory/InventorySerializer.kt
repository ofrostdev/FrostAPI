package com.github.ofrostdev.api.utils.common.inventory

import com.github.ofrostdev.api.utils.common.item.ItemSerializer
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack

object InventorySerializer {

    fun encodeInventory(player: Player): String? {
        val inventory = player.inventory
        val allItems = inventory.contents + inventory.armorContents
        return ItemSerializer.encodeItems(allItems)
    }

    fun decodeInventory(player: Player, base64: String) {
        val allItems = ItemSerializer.decodeItems(base64) ?: return

        val inventory = player.inventory
        val armorSize = 4
        val contentsSize = allItems.size - armorSize
        if (contentsSize < 0) return

        val contents = allItems.copyOfRange(0, contentsSize)
        val armor = allItems.copyOfRange(contentsSize, allItems.size)

        inventory.contents = contents
        inventory.armorContents = armor
    }

    fun encodeInventory(inventory: Inventory): String? {
        return ItemSerializer.encodeItems(inventory.contents)
    }

    fun decodeInventory(base64: String): Array<ItemStack?>? {
        return ItemSerializer.decodeItems(base64)
    }
}
