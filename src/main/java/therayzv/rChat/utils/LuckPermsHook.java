package therayzv.rChat.utils;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class LuckPermsHook {
    
    private LuckPerms luckPerms = null;
    private boolean initialized = false;
    
    /**
     * Инициализирует хук LuckPerms
     * @param plugin Экземпляр плагина
     * @return true, если инициализация прошла успешно
     */
    public boolean init(JavaPlugin plugin) {
        try {
            RegisteredServiceProvider<LuckPerms> provider = plugin.getServer().getServicesManager().getRegistration(LuckPerms.class);
            if (provider != null) {
                luckPerms = provider.getProvider();
                initialized = true;
                plugin.getLogger().info("Интеграция с LuckPerms успешно установлена!");
                return true;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Не удалось подключиться к LuckPerms: " + e.getMessage());
        }
        
        initialized = false;
        plugin.getLogger().warning("LuckPerms не найден. Плейсхолдеры LuckPerms не будут работать.");
        return false;
    }
    
    /**
     * Проверяет, инициализирован ли хук LuckPerms
     * @return true, если хук инициализирован
     */
    public boolean isInitialized() {
        return initialized && luckPerms != null;
    }
    
    /**
     * Получает префикс игрока из LuckPerms
     * @param player Игрок
     * @return Префикс игрока или пустая строка, если префикс не найден
     */
    public String getPlayerPrefix(Player player) {
        if (!isInitialized()) return "";
        
        try {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user == null) return "";
            
            String prefix = user.getCachedData().getMetaData().getPrefix();
            return prefix != null ? prefix : "";
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Получает суффикс игрока из LuckPerms
     * @param player Игрок
     * @return Суффикс игрока или пустая строка, если суффикс не найден
     */
    public String getPlayerSuffix(Player player) {
        if (!isInitialized()) return "";
        
        try {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user == null) return "";
            
            String suffix = user.getCachedData().getMetaData().getSuffix();
            return suffix != null ? suffix : "";
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Получает группу игрока из LuckPerms
     * @param player Игрок
     * @return Первичная группа игрока или пустая строка, если группа не найдена
     */
    public String getPlayerGroup(Player player) {
        if (!isInitialized()) return "";
        
        try {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user == null) return "";
            
            return user.getPrimaryGroup();
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Получает мета-значение игрока из LuckPerms
     * @param player Игрок
     * @param key Ключ мета-данных
     * @return Мета-значение или пустая строка, если значение не найдено
     */
    public String getPlayerMeta(Player player, String key) {
        if (!isInitialized()) return "";
        
        try {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user == null) return "";
            
            String meta = user.getCachedData().getMetaData().getMetaValue(key);
            return meta != null ? meta : "";
        } catch (Exception e) {
            return "";
        }
    }
} 