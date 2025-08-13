package com.github.ofrostdev.api.utils.inventory;

import com.github.ofrostdev.api.utils.items.ItemBase64;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class InventoryAPI {

    public static boolean isInventoryFull(Player player) {
        return player.getInventory().firstEmpty() == -1;
    }

    public static int getEmptySlots(Player player) {
        int empty = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null || item.getType() == Material.AIR) {
                empty++;
            }
        }
        return empty;
    }

    public static String encodeInventory(Player player) {
        PlayerInventory inventory = player.getInventory();

        ItemStack[] contents = inventory.getContents();
        ItemStack[] armor = inventory.getArmorContents();

        ItemStack[] allItems = new ItemStack[contents.length + armor.length];

        System.arraycopy(contents, 0, allItems, 0, contents.length);
        System.arraycopy(armor, 0, allItems, contents.length, armor.length);

        return ItemBase64.encodeItems(allItems);
    }

    public static void decodeInventory(Player player, String base64) {
        ItemStack[] allItems = ItemBase64.decodeItems(base64);

        if (allItems == null) return;

        PlayerInventory inventory = player.getInventory();

        int armorSize = 4;

        int contentsSize = allItems.length - armorSize;

        if (contentsSize < 0) {
            return;
        }

        ItemStack[] contents = new ItemStack[contentsSize];
        ItemStack[] armor = new ItemStack[armorSize];

        System.arraycopy(allItems, 0, contents, 0, contentsSize);
        System.arraycopy(allItems, contentsSize, armor, 0, armorSize);

        inventory.setContents(contents);
        inventory.setArmorContents(armor);
    }

    public static String encodeInventory(Inventory inventory) {
        ItemStack[] items = inventory.getContents();
        return ItemBase64.encodeItems(items);
    }

    public static ItemStack[] decodeInventory(String base64) {
        return ItemBase64.decodeItems(base64);
    }
}
