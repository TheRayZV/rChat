package therayzv.rChat.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import therayzv.rChat.RChat;
import therayzv.rChat.utils.MessageUtils;

public class JoinLeaveListener implements Listener {
    
    private final RChat plugin;
    private final MessageUtils messageUtils;
    
    public JoinLeaveListener(RChat plugin) {
        this.plugin = plugin;
        this.messageUtils = new MessageUtils(plugin);
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Отменяем стандартное сообщение о входе сразу
        event.setJoinMessage(null);
        
        // Проверяем, включены ли кастомные сообщения о входе
        if (!plugin.getPluginConfig().getBoolean("system.join.enabled", true)) {
            return;
        }
        
        Player player = event.getPlayer();
        
        // Получаем формат сообщения
        String format = plugin.getPluginConfig().getString("system.join.format", 
                "&8[&a+&8] {prefix}&f{player} &7присоединился к серверу");
        
        // Получаем настраиваемое название мира
        String worldName = player.getWorld().getName();
        String displayWorldName = messageUtils.getWorldDisplayName(worldName);
        
        // Заменяем плейсхолдер имени игрока и мира
        String message = format.replace("{player}", player.getName())
                              .replace("{world}", displayWorldName);
        
        // Применяем плейсхолдеры LuckPerms
        message = messageUtils.replaceLuckPermsPlaceholders(message, player);
        
        // Отправляем сообщение о входе всем игрокам
        Bukkit.broadcastMessage(messageUtils.colorize(message));
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Отменяем стандартное сообщение о выходе сразу
        event.setQuitMessage(null);
        
        // Проверяем, включены ли кастомные сообщения о выходе
        if (!plugin.getPluginConfig().getBoolean("system.quit.enabled", true)) {
            return;
        }
        
        Player player = event.getPlayer();
        
        // Получаем формат сообщения
        String format = plugin.getPluginConfig().getString("system.quit.format", 
                "&8[&c-&8] {prefix}&f{player} &7покинул сервер");
        
        // Получаем настраиваемое название мира
        String worldName = player.getWorld().getName();
        String displayWorldName = messageUtils.getWorldDisplayName(worldName);
        
        // Заменяем плейсхолдер имени игрока и мира
        String message = format.replace("{player}", player.getName())
                              .replace("{world}", displayWorldName);
        
        // Применяем плейсхолдеры LuckPerms
        message = messageUtils.replaceLuckPermsPlaceholders(message, player);
        
        // Отправляем сообщение о выходе всем игрокам
        Bukkit.broadcastMessage(messageUtils.colorize(message));
    }
} 