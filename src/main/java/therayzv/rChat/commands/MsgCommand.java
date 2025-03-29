package therayzv.rChat.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import therayzv.rChat.RChat;
import therayzv.rChat.utils.MessageUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class MsgCommand implements CommandExecutor, TabCompleter {
    
    private final RChat plugin;
    private final MessageUtils messageUtils;
    
    // Карта для хранения последних отправителей личных сообщений для каждого игрока
    private static final Map<UUID, UUID> lastMessageSenders = new HashMap<>();
    
    public MsgCommand(RChat plugin) {
        this.plugin = plugin;
        this.messageUtils = new MessageUtils(plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Проверяем, включены ли личные сообщения
        if (!plugin.getPluginConfig().getBoolean("chat.private.enabled", true)) {
            sender.sendMessage(messageUtils.getColorizedMessage("messages.errors.pm_disabled"));
            return true;
        }
        
        // Проверяем, есть ли у отправителя права на использование команды
        if (!sender.hasPermission(plugin.getPluginConfig().getString("permissions.msg", "rchat.msg"))) {
            sender.sendMessage(messageUtils.getColorizedMessage("messages.errors.no_msg_permission"));
            return true;
        }
        
        // Проверяем, является ли это командой reply
        if (label.equalsIgnoreCase("r") || label.equalsIgnoreCase("reply")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(messageUtils.getColorizedMessage("messages.errors.console_reply"));
                return true;
            }
            
            Player player = (Player) sender;
            UUID lastSenderUUID = lastMessageSenders.get(player.getUniqueId());
            
            if (lastSenderUUID == null) {
                sender.sendMessage(messageUtils.getColorizedMessage("messages.errors.no_reply_target"));
                return true;
            }
            
            Player recipient = Bukkit.getPlayer(lastSenderUUID);
            if (recipient == null) {
                sender.sendMessage(messageUtils.getColorizedMessage("messages.errors.player_not_found", "player", "получатель"));
                return true;
            }
            
            // Проверяем наличие сообщения
            if (args.length < 1) {
                sender.sendMessage(messageUtils.getColorizedMessage("messages.errors.invalid_msg_usage"));
                return true;
            }
            
            // Собираем сообщение из аргументов
            String message = String.join(" ", args);
            
            // Отправляем сообщение
            sendPrivateMessage(sender, recipient, message);
            return true;
        }
        
        // Стандартная команда /msg или /pm
        // Проверяем наличие аргументов
        if (args.length < 2) {
            sender.sendMessage(messageUtils.getColorizedMessage("messages.errors.invalid_msg_usage"));
            return true;
        }
        
        // Получаем имя получателя
        String recipientName = args[0];
        
        // Проверяем, существует ли игрок с таким именем и онлайн ли он
        Player recipient = Bukkit.getPlayer(recipientName);
        if (recipient == null) {
            sender.sendMessage(messageUtils.getColorizedMessage("messages.errors.player_not_found", "player", recipientName));
            return true;
        }
        
        // Проверяем, не отправляет ли игрок сообщение самому себе
        if (sender instanceof Player && recipient.getUniqueId().equals(((Player) sender).getUniqueId())) {
            sender.sendMessage(messageUtils.getColorizedMessage("messages.errors.self_message"));
            return true;
        }
        
        // Собираем сообщение из оставшихся аргументов
        StringBuilder messageBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            messageBuilder.append(args[i]).append(" ");
        }
        String message = messageBuilder.toString().trim();
        
        // Отправляем сообщение
        sendPrivateMessage(sender, recipient, message);
        
        return true;
    }
    
    /**
     * Отправляет личное сообщение от отправителя получателю
     * @param sender Отправитель
     * @param recipient Получатель
     * @param message Сообщение
     */
    private void sendPrivateMessage(CommandSender sender, Player recipient, String message) {
        // Получаем имя отправителя
        String senderName = sender instanceof Player ? ((Player) sender).getName() : "Console";
        
        // Форматируем сообщение
        String format = plugin.getPluginConfig().getString("chat.private.format", "&7[&eЛС&7] &f{sender} &7→ &f{recipient}&7: &f{message}");
        
        String worldName = "";
        String displayWorldName = "";
        if (sender instanceof Player) {
            worldName = ((Player) sender).getWorld().getName();
            displayWorldName = messageUtils.getWorldDisplayName(worldName);
        }
        
        // Отправляем сообщение получателю
        String outgoing = format
                .replace("{sender}", senderName)
                .replace("{recipient}", recipient.getName())
                .replace("{message}", message)
                .replace("{world}", displayWorldName);
        
        // Отправляем сообщение отправителю
        sender.sendMessage(messageUtils.colorize(outgoing));
        
        // Отправляем подтверждение отправителю, если включено в настройках
        if (plugin.getPluginConfig().getBoolean("chat.private.show_success_message", true)) {
            String confirmFormat = plugin.getPluginConfig().getString("chat.private.confirm_format", "&7[&eЛС&7] &7Вы &7→ &f{recipient}&7: &f{message}");
            String confirmation = confirmFormat
                    .replace("{sender}", senderName)
                    .replace("{recipient}", recipient.getName())
                    .replace("{message}", message)
                    .replace("{world}", displayWorldName);
            
            sender.sendMessage(messageUtils.colorize(confirmation));
        }
        
        // Сохраняем последнего отправителя для команды reply
        if (sender instanceof Player) {
            lastMessageSenders.put(recipient.getUniqueId(), ((Player) sender).getUniqueId());
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        // Если команда отключена, возвращаем пустой список
        if (!plugin.getPluginConfig().getBoolean("chat.private.enabled", true)) {
            return completions;
        }
        
        // Если нет прав на использование команды, возвращаем пустой список
        if (!sender.hasPermission(plugin.getPluginConfig().getString("permissions.msg", "rchat.msg"))) {
            return completions;
        }
        
        // Если это reply, то предлагаем только текст сообщения
        if (alias.equalsIgnoreCase("r") || alias.equalsIgnoreCase("reply")) {
            return completions;
        }
        
        // Если это первый аргумент, предлагаем имена онлайн-игроков
        if (args.length == 1) {
            String input = args[0].toLowerCase();
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(input) && (!(sender instanceof Player) || !player.getUniqueId().equals(((Player) sender).getUniqueId()))) {
                    completions.add(player.getName());
                }
            }
        }
        
        return completions;
    }
    
    /**
     * Получить карту последних отправителей сообщений
     * @return Карта последних отправителей
     */
    public static Map<UUID, UUID> getLastMessageSenders() {
        return lastMessageSenders;
    }
} 