package com.github.ofrostdev.api.utils;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

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
        if (plugin == null)
            throw new IllegalArgumentException("[FrostAPI] Config -> Registre o plugin com Config.init(plugin)!");

        this.fileName = fileName;
        this.configFile = new File(plugin.getDataFolder(), fileName);

        saveDefaultConfig();

        this.fileConfiguration = YamlConfiguration.loadConfiguration(this.configFile);

        reloadDefaults();
    }

    private void reloadDefaults() {
        InputStream defConfigStream = plugin.getResource(fileName);
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            fileConfiguration.setDefaults(defConfig);
        }
    }

    public void saveDefaultConfig() {
        try {
            File folder = plugin.getDataFolder();
            if (!folder.exists() && !folder.mkdirs()) {
                plugin.getLogger().warning("Não foi possível criar a pasta do plugin.");
            }

            if (!this.configFile.exists()) {
                File parent = configFile.getParentFile();
                if (!parent.exists() && !parent.mkdirs()) {
                    plugin.getLogger().severe("Não foi possível criar o diretório para o arquivo: " + fileName);
                }

                if (plugin.getResource(fileName) != null) {
                    plugin.saveResource(fileName, false);
                }

                if (!this.configFile.exists()) {
                    if (!this.configFile.createNewFile()) {
                        plugin.getLogger().warning("Falha ao criar o arquivo: " + fileName);
                    }
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Erro ao criar o arquivo de configuração " + fileName);
            e.printStackTrace();
        }
    }

    public void reloadConfig() {
        this.fileConfiguration = YamlConfiguration.loadConfiguration(this.configFile);
        InputStream defConfigStream = plugin.getResource(this.fileName);
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            fileConfiguration.setDefaults(defConfig);
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
            string = string.replace(key, value);
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
            result = result.replace(key, value);
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
            raw = raw.replace(key, value);
        }

        try {
            return Double.parseDouble(raw);
        } catch (NumberFormatException e) {
            return -1.0;
        }
    }

    public boolean getBoolean(String path) {
        return getConfig().getBoolean(path, false);
    }

    public List<String> getStringList(String path) {
        List<String> list = getConfig().getStringList(path);
        if (list == null) return Collections.emptyList();

        List<String> result = new ArrayList<>();
        for (String s : list) {
            result.add(s.replace("&", "§"));
        }
        return result;
    }

    public List<Integer> getIntegerList(String path) {
        return getConfig().getIntegerList(path);
    }

    public List<Double> getDoubleList(String path) {
        return getConfig().getDoubleList(path);
    }

    public List<Boolean> getBooleanList(String path) {
        return getConfig().getBooleanList(path);
    }

    public long getLong(String path) {
        return getConfig().getLong(path, -1L);
    }

    public List<Long> getLongList(String path) {
        return getConfig().getLongList(path);
    }

    public List<Map<?, ?>> getMapList(String path) {
        return getConfig().getMapList(path);
    }

    public ConfigurationSection getConfigurationSection(String path) {
        return getConfig().getConfigurationSection(path);
    }

    public Set<String> getKeys(boolean deep) {
        return getConfig().getKeys(deep);
    }

    public boolean contains(String path) {
        return getConfig().contains(path);
    }

    public Object get(String path) {
        return getConfig().get(path);
    }

    public Object getOrDefault(String path, Object def) {
        return getConfig().get(path, def);
    }

    public boolean isSet(String path) {
        return getConfig().isSet(path);
    }

    public boolean isString(String path) {
        return getConfig().isString(path);
    }

    public boolean isInt(String path) {
        return getConfig().isInt(path);
    }

    public boolean isDouble(String path) {
        return getConfig().isDouble(path);
    }

    public boolean isBoolean(String path) {
        return getConfig().isBoolean(path);
    }

    public boolean isLong(String path) {
        return getConfig().isLong(path);
    }

    public boolean isList(String path) {
        return getConfig().isList(path);
    }

    public boolean isConfigurationSection(String path) {
        return getConfig().isConfigurationSection(path);
    }

    public void set(String path, String value) {
        getConfig().set(path, value);
    }
}
