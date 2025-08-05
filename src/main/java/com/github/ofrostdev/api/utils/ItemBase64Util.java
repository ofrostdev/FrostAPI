package com.github.ofrostdev.api.utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.util.Base64;

public class ItemBase64Util {

    public static String encodeItem(ItemStack item) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             BukkitObjectOutputStream boos = new BukkitObjectOutputStream(baos)) {

            boos.writeObject(item);
            boos.flush();
            return Base64.getEncoder().encodeToString(baos.toByteArray());

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ItemStack decodeItem(String base64) {
        byte[] data = Base64.getDecoder().decode(base64);

        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             BukkitObjectInputStream bois = new BukkitObjectInputStream(bais)) {

            return (ItemStack) bois.readObject();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String encodeItems(ItemStack[] items) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             BukkitObjectOutputStream boos = new BukkitObjectOutputStream(baos)) {

            boos.writeInt(items.length);
            for (ItemStack item : items) {
                boos.writeObject(item);
            }
            boos.flush();

            return Base64.getEncoder().encodeToString(baos.toByteArray());

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ItemStack[] decodeItems(String base64) {
        byte[] data = Base64.getDecoder().decode(base64);

        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             BukkitObjectInputStream bois = new BukkitObjectInputStream(bais)) {

            int size = bois.readInt();
            ItemStack[] items = new ItemStack[size];

            for (int i = 0; i < size; i++) {
                items[i] = (ItemStack) bois.readObject();
            }

            return items;

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String encodePlayerInventory(Player player) {
        PlayerInventory inventory = player.getInventory();

        ItemStack[] contents = inventory.getContents();
        ItemStack[] armor = inventory.getArmorContents();

        ItemStack[] allItems = new ItemStack[contents.length + armor.length];

        System.arraycopy(contents, 0, allItems, 0, contents.length);
        System.arraycopy(armor, 0, allItems, contents.length, armor.length);

        return encodeItems(allItems);
    }

    public static void decodePlayerInventory(Player player, String base64) {
        ItemStack[] allItems = decodeItems(base64);

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
}
