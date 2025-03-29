package therayzv.rChat.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;

public class ConfigManager {
    
    private final JavaPlugin plugin;
    private FileConfiguration config;
    private File configFile;
    
    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        saveDefaultConfig();
    }
    
    /**
     * Перезагружает конфигурацию с диска
     */
    public void reloadConfig() {
        if (this.configFile == null) {
            this.configFile = new File(plugin.getDataFolder(), "config.yml");
        }
        
        this.config = YamlConfiguration.loadConfiguration(this.configFile);
        
        // Смысл этого кода в том, что он загружает конфигурацию из jar файла, если она не существует на диске
        InputStream defaultStream = plugin.getResource("config.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            this.config.setDefaults(defaultConfig);
        }
    }
    
    /**
     * Получает конфигурацию
     * @return Конфигурационный файл
     */
    public FileConfiguration getConfig() {
        if (this.config == null) {
            reloadConfig();
        }
        return this.config;
    }
    
    /**
     * Сохраняет конфигурацию на диск
     */
    public void saveConfig() {
        if (this.config == null || this.configFile == null) {
            return;
        }
        
        try {
            getConfig().save(this.configFile);
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config to " + this.configFile, ex);
        }
    }
    
    /**
     * Сохраняет конфигурацию по умолчанию, если она не существует
     */
    public void saveDefaultConfig() {
        if (this.configFile == null) {
            this.configFile = new File(plugin.getDataFolder(), "config.yml");
        }
        
        if (!this.configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
    }
} 