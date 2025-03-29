package therayzv.rChat.utils;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import therayzv.rChat.RChat;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtils {
    
    private final RChat plugin;
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^}]+)\\}");
    
    public MessageUtils(RChat plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Получить сообщение из конфигурации
     * @param path Путь к сообщению в конфигурации
     * @return Сообщение
     */
    public String getMessage(String path) {
        String message = plugin.getPluginConfig().getString(path, "Message not found: " + path);
        // Логируем, если сообщение не найдено
        if (message.startsWith("Message not found:")) {
            plugin.getLogger().warning("Сообщение не найдено: " + path);
        }
        return message;
    }
    
    /**
     * Получить сообщение из конфигурации с цветовыми кодами
     * @param path Путь к сообщению в конфигурации
     * @return Сообщение с цветовыми кодами
     */
    public String getColorizedMessage(String path) {
        return colorize(getMessage(path));
    }
    
    /**
     * Получить сообщение с заменой плейсхолдеров
     * @param path Путь к сообщению в конфигурации
     * @param placeholders Карта плейсхолдеров и их значений
     * @return Сообщение с замененными плейсхолдерами
     */
    public String getMessage(String path, Map<String, String> placeholders) {
        String message = getMessage(path);
        
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        
        return message;
    }
    
    /**
     * Получить сообщение с цветовыми кодами и заменой плейсхолдеров
     * @param path Путь к сообщению в конфигурации
     * @param placeholders Карта плейсхолдеров и их значений
     * @return Сообщение с цветовыми кодами и замененными плейсхолдерами
     */
    public String getColorizedMessage(String path, Map<String, String> placeholders) {
        return colorize(getMessage(path, placeholders));
    }
    
    /**
     * Получить сообщение с цветовыми кодами и заменой одного плейсхолдера
     * @param path Путь к сообщению в конфигурации
     * @param placeholder Название плейсхолдера
     * @param value Значение для замены плейсхолдера
     * @return Сообщение с цветовыми кодами и замененным плейсхолдером
     */
    public String getColorizedMessage(String path, String placeholder, String value) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(placeholder, value);
        return getColorizedMessage(path, placeholders);
    }
    
    /**
     * Преобразовать цветовые коды в строке
     * @param text Текст для преобразования
     * @return Текст с преобразованными цветовыми кодами
     */
    public String colorize(String text) {
        if (text == null) return "";
        return text.replace("&", "§");
    }
    
    /**
     * Получить все плейсхолдеры в строке
     * @param text Текст для поиска плейсхолдеров
     * @return Массив имен плейсхолдеров
     */
    public String[] getPlaceholders(String text) {
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(text);
        
        // Подсчитываем количество совпадений
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        
        // Сбрасываем маркер
        matcher.reset();
        
        // Извлекаем плейсхолдеры
        String[] placeholders = new String[count];
        int index = 0;
        
        while (matcher.find()) {
            placeholders[index++] = matcher.group(1);
        }
        
        return placeholders;
    }
    
    /**
     * Заменяет плейсхолдеры LuckPerms в строке
     * @param message Исходное сообщение
     * @param player Игрок, для которого заменяются плейсхолдеры
     * @return Сообщение с замененными плейсхолдерами
     */
    public String replaceLuckPermsPlaceholders(String message, Player player) {
        if (message == null || player == null) return message;
        
        LuckPermsHook lpHook = plugin.getLuckPermsHook();
        if (lpHook == null || !lpHook.isInitialized()) return message;
        
        // Старый формат плейсхолдеров
        message = message.replace("{prefix}", lpHook.getPlayerPrefix(player));
        message = message.replace("{suffix}", lpHook.getPlayerSuffix(player));
        message = message.replace("{group}", lpHook.getPlayerGroup(player));
        
        // Новый формат плейсхолдеров (PlaceholderAPI-подобный)
        message = message.replace("%luckperms_prefix%", lpHook.getPlayerPrefix(player));
        message = message.replace("%luckperms_suffix%", lpHook.getPlayerSuffix(player));
        message = message.replace("%luckperms_group%", lpHook.getPlayerGroup(player));
        message = message.replace("%luckperms_primarygroup%", lpHook.getPlayerGroup(player));
        
        // Замена плейсхолдеров формата {lp_meta:ключ}
        Matcher matcher = Pattern.compile("\\{lp_meta:([^}]+)\\}").matcher(message);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String metaKey = matcher.group(1);
            String metaValue = lpHook.getPlayerMeta(player, metaKey);
            matcher.appendReplacement(sb, metaValue.replace("$", "\\$"));
        }
        
        matcher.appendTail(sb);
        
        // Замена плейсхолдеров формата %luckperms_meta_ключ%
        Matcher metaMatcher = Pattern.compile("%luckperms_meta_([^%]+)%").matcher(sb.toString());
        StringBuffer metaSb = new StringBuffer();
        
        while (metaMatcher.find()) {
            String metaKey = metaMatcher.group(1);
            String metaValue = lpHook.getPlayerMeta(player, metaKey);
            metaMatcher.appendReplacement(metaSb, metaValue.replace("$", "\\$"));
        }
        
        metaMatcher.appendTail(metaSb);
        
        return metaSb.toString();
    }
    
    /**
     * Получает настраиваемое название мира для отображения
     * @param worldName Реальное название мира
     * @return Настраиваемое название мира или оригинальное название, если настройка не задана
     */
    public String getWorldDisplayName(String worldName) {
        String displayName = plugin.getPluginConfig().getString("worlds." + worldName, worldName);
        return displayName;
    }
} 