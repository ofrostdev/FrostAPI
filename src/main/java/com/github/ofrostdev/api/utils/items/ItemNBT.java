package com.github.ofrostdev.api.utils.items;

import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class ItemNBT {

    private static void consume(ItemStack itemStack, Consumer<NBTTagCompound> consumer) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();
        consumer.accept(tag);
        nmsItem.setTag(tag);
        itemStack.setItemMeta(CraftItemStack.asBukkitCopy(nmsItem).getItemMeta());
    }

    private static <T> T supply(ItemStack itemStack, Function<NBTTagCompound, T> function) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(itemStack);
        NBTTagCompound tag = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();
        return function.apply(tag);
    }

    public static void setString(ItemStack stack, String key, String value) {
        consume(stack, compound -> compound.setString(key, value));
    }

    public static void setInt(ItemStack stack, String key, int value) {
        consume(stack, compound -> compound.setInt(key, value));
    }

    public static void setDouble(ItemStack stack, String key, double value) {
        consume(stack, compound -> compound.setDouble(key, value));
    }

    public static void setBoolean(ItemStack stack, String key, boolean value) {
        consume(stack, compound -> compound.setBoolean(key, value));
    }

    public static void setLong(ItemStack stack, String key, long value) {
        consume(stack, compound -> compound.setLong(key, value));
    }

    public static void setFloat(ItemStack stack, String key, float value) {
        consume(stack, compound -> compound.setFloat(key, value));
    }

    public static void setByte(ItemStack stack, String key, byte value) {
        consume(stack, compound -> compound.setByte(key, value));
    }

    public static void setShort(ItemStack stack, String key, short value) {
        consume(stack, compound -> compound.setShort(key, value));
    }

    public static void setStringList(ItemStack stack, String key, List<String> list) {
        consume(stack, compound -> {
            NBTTagList tagList = new NBTTagList();
            for (String s : list) tagList.add(new NBTTagString(s));
            compound.set(key, tagList);
        });
    }

    public static void addTag(ItemStack stack, String key, NBTBase value) {
        consume(stack, compound -> compound.set(key, value));
    }

    public static String getString(ItemStack stack, String key) {
        return supply(stack, compound -> compound.getString(key));
    }

    public static int getInt(ItemStack stack, String key) {
        return supply(stack, compound -> compound.getInt(key));
    }

    public static double getDouble(ItemStack stack, String key) {
        return supply(stack, compound -> compound.getDouble(key));
    }

    public static boolean getBoolean(ItemStack stack, String key) {
        return supply(stack, compound -> compound.getBoolean(key));
    }

    public static long getLong(ItemStack stack, String key) {
        return supply(stack, compound -> compound.getLong(key));
    }

    public static float getFloat(ItemStack stack, String key) {
        return supply(stack, compound -> compound.getFloat(key));
    }

    public static byte getByte(ItemStack stack, String key) {
        return supply(stack, compound -> compound.getByte(key));
    }

    public static short getShort(ItemStack stack, String key) {
        return supply(stack, compound -> compound.getShort(key));
    }

    public static List<String> getStringList(ItemStack item, String key) {
        return supply(item, compound -> {
            if (!compound.hasKey(key)) return null;
            NBTTagList list = compound.getList(key, 8);
            List<String> result = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                result.add(list.getString(i));
            }
            return result;
        });
    }

    public static boolean hasTag(ItemStack item, String key) {
        return supply(item, tag -> tag.hasKey(key));
    }

    public static ItemStack removeTag(ItemStack item, String key) {
        net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
        if (!nms.hasTag()) return item;

        NBTTagCompound tag = nms.getTag();
        tag.remove(key);
        nms.setTag(tag);
        return CraftItemStack.asBukkitCopy(nms);
    }

    public static ItemStack clearTags(ItemStack item) {
        net.minecraft.server.v1_8_R3.ItemStack nms = CraftItemStack.asNMSCopy(item);
        nms.setTag(new NBTTagCompound());
        return CraftItemStack.asBukkitCopy(nms);
    }

    public static ItemStack mergeTags(ItemStack baseItem, ItemStack otherItem) {
        net.minecraft.server.v1_8_R3.ItemStack base = CraftItemStack.asNMSCopy(baseItem);
        net.minecraft.server.v1_8_R3.ItemStack other = CraftItemStack.asNMSCopy(otherItem);

        if (!base.hasTag()) base.setTag(new NBTTagCompound());
        if (other.hasTag()) base.getTag().a(other.getTag());

        return CraftItemStack.asBukkitCopy(base);
    }

    public static List<String> getAllKeys(ItemStack item) {
        return supply(item, compound -> {
            List<String> keys = new ArrayList<>();
            for (String key : compound.c()) {
                keys.add(key);
            }
            return keys;
        });
    }
}
