package com.github.ofrostdev.api.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class Config {

    private static Plugin plugin;

    private final String fileName;
    private File configFile;
    private FileConfiguration fileConfiguration;

    public static void init(Plugin plugin) {
        if (Config.plugin != null) return;
        Config.plugin = plugin;
    }

    public Config(String fileName) {
        if (plugin == null) {
            throw new IllegalArgumentException("[FrostAPI] Config -> Registre com.github.ofrostdev.api.FrostAPI.enable(Plugin plugin) na main!");
        }
        this.fileName = fileName;

        File folder = plugin.getDataFolder();
        if (!folder.exists() && !folder.mkdirs()) {
            plugin.getLogger().warning("Não foi possível criar a pasta do plugin.");
        }

        this.configFile = new File(folder, fileName);
        saveDefaultConfig();

        this.fileConfiguration = YamlConfiguration.loadConfiguration(this.configFile);
    }

    public void saveDefaultConfig() {
        if (!this.configFile.exists())
            plugin.saveResource(this.fileName, false);
    }

    public void reloadConfig() {
        this.fileConfiguration = YamlConfiguration.loadConfiguration(this.configFile);
        InputStream defConfigStream = plugin.getResource(this.fileName);
        if (defConfigStream != null) {
        }
    }

    public void saveConfig() {
        if (fileConfiguration == null || configFile == null) return;

        try {
            fileConfiguration.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Não foi possível salvar o arquivo de configuração " + fileName);
            e.printStackTrace();
        }
    }

    public FileConfiguration getConfig() {
        if (this.fileConfiguration == null) reloadConfig();
        return this.fileConfiguration;
    }

    public String getString(String path) {
        return getConfig().getString(path, "String não encontrada: " + path).replace("&", "§");
    }

    public String getString(String path, Object... placeholders) {
        String string = getConfig().getString(path);
        if (string == null) return "§cString não configurada: " + path;

        if (placeholders.length % 2 != 0) return string;

        for (int i = 0; i < placeholders.length; i += 2) {
            String key = String.valueOf(placeholders[i]);
            String value = String.valueOf(placeholders[i + 1]);
            string = string.replace("{" + key + "}", value);
        }
        return string.replace("&", "§");
    }

    public int getInt(String path) {
        return getConfig().getInt(path, -1);
    }

    public int getInt(String path, Object... placeholders) {
        String result = getConfig().getString(path);
        if (result == null) return -1;

        if (placeholders.length % 2 != 0) return Integer.parseInt(result);

        for (int i = 0; i < placeholders.length; i += 2) {
            String key = String.valueOf(placeholders[i]);
            String value = String.valueOf(placeholders[i + 1]);
            result = result.replace("{" + key + "}", value);
        }

        try {
            return Integer.parseInt(result);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public double getDouble(String path) {
        return getConfig().getDouble(path, -1.0);
    }

    public double getDouble(String path, Object... placeholders) {
        String raw = getConfig().getString(path);
        if (raw == null) return -1.0;

        if (placeholders.length % 2 != 0) return Double.parseDouble(raw);

        for (int i = 0; i < placeholders.length; i += 2) {
            String key = String.valueOf(placeholders[i]);
            String value = String.valueOf(placeholders[i + 1]);
            raw = raw.replace("{" + key + "}", value);
        }

        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            return -1.0;
        }
    }

    public List<String> getStringList(String path) {
        List<String> list = getConfig().getStringList(path);
        if (list == null) return java.util.Collections.emptyList();

        List<String> result = new java.util.ArrayList<>();
        for (String s : list) {
            result.add(s.replace("&", "§"));
        }
        return result;
    }

}
