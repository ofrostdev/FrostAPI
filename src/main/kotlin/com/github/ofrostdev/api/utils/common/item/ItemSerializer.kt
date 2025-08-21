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
        val data = Base64.getDecoder().decode(base64)

        try {
            ByteArrayInputStream(data).use { bais ->
                BukkitObjectInputStream(bais).use { bois ->
                    return bois.readObject() as ItemStack
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } catch (e: ClassNotFoundException) {
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
        val data = Base64.getDecoder().decode(base64)

        try {
            ByteArrayInputStream(data).use { bais ->
                BukkitObjectInputStream(bais).use { bois ->
                    val size = bois.readInt()
                    val items = arrayOfNulls<ItemStack>(size)

                    for (i in 0 until size) {
                        items[i] = bois.readObject() as ItemStack
                    }
                    return items
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            return null
        }
    }
}