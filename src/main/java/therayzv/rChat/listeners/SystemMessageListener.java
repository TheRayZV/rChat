package therayzv.rChat.listeners;

import io.papermc.paper.advancement.AdvancementDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import therayzv.rChat.RChat;
import therayzv.rChat.utils.MessageUtils;

import java.util.HashMap;
import java.util.Map;

public class SystemMessageListener implements Listener {
    
    private final RChat plugin;
    private final MessageUtils messageUtils;
    
    public SystemMessageListener(RChat plugin) {
        this.plugin = plugin;
        this.messageUtils = new MessageUtils(plugin);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerAdvancement(PlayerAdvancementDoneEvent event) {
        // Проверяем, включены ли кастомные сообщения о достижениях
        if (!plugin.getPluginConfig().getBoolean("system.achievements.enabled", true)) {
            return;
        }
        
        Player player = event.getPlayer();
        Advancement advancement = event.getAdvancement();
        
        // Проверяем наличие Display у достижения
        if (advancement.getDisplay() == null) {
            return;
        }
        
        // Получаем информацию о достижении
        AdvancementDisplay display = advancement.getDisplay();
        
        // Проверяем, нужно ли объявлять о достижении
        if (!display.doesAnnounceToChat()) {
            return;
        }
        
        // Полностью отменяем ванильное сообщение о достижении 
        event.message(null);
        
        // Получаем формат сообщения о достижении
        String format = plugin.getPluginConfig().getString("system.achievements.format", 
                "&8[&e⭐&8] {prefix}&f{player}{suffix} &eполучил достижение &6[{achievement}]");
        
        // Получаем название и описание достижения в чистом виде
        String achievementTitle = componentToPlainText(display.title());
        String achievementDescription = componentToPlainText(display.description());
        
        // Заменяем плейсхолдеры
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", player.getName());
        placeholders.put("achievement", achievementTitle);
        placeholders.put("description", achievementDescription);
        
        // Формируем сообщение
        String message = format;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        
        // Применяем плейсхолдеры LuckPerms
        message = messageUtils.replaceLuckPermsPlaceholders(message, player);
        
        // Отправляем сообщение о достижении
        Bukkit.broadcastMessage(messageUtils.colorize(message));
        
        // Если нужно показывать описание и описание не пустое
        if (plugin.getPluginConfig().getBoolean("system.achievements.show_description", true) 
                && !achievementDescription.isEmpty()) {
            String descriptionFormat = plugin.getPluginConfig().getString("system.achievements.description_format", "&7{description}");
            String descriptionMessage = descriptionFormat.replace("{description}", achievementDescription);
            Bukkit.broadcastMessage(messageUtils.colorize(descriptionMessage));
        }
    }
    
    /**
     * Преобразует компонент в простой текст
     * 
     * @param component Компонент для преобразования
     * @return Простой текст без форматирования
     */
    private String componentToPlainText(Component component) {
        if (component == null) {
            return "";
        }
        return PlainTextComponentSerializer.plainText().serialize(component);
    }
} 