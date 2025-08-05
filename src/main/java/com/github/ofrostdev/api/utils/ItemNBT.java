package com.github.ofrostdev.api.utils;

import net.minecraft.server.v1_8_R3.NBTBase;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.inventory.ItemStack;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemNBT {

    public static ItemStack setString(ItemStack item, String key, String value) {
        if (item == null) return null;
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        if (!nmsItem.hasTag()) {
            nmsItem.setTag(new NBTTagCompound());
        }
        nmsItem.getTag().set(key, new NBTTagString(value));
        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    public static String getString(ItemStack item, String key) {
        if (item == null) return null;
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        if (nmsItem.hasTag() && nmsItem.getTag().hasKey(key)) {
            return nmsItem.getTag().getString(key);
        }
        return null;
    }

    public static ItemStack setStringList(ItemStack item, String key, List<String> list) {
        if (item == null || list == null) return item;

        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        NBTTagCompound compound = (nmsItem.hasTag()) ? nmsItem.getTag() : new NBTTagCompound();

        NBTTagList nbtList = new NBTTagList();
        for (String s : list) {
            nbtList.add(new NBTTagString(s));
        }

        compound.set(key, nbtList);
        nmsItem.setTag(compound);

        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    public static List<String> getStringList(ItemStack item, String key) {
        if (item == null) return null;

        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        NBTTagCompound compound = nmsItem.getTag();

        if (compound == null || !compound.hasKey(key)) return null;

        NBTTagList list = compound.getList(key, 8);

        List<String> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            result.add(list.getString(i));
        }

        return result;
    }

    public static ItemStack removeTag(ItemStack item, String key) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        if (nmsItem == null) return item;

        NBTTagCompound tag = nmsItem.hasTag() ? nmsItem.getTag() : new NBTTagCompound();
        tag.remove(key);
        nmsItem.setTag(tag);

        return CraftItemStack.asCraftMirror(nmsItem);
    }

    public static ItemStack addTag(ItemStack item, String s, NBTBase nbtBase) {
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(item);

        NBTTagCompound tag = (nmsStack.hasTag()) ? nmsStack.getTag() : new NBTTagCompound();

        tag.set(s, nbtBase);

        nmsStack.setTag(tag);
        return CraftItemStack.asBukkitCopy(nmsStack);
    }

    public static boolean hasTag(ItemStack item, String key) {
        if (item == null) return false;
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        return nmsItem.hasTag() && nmsItem.getTag().hasKey(key);
    }

    public static ItemStack mergeTags(ItemStack baseItem, ItemStack other) {
        net.minecraft.server.v1_8_R3.ItemStack nmsBase = CraftItemStack.asNMSCopy(baseItem);
        net.minecraft.server.v1_8_R3.ItemStack nmsOther = CraftItemStack.asNMSCopy(other);

        if (!nmsBase.hasTag()) nmsBase.setTag(new NBTTagCompound());
        if (nmsOther.hasTag()) {
            nmsBase.getTag().a(nmsOther.getTag());
        }
        return CraftItemStack.asBukkitCopy(nmsBase);
    }

    public static List<String> getAllKeys(ItemStack item) {
        List<String> keys = new ArrayList<>();
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        if (!nmsItem.hasTag()) return keys;

        NBTTagCompound compound = nmsItem.getTag();
        for (String key : compound.c()) {
            keys.add(key);
        }
        return keys;
    }

    public static ItemStack clearTags(ItemStack item) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        nmsItem.setTag(new NBTTagCompound());
        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    public static ItemStack setInt(ItemStack item, String key, int value) {
        if (item == null) return null;
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        if (!nmsItem.hasTag()) nmsItem.setTag(new NBTTagCompound());
        nmsItem.getTag().setInt(key, value);
        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    public static int getInt(ItemStack item, String key) {
        if (item == null) return 0;
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        return (nmsItem.hasTag() && nmsItem.getTag().hasKey(key)) ? nmsItem.getTag().getInt(key) : 0;
    }

    public static ItemStack setDouble(ItemStack item, String key, double value) {
        if (item == null) return null;
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        if (!nmsItem.hasTag()) nmsItem.setTag(new NBTTagCompound());
        nmsItem.getTag().setDouble(key, value);
        return CraftItemStack.asBukkitCopy(nmsItem);
    }

    public static double getDouble(ItemStack item, String key) {
        if (item == null) return 0.0;
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(item);
        return (nmsItem.hasTag() && nmsItem.getTag().hasKey(key)) ? nmsItem.getTag().getDouble(key) : 0.0;
    }

    public static ItemStack setBoolean(ItemStack item, String key, boolean value) {
        return setInt(item, key, value ? 1 : 0);
    }

    public static boolean getBoolean(ItemStack item, String key) {
        return getInt(item, key) == 1;
    }

}
