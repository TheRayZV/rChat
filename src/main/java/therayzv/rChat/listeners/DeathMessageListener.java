package therayzv.rChat.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import therayzv.rChat.RChat;
import therayzv.rChat.utils.MessageUtils;

import java.util.HashMap;
import java.util.Map;

public class DeathMessageListener implements Listener {
    
    private final RChat plugin;
    private final MessageUtils messageUtils;
    
    public DeathMessageListener(RChat plugin) {
        this.plugin = plugin;
        this.messageUtils = new MessageUtils(plugin);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        // Проверяем, включены ли кастомные сообщения о смерти
        if (!plugin.getPluginConfig().getBoolean("system.death.enabled", true)) {
            return;
        }
        
        Player player = event.getEntity();
        Entity killer = player.getKiller();
        
        // Отменяем стандартное сообщение о смерти
        event.setDeathMessage(null);
        
        // Выбираем соответствующий формат сообщения
        String format;
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", player.getName());
        placeholders.put("world", player.getWorld().getName());
        
        if (killer != null && killer instanceof Player) {
            // Если игрок был убит другим игроком
            Player killerPlayer = (Player) killer;
            format = plugin.getPluginConfig().getString("system.death.killed_by_player_format", 
                    "&8[&4☠&8] {prefix}&f{player} &7был убит игроком &f{killer}");
            
            placeholders.put("killer", killerPlayer.getName());
            placeholders.put("weapon", getWeaponName(killerPlayer));
            placeholders.put("killer_world", killerPlayer.getWorld().getName());
            
            // Формируем сообщение
            String message = format;
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
            
            // Применяем плейсхолдеры LuckPerms для игрока
            message = messageUtils.replaceLuckPermsPlaceholders(message, player);
            
            // Применяем плейсхолдеры LuckPerms для убийцы (префикс убийцы)
            message = message.replace("{killer_prefix}", plugin.getLuckPermsHook() != null ? 
                    plugin.getLuckPermsHook().getPlayerPrefix(killerPlayer) : "");
            
            // Отправляем сообщение о смерти всем игрокам
            Bukkit.broadcastMessage(messageUtils.colorize(message));
        } else {
            // Если игрок умер по другой причине
            format = plugin.getPluginConfig().getString("system.death.generic_format", 
                    "&8[&4☠&8] {prefix}&f{player} &7погиб");
            
            // Если есть сообщение о причине смерти, добавляем его
            String deathCause = getDeathCause(event);
            if (deathCause != null) {
                placeholders.put("cause", deathCause);
            }
            
            // Формируем сообщение
            String message = format;
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
            
            // Применяем плейсхолдеры LuckPerms для игрока
            message = messageUtils.replaceLuckPermsPlaceholders(message, player);
            
            // Отправляем сообщение о смерти всем игрокам
            Bukkit.broadcastMessage(messageUtils.colorize(message));
        }
    }
    
    /**
     * Получает название оружия, которым был убит игрок
     * @param killer Игрок-убийца
     * @return Название оружия
     */
    private String getWeaponName(Player killer) {
        if (killer.getInventory().getItemInMainHand().getItemMeta() != null && 
                killer.getInventory().getItemInMainHand().getItemMeta().hasDisplayName()) {
            return killer.getInventory().getItemInMainHand().getItemMeta().getDisplayName();
        } else if (!killer.getInventory().getItemInMainHand().getType().isAir()) {
            return killer.getInventory().getItemInMainHand().getType().name()
                    .toLowerCase().replace("_", " ");
        } else {
            return "кулаками";
        }
    }
    
    /**
     * Получает причину смерти игрока на русском языке
     * @param event Событие смерти
     * @return Причина смерти
     */
    private String getDeathCause(PlayerDeathEvent event) {
        switch (event.getEntity().getLastDamageCause() != null ? 
                event.getEntity().getLastDamageCause().getCause() : null) {
            case BLOCK_EXPLOSION:
                return "от взрыва";
            case CONTACT:
                return "от контакта с блоком";
            case CRAMMING:
                return "от давки сущностей";
            case DROWNING:
                return "утонув";
            case ENTITY_ATTACK:
                return "от атаки существа";
            case ENTITY_EXPLOSION:
                return "от взрыва существа";
            case ENTITY_SWEEP_ATTACK:
                return "от размашистой атаки";
            case FALL:
                return "от падения";
            case FALLING_BLOCK:
                return "от падающего блока";
            case FIRE:
                return "сгорев";
            case FIRE_TICK:
                return "сгорев";
            case FLY_INTO_WALL:
                return "врезавшись в стену";
            case HOT_FLOOR:
                return "от горячего пола";
            case LAVA:
                return "в лаве";
            case LIGHTNING:
                return "от удара молнии";
            case MAGIC:
                return "от магии";
            case POISON:
                return "от яда";
            case PROJECTILE:
                return "от снаряда";
            case STARVATION:
                return "от голода";
            case SUFFOCATION:
                return "задохнувшись";
            case SUICIDE:
                return "совершив самоубийство";
            case THORNS:
                return "от шипов";
            case VOID:
                return "в пустоте";
            case WITHER:
                return "от иссушения";
            default:
                return "по неизвестной причине";
        }
    }
} 