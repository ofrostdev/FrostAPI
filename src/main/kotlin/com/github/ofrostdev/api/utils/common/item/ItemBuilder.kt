package com.github.ofrostdev.api.utils.common.item

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import net.minecraft.server.v1_8_R3.NBTTagCompound
import net.minecraft.server.v1_8_R3.NBTTagList
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.util.*

@DslMarker
annotation class ItemBuilderDSL

@ItemBuilderDSL
class ItemBuilder(private var item: ItemStack) {

    constructor(material: Material, amount: Int = 1) : this(ItemStack(material, amount))
    constructor(material: Material, amount: Int, data: Int) : this(ItemStack(material, amount, data.toShort()))
    constructor(head: String) : this(ItemStack(Material.SKULL_ITEM, 1, 3.toShort())) {
        val meta = item.itemMeta as SkullMeta
        if (!head.contains("textures.minecraft.net")) meta.owner = head
        else {
            val profile = GameProfile(UUID.randomUUID(), null)
            val json = """{"textures":{"SKIN":{"url":"$head"}}}"""
            profile.properties.put("textures", Property("textures", Base64.getEncoder().encodeToString(json.toByteArray())))
            try {
                val field = meta.javaClass.getDeclaredField("profile")
                field.isAccessible = true
                field.set(meta, profile)
            } catch (_: Exception) {}
        }
        item.itemMeta = meta
    }

    fun setAmount(amount: Int) = apply { item.amount = amount; item.itemMeta?.addItemFlags(ItemFlag.HIDE_POTION_EFFECTS) }
    fun setDurability(durability: Short) = apply { item.durability = durability }
    fun setData(data: Short) = apply { item.durability = data }
    fun setName(name: String) = apply { item.itemMeta = item.itemMeta?.apply { displayName = ChatColor.translateAlternateColorCodes('&', name) } }

    fun setLore(vararg lore: String) = apply { item.itemMeta = item.itemMeta?.apply { setLore(lore.map { ChatColor.translateAlternateColorCodes('&', it) }) } }
    fun setLore(lore: List<String>) = apply { item.itemMeta = item.itemMeta?.apply { this.lore = lore.map { ChatColor.translateAlternateColorCodes('&', it) } } }
    fun addLoreLine(line: String) = apply {
        val meta = item.itemMeta
        val lore = meta?.lore?.toMutableList() ?: mutableListOf()
        lore.add(ChatColor.translateAlternateColorCodes('&', line))
        meta?.lore = lore
        item.itemMeta = meta
    }
    fun addLores(lines: List<String>) = apply { lines.forEach { addLoreLine(it) } }
    fun removeLoreLine(line: String) = apply {
        val meta = item.itemMeta
        val lore = meta?.lore?.toMutableList() ?: return@apply
        lore.remove(line)
        meta.lore = lore
        item.itemMeta = meta
    }

    fun addEnchantment(enchantment: Enchantment, level: Int) = apply { item.itemMeta = item.itemMeta?.apply { addEnchant(enchantment, level, true) } }
    fun addEnchantments(enchantments: Map<Enchantment, Int>) = apply { item.addEnchantments(enchantments) }
    fun addUnsafeEnchantment(enchantment: Enchantment, level: Int) = apply { item.addUnsafeEnchantment(enchantment, level) }
    fun removeEnchantment(enchantment: Enchantment) = apply { item.removeEnchantment(enchantment) }

    fun setGlowing(glow: Boolean) = apply {
        val nmsItem = CraftItemStack.asNMSCopy(item)
        val tag = if (nmsItem.hasTag()) nmsItem.tag else NBTTagCompound()
        if (glow) tag["ench"] = NBTTagList() else tag.remove("ench")
        nmsItem.tag = tag
        item = CraftItemStack.asBukkitCopy(nmsItem)
    }

    fun setNBTString(key: String, value: String) = apply {
        val nmsItem = CraftItemStack.asNMSCopy(item)
        if (!nmsItem.hasTag()) nmsItem.tag = NBTTagCompound()
        nmsItem.tag.setString(key, value)
        item = CraftItemStack.asBukkitCopy(nmsItem)
    }

    fun build(): ItemStack = item
}

fun ItemBuilder.dsl(block: ItemBuilder.() -> Unit): ItemStack {
    this.block()
    return this.build()
}

fun itemBuilder(material: Material, block: ItemBuilder.() -> Unit): ItemStack {
    val builder = ItemBuilder(material)
    builder.block()
    return builder.build()
}
