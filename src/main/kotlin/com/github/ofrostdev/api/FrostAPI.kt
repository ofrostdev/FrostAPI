package com.github.ofrostdev.api

import com.github.ofrostdev.api.utils.storage.DatabaseFactory
import org.bukkit.Bukkit

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

import com.henryfabio.sqlprovider.connector.SQLConnector as sqlCon

import java.lang.IllegalStateException

class FrostAPI : JavaPlugin() {

    class SQLConnector(
        private val plugin: JavaPlugin,
        private val config: FileConfiguration
    ) {
        private var sqlConnector: com.henryfabio.sqlprovider.connector.SQLConnector? = null

        fun build(section: String): Boolean {
            return try {
                val databaseFactory = DatabaseFactory(plugin)
                sqlConnector = databaseFactory.createConnector(
                    config.getConfigurationSection(section)
                        ?: throw IllegalStateException("database section is not defined!")
                )
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

        fun get(): sqlCon? {
            return sqlConnector
        }
    }

    class getManager(){
        fun hasPlugin(name: String): Boolean {
            if (name.isEmpty()) return false
            val found = Bukkit.getPluginManager().getPlugin(name)
            return found != null && found.isEnabled
        }
        fun hasPlugin(plugin: Plugin): Boolean {return hasPlugin(plugin.name)}

        fun hasPluginClass(clazz: String): Boolean {
            return try {
                Class.forName(clazz)
                true
            } catch (_: ClassNotFoundException) {
                false
            }
        }
    }

    override fun onEnable() {
        // Plugin startup logic
        UpdateChecker(this, "ofrostdev", "frostapi").check(file)
    }

    override fun onDisable() {
        // Plugin shutdown logic
    }

}
