package com.github.ofrostdev.api.utils.common.item

import org.bukkit.inventory.ItemStack
import org.bukkit.util.io.BukkitObjectInputStream
import org.bukkit.util.io.BukkitObjectOutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

object ItemSerializer {

    fun encodeItem(item: ItemStack?): String? {
        try {
            ByteArrayOutputStream().use { baos ->
                BukkitObjectOutputStream(baos).use { boos ->
                    boos.writeObject(item)
                    boos.flush()
                    return Base64.getEncoder().encodeToString(baos.toByteArray())
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    fun decodeItem(base64: String?): ItemStack? {
        if (base64.isNullOrBlank()) return null
        val data: ByteArray = try {
            Base64.getDecoder().decode(base64)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            return null
        }

        try {
            ByteArrayInputStream(data).use { bais ->
                BukkitObjectInputStream(bais).use { bois ->
                    return bois.readObject() as? ItemStack
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun encodeItems(items: Array<ItemStack?>): String? {
        try {
            ByteArrayOutputStream().use { baos ->
                BukkitObjectOutputStream(baos).use { boos ->
                    boos.writeInt(items.size)
                    for (item in items) {
                        boos.writeObject(item)
                    }
                    boos.flush()
                    return Base64.getEncoder().encodeToString(baos.toByteArray())
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    fun decodeItems(base64: String?): Array<ItemStack?>? {
        if (base64.isNullOrBlank()) return null
        val data: ByteArray = try {
            Base64.getDecoder().decode(base64)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            return null
        }

        try {
            ByteArrayInputStream(data).use { bais ->
                BukkitObjectInputStream(bais).use { bois ->
                    val size = try {
                        bois.readInt()
                    } catch (e: IOException) {
                        e.printStackTrace()
                        return null
                    }

                    val items = arrayOfNulls<ItemStack>(size)
                    for (i in 0 until size) {
                        try {
                            items[i] = bois.readObject() as? ItemStack
                        } catch (_: Exception) {
                            items[i] = null
                        }
                    }
                    return items
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
