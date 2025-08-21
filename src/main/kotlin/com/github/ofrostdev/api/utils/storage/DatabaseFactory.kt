package com.github.ofrostdev.api.utils.storage

import com.henryfabio.sqlprovider.connector.SQLConnector
import com.henryfabio.sqlprovider.connector.type.impl.MySQLDatabaseType
import com.henryfabio.sqlprovider.connector.type.impl.SQLiteDatabaseType
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.plugin.Plugin
import java.io.File

class DatabaseFactory(private val plugin: Plugin) {
    private val dataFolder = File(plugin.dataFolder, "data")

    fun createConnector(section: ConfigurationSection): SQLConnector {
        val databaseType = section.getString("type")?.lowercase()
        val typeSection = section.getConfigurationSection(databaseType)

        return when (databaseType) {
            "sqlite" -> buildSQLITE(typeSection).connect()
            "mysql" -> buildMYSQL(typeSection).connect()
            else -> throw UnsupportedOperationException("database type unsupported!")
        }
    }

    private fun buildSQLITE(typeSection: ConfigurationSection): SQLiteDatabaseType {
        return SQLiteDatabaseType.builder()
            .file(File(dataFolder, typeSection.getString("fileName")))
            .build()
    }

    private fun buildMYSQL(typeSection: ConfigurationSection): MySQLDatabaseType {
        return MySQLDatabaseType.builder()
            .address(typeSection.getString("address"))
            .username(typeSection.getString("username"))
            .password(typeSection.getString("password"))
            .database(typeSection.getString("database"))
            .build()
    }

    companion object {
        fun getType(section: ConfigurationSection): String? {
            return section.getString("type")?.lowercase()
        }
    }
}
