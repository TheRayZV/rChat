package therayzv.rChat.commands;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import therayzv.rChat.RChat;
import therayzv.rChat.utils.MessageUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BroadcastCommand implements CommandExecutor {
    
    private final RChat plugin;
    private final MessageUtils messageUtils;
    
    public BroadcastCommand(RChat plugin) {
        this.plugin = plugin;
        this.messageUtils = new MessageUtils(plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Проверяем, включены ли оповещения
        if (!plugin.getPluginConfig().getBoolean("chat.broadcast.enabled", true)) {
            sender.sendMessage(messageUtils.getColorizedMessage("messages.errors.broadcast_disabled"));
            return true;
        }
        
        // Проверяем, есть ли у отправителя права на использование команды
        if (!sender.hasPermission(plugin.getPluginConfig().getString("permissions.broadcast", "rchat.broadcast"))) {
            sender.sendMessage(messageUtils.getColorizedMessage("messages.errors.no_broadcast_permission"));
            return true;
        }
        
        // Проверяем наличие аргументов
        if (args.length < 1) {
            sender.sendMessage(messageUtils.getColorizedMessage("messages.errors.invalid_broadcast_usage"));
            return true;
        }
        
        // Собираем сообщение из аргументов
        String message = String.join(" ", args);
        
        // Получаем имя отправителя
        String senderName = sender instanceof Player ? ((Player) sender).getName() : "Console";
        
        // Получаем форматы строк оповещения
        List<String> formatLines = plugin.getPluginConfig().getStringList("chat.broadcast.format-lines");
        if (formatLines.isEmpty()) {
            // Если не заданы строки формата, используем одиночную строку по умолчанию
            formatLines = new ArrayList<>();
            formatLines.add("&8[&c&lОБЪЯВЛЕНИЕ&8]");
            formatLines.add("&f{message}");
        }
        
        // Заменяем плейсхолдеры
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("sender", senderName);
        placeholders.put("message", message);
        
        // Отправляем каждую строку формата
        for (String line : formatLines) {
            String formattedLine = line;
            
            // Применяем плейсхолдеры LuckPerms для отправителя, если это игрок
            if (sender instanceof Player) {
                formattedLine = messageUtils.replaceLuckPermsPlaceholders(formattedLine, (Player) sender);
            }
            
            // Заменяем плейсхолдеры
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                formattedLine = formattedLine.replace("{" + entry.getKey() + "}", entry.getValue());
            }
            
            // Отправляем сообщение всем игрокам
            Bukkit.getServer().broadcastMessage(messageUtils.colorize(formattedLine));
        }
        
        // Проверяем, включено ли воспроизведение звука
        if (plugin.getPluginConfig().getBoolean("chat.broadcast.sound.enabled", false)) {
            try {
                String soundName = plugin.getPluginConfig().getString("chat.broadcast.sound.sound", "ENTITY_EXPERIENCE_ORB_PICKUP");
                float volume = (float) plugin.getPluginConfig().getDouble("chat.broadcast.sound.volume", 1.0);
                float pitch = (float) plugin.getPluginConfig().getDouble("chat.broadcast.sound.pitch", 1.0);
                
                Sound sound = Sound.valueOf(soundName);
                
                // Воспроизводим звук для всех игроков
                for (Player player : Bukkit.getOnlinePlayers()) {
                    player.playSound(player.getLocation(), sound, volume, pitch);
                }
            } catch (IllegalArgumentException e) {
                // Игнорируем ошибку, если звук не найден
                plugin.getLogger().warning("Неверное имя звука для оповещения: " + plugin.getPluginConfig().getString("chat.broadcast.sound.sound"));
            }
        }
        
        // Отправляем сообщение об успешной отправке оповещения
        sender.sendMessage(messageUtils.getColorizedMessage("messages.success.broadcast_sent"));
        
        return true;
    }
} 