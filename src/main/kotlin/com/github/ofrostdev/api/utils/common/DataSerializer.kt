package com.github.ofrostdev.api.utils.common

import com.google.gson.Gson
import java.io.*
import java.util.Base64

object DataSerializer {

    val gson = Gson()

    fun encodeBase64(input: String): String =
        Base64.getEncoder().encodeToString(input.toByteArray())

    fun decodeBase64(base64: String): String =
        String(Base64.getDecoder().decode(base64))

    fun encodeObject(obj: Serializable): String {
        return try {
            val baos = ByteArrayOutputStream()
            ObjectOutputStream(baos).use { it.writeObject(obj) }
            Base64.getEncoder().encodeToString(baos.toByteArray())
        } catch (e: IOException) {
            e.printStackTrace()
            ""
        }
    }

    fun decodeObject(base64: String): Any? {
        return try {
            val data = Base64.getDecoder().decode(base64)
            ObjectInputStream(ByteArrayInputStream(data)).use { it.readObject() }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            null
        }
    }

    inline fun <reified T> decodeObjectAs(base64: String): T? {
        val obj = decodeObject(base64)
        return if (obj is T) obj else null
    }

    fun <T> toJson(obj: T): String = gson.toJson(obj)

    inline fun <reified T> fromJson(json: String): T = gson.fromJson(json, T::class.java)

    fun toBytes(obj: Serializable): ByteArray {
        return try {
            val baos = ByteArrayOutputStream()
            ObjectOutputStream(baos).use { it.writeObject(obj) }
            baos.toByteArray()
        } catch (e: IOException) {
            e.printStackTrace()
            ByteArray(0)
        }
    }

    fun fromBytes(bytes: ByteArray): Any? {
        return try {
            ObjectInputStream(ByteArrayInputStream(bytes)).use { it.readObject() }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
            null
        }
    }
}
