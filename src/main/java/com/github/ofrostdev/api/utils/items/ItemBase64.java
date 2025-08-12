package com.github.ofrostdev.api.utils.items;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

public class ItemBase64 {

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
}
