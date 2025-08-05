package com.github.ofrostdev.api.utils.database;

import com.henryfabio.sqlprovider.connector.SQLConnector;
import com.henryfabio.sqlprovider.connector.type.impl.MySQLDatabaseType;
import com.henryfabio.sqlprovider.connector.type.impl.SQLiteDatabaseType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;

import java.io.File;

public class DatabaseFactory {

    private final Plugin plugin;
    private final File dataFolder;

    public DatabaseFactory(Plugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "data");
    }

    public SQLConnector createConnector(ConfigurationSection section) {
        String databaseType = section.getString("type");
        ConfigurationSection typeSection = section.getConfigurationSection(databaseType);

        switch (databaseType) {
            case "sqlite":
                return buildSQLITE(typeSection).connect();
            case "mysql":
                return buildMYSQL(typeSection).connect();
            default:
                throw new UnsupportedOperationException("database type unsupported!");
        }
    }

    private SQLiteDatabaseType buildSQLITE(ConfigurationSection typeSection) {
        return SQLiteDatabaseType.builder()
                .file(new File(dataFolder, typeSection.getString("fileName")))
                .build();
    }

    private MySQLDatabaseType buildMYSQL(ConfigurationSection typeSection) {
        return MySQLDatabaseType.builder()
                .address(typeSection.getString("address"))
                .username(typeSection.getString("username"))
                .password(typeSection.getString("password"))
                .database(typeSection.getString("database"))
                .build();
    }

}
