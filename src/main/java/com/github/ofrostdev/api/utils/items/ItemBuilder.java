package com.github.ofrostdev.api.utils.items;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import net.minecraft.server.v1_8_R3.NBTTagList;
import net.minecraft.server.v1_8_R3.NBTTagString;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.*;

public class ItemBuilder {
    private ItemStack is;

    public ItemBuilder(Material m) {
        this(m, 1);
    }

    public ItemBuilder(String url) {
        this.is = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        SkullMeta meta = (SkullMeta) this.is.getItemMeta();

        if (!url.contains("textures.minecraft.net")) {
            meta.setOwner(url);
        } else {
            GameProfile profile = new GameProfile(UUID.randomUUID(), null);
            String json = String.format("{textures:{SKIN:{url:\"%s\"}}}", url);
            byte[] encoded = Base64.getEncoder().encode(json.getBytes());
            profile.getProperties().put("textures", new Property("textures", new String(encoded)));

            try {
                Field field = meta.getClass().getDeclaredField("profile");
                field.setAccessible(true);
                field.set(meta, profile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        this.is.setItemMeta(meta);
    }

    public ItemBuilder(ItemStack is) {
        this.is = is;
    }

    public ItemBuilder(Material m, int quantia) {
        this.is = new ItemStack(m, quantia);
    }

    public ItemBuilder(Material m, int quantia, byte data) {
        this.is = new ItemStack(m, quantia, data);
    }

    public ItemBuilder(Material m, int quantia, int data) {
        this.is = new ItemStack(m, quantia, (short) data);
    }

    public ItemBuilder clone() {
        return new ItemBuilder(this.is);
    }

    public ItemBuilder setData(short data) {
        this.is.setDurability(data);
        return this;
    }

    public ItemBuilder setAmount(int amount) {
        this.is.setAmount(amount);
        ItemMeta im = this.is.getItemMeta();
        im.addItemFlags(new ItemFlag[] { ItemFlag.HIDE_POTION_EFFECTS });
        this.is.setItemMeta(im);
        return this;
    }

    public ItemBuilder setDurability(String durabilidade) {
        this.is.setDurability(Short.valueOf(durabilidade).shortValue());
        return this;
    }

    public ItemBuilder setName(String nome) {
        ItemMeta im = this.is.getItemMeta();
        im.setDisplayName(nome);
        this.is.setItemMeta(im);
        return this;
    }

    public ItemBuilder setNBTString(String key, String value) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(this.is);
        if (!nmsItem.hasTag()) {
            nmsItem.setTag(new NBTTagCompound());
        }
        NBTTagCompound tag = nmsItem.getTag();
        tag.setString(key, value);
        nmsItem.setTag(tag);
        this.is = CraftItemStack.asBukkitCopy(nmsItem);
        return this;
    }

    public ItemBuilder setNBTStringList(String key, List<String> list) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(this.is);
        if (!nmsItem.hasTag()) {
            nmsItem.setTag(new NBTTagCompound());
        }
        NBTTagCompound tag = nmsItem.getTag();
        NBTTagList nbtList = new NBTTagList();
        for (String value : list) {
            nbtList.add(new NBTTagString(value));
        }
        tag.set(key, nbtList);
        nmsItem.setTag(tag);
        this.is = CraftItemStack.asBukkitCopy(nmsItem);
        return this;
    }

    public ItemBuilder setNBTInt(String key, int value) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(this.is);
        if (!nmsItem.hasTag()) {
            nmsItem.setTag(new NBTTagCompound());
        }
        NBTTagCompound tag = nmsItem.getTag();
        tag.setInt(key, value);
        nmsItem.setTag(tag);
        this.is = CraftItemStack.asBukkitCopy(nmsItem);
        return this;
    }

    public ItemBuilder setNBTBoolean(String key, boolean value) {
        net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(this.is);
        if (!nmsItem.hasTag()) {
            nmsItem.setTag(new NBTTagCompound());
        }
        NBTTagCompound tag = nmsItem.getTag();
        tag.setBoolean(key, value);
        nmsItem.setTag(tag);
        this.is = CraftItemStack.asBukkitCopy(nmsItem);
        return this;
    }

    public ItemBuilder setGlowing(boolean glow) {
        net.minecraft.server.v1_8_R3.ItemStack nmsStack = CraftItemStack.asNMSCopy(this.is);
        NBTTagCompound tag = (nmsStack.hasTag()) ? nmsStack.getTag() : new NBTTagCompound();
        if (glow) {
            tag.set("ench", new NBTTagList());
        } else {
            tag.remove("ench");
        }
        nmsStack.setTag(tag);
        this.is = CraftItemStack.asBukkitCopy(nmsStack);
        return this;
    }


    public ItemBuilder addUnsafeEnchantment(Enchantment ench, int level) {
        this.is.addUnsafeEnchantment(ench, level);
        return this;
    }

    public ItemBuilder removeEnchantment(Enchantment ench) {
        this.is.removeEnchantment(ench);
        return this;
    }

    public ItemBuilder setSkullOwner(String dono) {
        try {
            SkullMeta im = (SkullMeta) this.is.getItemMeta();
            im.setOwner(dono);
            this.is.setItemMeta((ItemMeta) im);
        } catch (ClassCastException classCastException) {
        }
        return this;
    }

    public ItemBuilder addEnchant(Enchantment ench, int level) {
        ItemMeta im = this.is.getItemMeta();
        im.addEnchant(ench, level, true);
        this.is.setItemMeta(im);
        return this;
    }

    public ItemBuilder addEnchantments(Map<Enchantment, Integer> enchantments) {
        this.is.addEnchantments(enchantments);
        return this;
    }

    public ItemBuilder setInfinityDurability() {
        this.is.setDurability((short) '?');
        return this;
    }

    public ItemBuilder addItemFlag(ItemFlag flag) {
        ItemMeta im = this.is.getItemMeta();
        im.addItemFlags(new ItemFlag[] { flag });
        this.is.setItemMeta(im);
        return this;
    }

    public ItemBuilder setLore(String... lore) {
        ItemMeta im = this.is.getItemMeta();
        im.setLore(Arrays.asList(lore));
        this.is.setItemMeta(im);
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        ItemMeta im = this.is.getItemMeta();
        im.setLore(lore);
        this.is.setItemMeta(im);
        return this;
    }

    public ItemBuilder removeLoreLine(String linha) {
        ItemMeta im = this.is.getItemMeta();
        List<String> lore = new ArrayList<>(im.getLore());
        if (!lore.contains(linha))
            return this;
        lore.remove(linha);
        im.setLore(lore);
        this.is.setItemMeta(im);
        return this;
    }

    public ItemBuilder removeLoreLine(int index) {
        ItemMeta im = this.is.getItemMeta();
        List<String> lore = new ArrayList<>(im.getLore());
        if (index < 0 || index > lore.size())
            return this;
        lore.remove(index);
        im.setLore(lore);
        this.is.setItemMeta(im);
        return this;
    }

    public ItemBuilder addLoreLine(String linha) {
        ItemMeta im = this.is.getItemMeta();
        List<String> lore = new ArrayList<>();
        if (im.hasLore())
            lore = new ArrayList<>(im.getLore());
        lore.add(linha);
        im.setLore(lore);
        this.is.setItemMeta(im);
        return this;
    }

    public ItemBuilder addLores(List<String> linha) {
        ItemMeta im = this.is.getItemMeta();
        List<String> lore = new ArrayList<>();
        if (im.hasLore())
            lore = new ArrayList<>(im.getLore());
        for (String s : linha) {
            lore.add(s);
        }
        im.setLore(lore);
        this.is.setItemMeta(im);
        return this;
    }

    public ItemBuilder addLoreLine(String linha, int pos) {
        ItemMeta im = this.is.getItemMeta();
        List<String> lore = new ArrayList<>(im.getLore());
        lore.set(pos, linha);
        im.setLore(lore);
        this.is.setItemMeta(im);
        return this;
    }

    public ItemBuilder owner(String owner) {
        try {
            SkullMeta im = (SkullMeta) this.is.getItemMeta();
            im.setOwner(owner);
            this.is.setItemMeta((ItemMeta) im);
        } catch (ClassCastException classCastException) {
        }

        return this;
    }

    public ItemBuilder lore(String... lore) {
        ItemMeta im = this.is.getItemMeta();
        List<String> out = (im.getLore() == null) ? new ArrayList<>() : im.getLore();
        byte b;
        int i;
        String[] arrayOfString;
        for (i = (arrayOfString = lore).length, b = 0; b < i;) {
            String string = arrayOfString[b];
            out.add(ChatColor.translateAlternateColorCodes('&', string));
            b++;
        }
        im.setLore(out);
        this.is.setItemMeta(im);
        return this;
    }

    public ItemBuilder setLore2(String... lore) {
        ItemMeta im = this.is.getItemMeta();
        List<String> out = (im.getLore() == null) ? new ArrayList<>() : im.getLore(); byte b; int i; String[] arrayOfString;
        for (i = (arrayOfString = lore).length, b = 0; b < i; ) { String string = arrayOfString[b];
            out.add(ChatColor.translateAlternateColorCodes('&', string)); b++; }
        im.setLore(out);
        this.is.setItemMeta(im);
        return this;
    }

    public ItemBuilder removeAttributes() {
        ItemMeta meta = this.is.getItemMeta();
        meta.addItemFlags(ItemFlag.values());
        this.is.setItemMeta(meta);
        return this;
    }

    public ItemBuilder color(Color color) {
        if (!this.is.getType().name().contains("LEATHER_"))
            return this;
        LeatherArmorMeta meta = (LeatherArmorMeta) this.is.getItemMeta();
        meta.setColor(color);
        this.is.setItemMeta((ItemMeta) meta);
        return this;
    }

    @SuppressWarnings("deprecation")
    public ItemBuilder setDyeColor(DyeColor cor) {
        this.is.setDurability(cor.getDyeData());
        return this;
    }

    @Deprecated
    public ItemBuilder setWoolColor(DyeColor cor) {
        if (!this.is.getType().equals(Material.WOOL))
            return this;
        this.is.setDurability(cor.getDyeData());
        return this;
    }

    public ItemBuilder setLeatherArmorColor(Color cor) {
        try {
            LeatherArmorMeta im = (LeatherArmorMeta) this.is.getItemMeta();
            im.setColor(cor);
            this.is.setItemMeta((ItemMeta) im);
        } catch (ClassCastException classCastException) {
        }
        return this;
    }

    public ItemStack toItemStack() {
        return this.is;
    }

    public ItemStack build() {
        return this.is;
    }
}