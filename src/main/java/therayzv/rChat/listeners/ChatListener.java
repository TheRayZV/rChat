package therayzv.rChat.listeners;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import therayzv.rChat.RChat;
import therayzv.rChat.utils.MessageUtils;

public class ChatListener implements Listener {
    
    private final RChat plugin;
    private final MessageUtils messageUtils;
    
    public ChatListener(RChat plugin) {
        this.plugin = plugin;
        this.messageUtils = new MessageUtils(plugin);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        
        // Проверяем, имеет ли игрок право использовать цветовые коды
        boolean hasColorPermission = player.hasPermission(plugin.getPluginConfig().getString("permissions.color", "rchat.color"));
        if (!hasColorPermission) {
            // Если у игрока нет прав, удаляем цветовые коды из сообщения
            message = message.replaceAll("&[0-9a-fk-or]", "");
        }
        
        // Проверяем, начинается ли сообщение с префикса глобального чата
        String globalPrefix = plugin.getPluginConfig().getString("chat.global.prefix", "!");
        boolean isGlobalMessage = message.startsWith(globalPrefix);
        
        // Если это глобальное сообщение, удаляем префикс
        if (isGlobalMessage) {
            message = message.substring(globalPrefix.length());
        }
        
        // Форматируем сообщение в зависимости от типа
        String formattedMessage;
        if (isGlobalMessage) {
            // Проверяем право на использование глобального чата
            if (!player.hasPermission(plugin.getPluginConfig().getString("permissions.global", "rchat.global"))) {
                player.sendMessage(messageUtils.getColorizedMessage("messages.errors.no_global_permission"));
                event.setCancelled(true);
                return;
            }
            
            // Форматируем глобальное сообщение
            formattedMessage = formatGlobalMessage(player, message);
            
            // Отправляем сообщение всем игрокам
            for (Player recipient : Bukkit.getOnlinePlayers()) {
                recipient.sendMessage(Component.text(formattedMessage));
            }
        } else {
            // Проверяем право на использование локального чата
            if (!player.hasPermission(plugin.getPluginConfig().getString("permissions.local", "rchat.local"))) {
                player.sendMessage(messageUtils.getColorizedMessage("messages.errors.no_local_permission"));
                event.setCancelled(true);
                return;
            }
            
            // Форматируем локальное сообщение
            formattedMessage = formatLocalMessage(player, message);
            
            // Получаем радиус локального чата
            int radius = plugin.getPluginConfig().getInt("chat.local.radius", 100);
            
            // Отправляем сообщение только игрокам в радиусе
            boolean anyoneReceivedMessage = false;
            for (Player recipient : Bukkit.getOnlinePlayers()) {
                if (recipient.getWorld().equals(player.getWorld()) && 
                    recipient.getLocation().distance(player.getLocation()) <= radius) {
                    recipient.sendMessage(Component.text(formattedMessage));
                    if (!recipient.getUniqueId().equals(player.getUniqueId())) {
                        anyoneReceivedMessage = true;
                    }
                }
            }
            
            // Проверяем, видел ли кто-то сообщение (кроме отправителя)
            if (!anyoneReceivedMessage && plugin.getPluginConfig().getBoolean("chat.local.no_recipients.enabled", true)) {
                // Отправляем уведомление, что никто не видел сообщение
                player.sendMessage(messageUtils.getColorizedMessage("chat.local.no_recipients.message"));
            }
        }
        
        // Отменяем стандартное сообщение в чате
        event.setCancelled(true);
    }
    
    /**
     * Форматирует сообщение для глобального чата
     * 
     * @param player Игрок, отправивший сообщение
     * @param message Содержание сообщения
     * @return Отформатированное сообщение
     */
    private String formatGlobalMessage(Player player, String message) {
        String format = plugin.getPluginConfig().getString("chat.global.format", 
                "&7[&cГлобальный&7] &f{player}&7: &f{message}");
        
        // Если у игрока есть право на использование цветов, обрабатываем цветовые коды в сообщении
        if (player.hasPermission(plugin.getPluginConfig().getString("permissions.color", "rchat.color"))) {
            message = messageUtils.colorize(message);
        }
        
        // Заменяем плейсхолдеры LuckPerms
        format = messageUtils.replaceLuckPermsPlaceholders(format, player);
        
        // Получаем название мира
        String worldName = player.getWorld().getName();
        
        // Получаем настраиваемое название мира
        String displayWorldName = messageUtils.getWorldDisplayName(worldName);
        
        // Заменяем оставшиеся плейсхолдеры
        format = format.replace("{player}", player.getName())
                       .replace("{message}", message)
                       .replace("{world}", displayWorldName);
        
        return messageUtils.colorize(format);
    }
    
    /**
     * Форматирует сообщение для локального чата
     * 
     * @param player Игрок, отправивший сообщение
     * @param message Содержание сообщения
     * @return Отформатированное сообщение
     */
    private String formatLocalMessage(Player player, String message) {
        String format = plugin.getPluginConfig().getString("chat.local.format", 
                "&7[&aЛокальный&7] &f{player}&7: &f{message}");
        
        // Если у игрока есть право на использование цветов, обрабатываем цветовые коды в сообщении
        if (player.hasPermission(plugin.getPluginConfig().getString("permissions.color", "rchat.color"))) {
            message = messageUtils.colorize(message);
        }
        
        // Заменяем плейсхолдеры LuckPerms
        format = messageUtils.replaceLuckPermsPlaceholders(format, player);
        
        // Получаем название мира
        String worldName = player.getWorld().getName();
        
        // Получаем настраиваемое название мира
        String displayWorldName = messageUtils.getWorldDisplayName(worldName);
        
        // Заменяем оставшиеся плейсхолдеры
        format = format.replace("{player}", player.getName())
                       .replace("{message}", message)
                       .replace("{world}", displayWorldName);
        
        return messageUtils.colorize(format);
    }
} 