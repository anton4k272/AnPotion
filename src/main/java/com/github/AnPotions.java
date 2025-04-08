package com.github;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class AnPotions extends JavaPlugin {
    private Map<String, String> messages = new HashMap<>();

    public AnPotions() {
    }

    @Override
    public void onEnable() {
        PluginCommand AnbaffCommand = this.getCommand("anbaff");
        if (AnbaffCommand != null) {
            AnbaffCommand.setExecutor(new BaffCommand(this));
        } else {
            this.getLogger().warning("Команда 'anbaff' не найдена в plugin.yml.");
        }

        this.getServer().getPluginManager().registerEvents(new BaffMenuListener(this), this);
        this.loadMessages();
        this.saveDefaultConfig();
        this.reloadConfig();
    }

    private void loadMessages() {
        FileConfiguration config = this.getConfig();
        ConfigurationSection messagesSection = config.getConfigurationSection("messages");
        if (messagesSection != null) {
            for (String key : messagesSection.getKeys(false)) {
                this.messages.put(key, messagesSection.getString(key));
            }
        }
    }

    public String getMessage(String key) {
        return this.getConfig().getString("messages." + key, "Сообщение не найдено!");
    }

    public String getColoredMessage(String key) {
        String message = this.getMessage(key);
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}