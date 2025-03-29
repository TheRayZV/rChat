package therayzv.rChat;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import therayzv.rChat.commands.BroadcastCommand;
import therayzv.rChat.commands.MsgCommand;
import therayzv.rChat.commands.RChatCommand;
import therayzv.rChat.listeners.ChatListener;
import therayzv.rChat.listeners.DeathMessageListener;
import therayzv.rChat.listeners.JoinLeaveListener;
import therayzv.rChat.listeners.SystemMessageListener;
import therayzv.rChat.utils.ConfigManager;
import therayzv.rChat.utils.LuckPermsHook;

public final class RChat extends JavaPlugin {
    
    private ConfigManager configManager;
    private static RChat instance;
    private LuckPermsHook luckPermsHook;

    @Override
    public void onEnable() {
        // Устанавливаем экземпляр
        instance = this;
        
        // Инициализируем менеджер конфигурации
        configManager = new ConfigManager(this);
        configManager.saveDefaultConfig();
        
        // Проверяем наличие всех необходимых сообщений
        checkRequiredMessages();
        
        // Инициализируем хук LuckPerms
        luckPermsHook = new LuckPermsHook();
        luckPermsHook.init(this);
        
        // Регистрируем команды
        getCommand("rchat").setExecutor(new RChatCommand(this));
        MsgCommand msgCommand = new MsgCommand(this);
        getCommand("msg").setExecutor(msgCommand);
        getCommand("reply").setExecutor(msgCommand);
        getCommand("broadcast").setExecutor(new BroadcastCommand(this));
        
        // Регистрируем слушатели
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new SystemMessageListener(this), this);
        getServer().getPluginManager().registerEvents(new JoinLeaveListener(this), this);
        getServer().getPluginManager().registerEvents(new DeathMessageListener(this), this);
        
        getLogger().info("rChat plugin enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("rChat plugin disabled!");
    }
    
    /**
     * Получить экземпляр плагина
     * @return Экземпляр плагина
     */
    public static RChat getInstance() {
        return instance;
    }
    
    /**
     * Перезагрузить конфигурацию плагина
     */
    public void reloadPluginConfig() {
        configManager.reloadConfig();
        getLogger().info("Configuration reloaded!");
    }
    
    /**
     * Получить конфигурацию плагина
     * @return Конфигурация плагина
     */
    public FileConfiguration getPluginConfig() {
        return configManager.getConfig();
    }
    
    /**
     * Сохранить конфигурацию плагина
     */
    public void savePluginConfig() {
        configManager.saveConfig();
        getLogger().info("Configuration saved!");
    }
    
    /**
     * Получить хук LuckPerms
     * @return Хук LuckPerms
     */
    public LuckPermsHook getLuckPermsHook() {
        return luckPermsHook;
    }
    
    /**
     * Проверяет наличие всех необходимых сообщений в конфигурации
     * и добавляет отсутствующие сообщения
     */
    private void checkRequiredMessages() {
        boolean needSave = false;
        
        // Сообщения об ошибках
        String[] errorMessages = {
            "no_permission", "no_global_permission", "no_local_permission", 
            "no_msg_permission", "no_broadcast_permission", "no_reload_permission", 
            "no_set_permission", "no_toggle_permission", "unknown_command", 
            "unknown_subcommand", "invalid_option", "invalid_radius", 
            "invalid_chat_type", "invalid_format_usage", "invalid_set_usage", 
            "invalid_toggle_usage", "invalid_msg_usage", "invalid_broadcast_usage", 
            "invalid_rchat_usage", "player_not_found", "self_message", 
            "pm_disabled", "broadcast_disabled", "unknown_feature"
        };
        
        // Проверяем наличие каждого сообщения об ошибке
        for (String message : errorMessages) {
            String path = "messages.errors." + message;
            if (getPluginConfig().getString(path) == null) {
                // Если сообщение отсутствует, добавляем его
                getLogger().warning("Сообщение " + path + " отсутствует в конфигурации. Добавляем.");
                
                // Значение по умолчанию
                String defaultValue = "&cОшибка: " + message.replace("_", " ");
                
                // Специальные значения для некоторых сообщений
                if (message.equals("invalid_msg_usage")) {
                    defaultValue = "&cИспользование: /msg <игрок> <сообщение>";
                } else if (message.equals("invalid_broadcast_usage")) {
                    defaultValue = "&cИспользование: /broadcast <сообщение>";
                } else if (message.equals("no_broadcast_permission")) {
                    defaultValue = "&cУ вас нет прав для отправки оповещений на сервер!";
                }
                
                getPluginConfig().set(path, defaultValue);
                needSave = true;
            }
        }
        
        // Если были добавлены новые сообщения, сохраняем конфигурацию
        if (needSave) {
            savePluginConfig();
            getLogger().info("Конфигурация обновлена с новыми сообщениями.");
        }
    }
}
