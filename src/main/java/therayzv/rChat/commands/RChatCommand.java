package therayzv.rChat.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import therayzv.rChat.RChat;
import therayzv.rChat.utils.MessageUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RChatCommand implements CommandExecutor, TabCompleter {
    
    private final RChat plugin;
    private final MessageUtils messageUtils;
    private final List<String> subCommands = Arrays.asList("reload", "help", "set", "toggle");
    private final List<String> setOptions = Arrays.asList("prefix", "radius", "format", "join", "quit", "achievements");
    private final List<String> toggleOptions = Arrays.asList("join", "quit", "achievements", "private", "broadcast", "no_recipients");
    private final List<String> formatOptions = Arrays.asList("global", "local");
    
    public RChatCommand(RChat plugin) {
        this.plugin = plugin;
        this.messageUtils = new MessageUtils(plugin);
        plugin.getCommand("rchat").setTabCompleter(this);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelpMessage(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "reload":
                if (!sender.hasPermission("rchat.reload")) {
                    sender.sendMessage(messageUtils.getColorizedMessage("messages.errors.no_reload_permission"));
                    return true;
                }
                
                plugin.reloadPluginConfig();
                sender.sendMessage(messageUtils.getColorizedMessage("messages.success.reload"));
                return true;
                
            case "help":
                sendHelpMessage(sender);
                return true;
                
            case "set":
                if (args.length < 3) {
                    sender.sendMessage(messageUtils.getColorizedMessage("messages.errors.invalid_set_usage"));
                    return true;
                }
                
                if (!sender.hasPermission("rchat.set")) {
                    sender.sendMessage(messageUtils.getColorizedMessage("messages.errors.no_set_permission"));
                    return true;
                }
                
                String option = args[1].toLowerCase();
                
                switch (option) {
                    case "prefix":
                        String prefix = args[2];
                        plugin.getPluginConfig().set("chat.global.prefix", prefix);
                        sender.sendMessage(messageUtils.getColorizedMessage("messages.success.prefix_set", "value", prefix));
                        break;
                        
                    case "radius":
                        try {
                            int radius = Integer.parseInt(args[2]);
                            plugin.getPluginConfig().set("chat.local.radius", radius);
                            sender.sendMessage(messageUtils.getColorizedMessage("messages.success.radius_set", "value", String.valueOf(radius)));
                        } catch (NumberFormatException e) {
                            sender.sendMessage(messageUtils.getColorizedMessage("messages.errors.invalid_radius"));
                            return true;
                        }
                        break;
                        
                    case "format":
                        if (args.length < 4) {
                            sender.sendMessage(messageUtils.getColorizedMessage("messages.errors.invalid_format_usage"));
                            return true;
                        }
                        
                        String chatType = args[2].toLowerCase();
                        String format = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                        
                        if (chatType.equals("global")) {
                            plugin.getPluginConfig().set("chat.global.format", format);
                            sender.sendMessage(messageUtils.getColorizedMessage("messages.success.global_format_set", "value", format));
                        } else if (chatType.equals("local")) {
                            plugin.getPluginConfig().set("chat.local.format", format);
                            sender.sendMessage(messageUtils.getColorizedMessage("messages.success.local_format_set", "value", format));
                        } else {
                            sender.sendMessage(messageUtils.getColorizedMessage("messages.errors.invalid_chat_type"));
                            return true;
                        }
                        break;
                        
                    case "join":
                        if (args.length < 3) {
                            sender.sendMessage(messageUtils.getColorizedMessage("messages.errors.invalid_set_usage"));
                            return true;
                        }
                        String joinFormat = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                        plugin.getPluginConfig().set("system.join.format", joinFormat);
                        sender.sendMessage(messageUtils.getColorizedMessage("messages.success.join_format_set", "value", joinFormat));
                        break;
                        
                    case "quit":
                        if (args.length < 3) {
                            sender.sendMessage(messageUtils.getColorizedMessage("messages.errors.invalid_set_usage"));
                            return true;
                        }
                        String quitFormat = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                        plugin.getPluginConfig().set("system.quit.format", quitFormat);
                        sender.sendMessage(messageUtils.getColorizedMessage("messages.success.quit_format_set", "value", quitFormat));
                        break;
                        
                    case "achievements":
                        if (args.length < 3) {
                            sender.sendMessage(messageUtils.getColorizedMessage("messages.errors.invalid_set_usage"));
                            return true;
                        }
                        String achievementsFormat = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
                        plugin.getPluginConfig().set("system.achievements.format", achievementsFormat);
                        sender.sendMessage(messageUtils.getColorizedMessage("messages.success.achievements_format_set", "value", achievementsFormat));
                        break;
                        
                    default:
                        sender.sendMessage(messageUtils.getColorizedMessage("messages.errors.invalid_option"));
                        return true;
                }
                
                // Сохраняем конфигурацию
                plugin.savePluginConfig();
                return true;
                
            case "toggle":
                if (args.length < 2) {
                    sender.sendMessage(messageUtils.getColorizedMessage("messages.errors.invalid_toggle_usage"));
                    return true;
                }
                
                if (!sender.hasPermission("rchat.toggle")) {
                    sender.sendMessage(messageUtils.getColorizedMessage("messages.errors.no_toggle_permission"));
                    return true;
                }
                
                String feature = args[1].toLowerCase();
                
                switch (feature) {
                    case "join":
                        boolean joinEnabled = !plugin.getPluginConfig().getBoolean("system.join.enabled", true);
                        plugin.getPluginConfig().set("system.join.enabled", joinEnabled);
                        String joinStatus = joinEnabled ? "feature_enabled" : "feature_disabled";
                        sender.sendMessage(messageUtils.getColorizedMessage("messages.success." + joinStatus, "feature", "сообщения о входе"));
                        break;
                        
                    case "quit":
                        boolean quitEnabled = !plugin.getPluginConfig().getBoolean("system.quit.enabled", true);
                        plugin.getPluginConfig().set("system.quit.enabled", quitEnabled);
                        String quitStatus = quitEnabled ? "feature_enabled" : "feature_disabled";
                        sender.sendMessage(messageUtils.getColorizedMessage("messages.success." + quitStatus, "feature", "сообщения о выходе"));
                        break;
                        
                    case "achievements":
                        boolean achievementsEnabled = !plugin.getPluginConfig().getBoolean("system.achievements.enabled", true);
                        plugin.getPluginConfig().set("system.achievements.enabled", achievementsEnabled);
                        String achievementsStatus = achievementsEnabled ? "feature_enabled" : "feature_disabled";
                        sender.sendMessage(messageUtils.getColorizedMessage("messages.success." + achievementsStatus, "feature", "сообщения о достижениях"));
                        break;
                    
                    case "private":
                        boolean privateEnabled = !plugin.getPluginConfig().getBoolean("chat.private.enabled", true);
                        plugin.getPluginConfig().set("chat.private.enabled", privateEnabled);
                        String privateStatus = privateEnabled ? "feature_enabled" : "feature_disabled";
                        sender.sendMessage(messageUtils.getColorizedMessage("messages.success." + privateStatus, "feature", "личные сообщения"));
                        break;
                        
                    case "broadcast":
                        boolean broadcastEnabled = !plugin.getPluginConfig().getBoolean("chat.broadcast.enabled", true);
                        plugin.getPluginConfig().set("chat.broadcast.enabled", broadcastEnabled);
                        String broadcastStatus = broadcastEnabled ? "feature_enabled" : "feature_disabled";
                        sender.sendMessage(messageUtils.getColorizedMessage("messages.success." + broadcastStatus, "feature", "оповещения"));
                        break;
                        
                    case "no_recipients":
                        boolean noRecipientsEnabled = !plugin.getPluginConfig().getBoolean("chat.local.no_recipients.enabled", true);
                        plugin.getPluginConfig().set("chat.local.no_recipients.enabled", noRecipientsEnabled);
                        String noRecipientsStatus = noRecipientsEnabled ? "feature_enabled" : "feature_disabled";
                        sender.sendMessage(messageUtils.getColorizedMessage("messages.success." + noRecipientsStatus, "feature", "уведомления о пустом чате"));
                        break;
                        
                    default:
                        sender.sendMessage(messageUtils.getColorizedMessage("messages.errors.unknown_feature"));
                        return true;
                }
                
                // Сохраняем конфигурацию
                plugin.savePluginConfig();
                return true;
                
            default:
                sender.sendMessage(messageUtils.getColorizedMessage("messages.errors.unknown_subcommand"));
                return true;
        }
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        if (args.length == 1) {
            return filterCompletions(subCommands, args[0]);
        } else if (args.length == 2) {
            if (args[0].equalsIgnoreCase("set")) {
                return filterCompletions(setOptions, args[1]);
            } else if (args[0].equalsIgnoreCase("toggle")) {
                return filterCompletions(toggleOptions, args[1]);
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("set") && args[1].equalsIgnoreCase("format")) {
            return filterCompletions(formatOptions, args[2]);
        }
        
        return completions;
    }
    
    /**
     * Отфильтровать варианты автозаполнения на основе ввода пользователя
     * @param options Доступные варианты
     * @param input Ввод пользователя
     * @return Отфильтрованные варианты
     */
    private List<String> filterCompletions(List<String> options, String input) {
        return options.stream()
                .filter(option -> option.toLowerCase().startsWith(input.toLowerCase()))
                .collect(Collectors.toList());
    }
    
    /**
     * Отправить сообщение с помощью игроку
     * @param sender Отправитель команды
     */
    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage(messageUtils.getColorizedMessage("messages.help.header"));
        sender.sendMessage(messageUtils.getColorizedMessage("messages.help.help_cmd"));
        sender.sendMessage(messageUtils.getColorizedMessage("messages.help.reload_cmd"));
        sender.sendMessage(messageUtils.getColorizedMessage("messages.help.prefix_cmd"));
        sender.sendMessage(messageUtils.getColorizedMessage("messages.help.radius_cmd"));
        sender.sendMessage(messageUtils.getColorizedMessage("messages.help.format_cmd"));
        sender.sendMessage(messageUtils.getColorizedMessage("messages.help.join_format_cmd"));
        sender.sendMessage(messageUtils.getColorizedMessage("messages.help.quit_format_cmd"));
        sender.sendMessage(messageUtils.getColorizedMessage("messages.help.achievements_format_cmd"));
        sender.sendMessage(messageUtils.getColorizedMessage("messages.help.toggle_cmd"));
        sender.sendMessage(messageUtils.getColorizedMessage("messages.help.msg_cmd"));
        sender.sendMessage(messageUtils.getColorizedMessage("messages.help.broadcast_cmd"));
        sender.sendMessage(messageUtils.getColorizedMessage("messages.help.footer"));
        
        // Основные плейсхолдеры
        sender.sendMessage(messageUtils.getColorizedMessage("messages.help.placeholders_header"));
        sender.sendMessage(messageUtils.getColorizedMessage("messages.help.player_placeholder"));
        sender.sendMessage(messageUtils.getColorizedMessage("messages.help.sender_placeholder"));
        sender.sendMessage(messageUtils.getColorizedMessage("messages.help.recipient_placeholder"));
        sender.sendMessage(messageUtils.getColorizedMessage("messages.help.message_placeholder"));
        sender.sendMessage(messageUtils.getColorizedMessage("messages.help.world_placeholder"));
        sender.sendMessage(messageUtils.getColorizedMessage("messages.help.achievement_placeholder"));
        sender.sendMessage(messageUtils.getColorizedMessage("messages.help.description_placeholder"));
        
        // LuckPerms плейсхолдеры
        if (plugin.getLuckPermsHook() != null && plugin.getLuckPermsHook().isInitialized()) {
            sender.sendMessage(messageUtils.getColorizedMessage("messages.help.luckperms_header"));
            sender.sendMessage(messageUtils.getColorizedMessage("messages.help.prefix_placeholder"));
            sender.sendMessage(messageUtils.getColorizedMessage("messages.help.suffix_placeholder"));
            sender.sendMessage(messageUtils.getColorizedMessage("messages.help.group_placeholder"));
            sender.sendMessage(messageUtils.getColorizedMessage("messages.help.meta_placeholder"));
        }
    }
} 