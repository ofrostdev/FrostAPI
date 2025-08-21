package com.github.ofrostdev.api.utils.common.fileconfiguration

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.IOException
import java.io.InputStreamReader

class Config(private val plugin: JavaPlugin, private val fileName: String) {
    private val file: File = File(plugin.dataFolder, fileName)
    private var fileConfiguration: FileConfiguration = YamlConfiguration()

    init {
        saveDefaultConfig()
        reloadConfig()
    }

    private fun reloadDefaults() {
        plugin.getResource(fileName)?.let { stream ->
            val defaultConfig = YamlConfiguration.loadConfiguration(InputStreamReader(stream))
            (fileConfiguration as YamlConfiguration).setDefaults(defaultConfig)
        }
    }

    fun saveDefaultConfig() {
        if (!plugin.dataFolder.exists() && !plugin.dataFolder.mkdirs())
            plugin.logger.warning("Could not create plugin folder!")

        if (!file.exists()) {
            plugin.getResource(fileName)?.let { plugin.saveResource(fileName, false) }
            if (!file.exists() && !file.createNewFile())
                plugin.logger.warning("Failed to create file: $fileName")
        }
    }

    fun reloadConfig() {
        fileConfiguration = YamlConfiguration.loadConfiguration(file)
        reloadDefaults()
    }

    fun saveConfig() {
        try {
            fileConfiguration.save(file)
        } catch (e: IOException) {
            plugin.logger.severe("Could not save configuration file: $fileName")
            e.printStackTrace()
        }
    }

    val config: FileConfiguration
        get() = fileConfiguration

    fun dsl(block: ConfigDSL.() -> Unit) = ConfigDSL(this).apply(block)

    class ConfigDSL(private val config: Config) {
        infix fun String.to(value: Any?) {
            when (value) {
                is Map<*, *> -> value.forEach { (k, v) -> sectionOrCreate(this).set(k.toString(), v) }
                is List<*> -> config.setList(this, value.map { it.toString() })
                is ConfigSectionDSL -> value.applyTo(sectionOrCreate(this))
                else -> config.set(this, value)
            }
        }

        fun section(name: String, block: ConfigSectionDSL.() -> Unit) {
            val section = sectionOrCreate(name)
            ConfigSectionDSL(section).block()
        }

        private fun sectionOrCreate(name: String): ConfigurationSection {
            return config.config.getConfigurationSection(name) ?: config.config.createSection(name)
        }
    }

    class ConfigSectionDSL(private val section: ConfigurationSection) {
        infix fun String.to(value: Any?) {
            when (value) {
                is Map<*, *> -> value.forEach { (k, v) -> section.createSection(k.toString()).also { it[k.toString()] = v } }
                is List<*> -> section[this] = value
                is ConfigSectionDSL -> value.applyTo(section.createSection(this))
                else -> section[this] = value
            }
        }

        fun section(name: String, block: ConfigSectionDSL.() -> Unit) {
            val sub = section.getConfigurationSection(name) ?: section.createSection(name)
            ConfigSectionDSL(sub).block()
        }

        fun applyTo(target: ConfigurationSection) {
            section.getKeys(false).forEach { key ->
                target[key] = section[key]
            }
        }
    }

    fun getString(path: String): String = config.getString(path, "string not found: $path").replace("&", "§")
    fun getString(path: String, def: String): String = config.getString(path, def)
    fun getString(path: String, vararg placeholders: Any): String? {
        var string: String? = config.getString(path) ?: return "string not found: $path"
        if (placeholders.size % 2 != 0) return string
        var i = 0
        while (i < placeholders.size) {
            val key = placeholders[i].toString()
            val value = placeholders[i + 1].toString()
            string = string!!.replace(key, value)
            i += 2
        }
        return string!!.replace("&", "§")
    }

    fun getInt(path: String?) = config.getInt(path, -1)
    fun getInt(path: String?, def: Int) = config.getInt(path, def)
    fun getInt(path: String?, vararg placeholders: Any): Int {
        var result: String? = config.getString(path) ?: return -1
        if (placeholders.size % 2 != 0) if (result != null) return result.toInt()
        var i = 0
        while (i < placeholders.size) {
            val key = placeholders[i].toString()
            val value = placeholders[i + 1].toString()
            result = result!!.replace(key, value)
            i += 2
        }
        return try { result!!.toInt() } catch (e: NumberFormatException) { -1 }
    }

    fun getDouble(path: String?) = config.getDouble(path, -1.0)
    fun getDouble(path: String?, def: Double) = config.getDouble(path, def)
    fun getDouble(path: String?, vararg placeholders: Any): Double {
        var raw: String? = config.getString(path) ?: return -1.0
        if (placeholders.size % 2 != 0) if (raw != null) return raw.toDouble()
        var i = 0
        while (i < placeholders.size) {
            val key = placeholders[i].toString()
            val value = placeholders[i + 1].toString()
            raw = raw!!.replace(key, value)
            i += 2
        }
        return try { raw!!.toDouble() } catch (e: NumberFormatException) { -1.0 }
    }

    fun getBoolean(path: String?) = config.getBoolean(path, false)
    fun getBoolean(path: String?, def: Boolean) = config.getBoolean(path, def)
    fun getStringList(path: String?): List<String> = config.getStringList(path).map { it.replace("&", "§") }
    fun getIntegerList(path: String?) = config.getIntegerList(path)
    fun getDoubleList(path: String?) = config.getDoubleList(path)
    fun getBooleanList(path: String?) = config.getBooleanList(path)
    fun getLong(path: String?) = config.getLong(path, -1L)
    fun getLongList(path: String?) = config.getLongList(path)
    fun getMapList(path: String?) = config.getMapList(path)
    fun getConfigurationSection(path: String?) = config.getConfigurationSection(path)
    fun getKeys(deep: Boolean) = config.getKeys(deep)
    fun contains(path: String?) = config.contains(path)
    fun get(path: String?) = config[path]
    fun getOrDefault(path: String?, def: Any?) = config[path, def]
    fun isSet(path: String?) = config.isSet(path)
    fun isString(path: String?) = config.isString(path)
    fun isInt(path: String?) = config.isInt(path)
    fun isDouble(path: String?) = config.isDouble(path)
    fun isBoolean(path: String?) = config.isBoolean(path)
    fun isLong(path: String?) = config.isLong(path)
    fun isList(path: String?) = config.isList(path)
    fun isConfigurationSection(path: String?) = config.isConfigurationSection(path)
    fun set(path: String?, value: Any?) { config[path] = value }
    fun setList(path: String?, list: List<String?>?) { config[path] = list ?: null }
}
