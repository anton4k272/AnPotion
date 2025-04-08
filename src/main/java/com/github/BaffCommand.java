package com.github;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BaffCommand implements CommandExecutor {
    private final AnPotions plugin;

    public BaffCommand(AnPotions plugin) {
        this.plugin = plugin;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("admin") && sender.hasPermission("Anpotions.admin")) {
            if (args.length > 1 && args[1].equalsIgnoreCase("reload")) {
                this.plugin.reloadConfig();
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', this.plugin.getConfig().getString("messages.reload", "&aПлагин AnPotions перезагружен!")));
                return true;
            } else {
                sender.sendMessage("Используйте: /anbaff admin reload");
                return true;
            }
        } else if (args.length > 0) {
            sender.sendMessage("Неверная команда. Используйте: /anbaff или /anbaff admin reload");
            return true;
        } else if (!(sender instanceof Player)) {
            sender.sendMessage("Команда доступна только для игроков.");
            return true;
        } else {
            Player player = (Player)sender;
            BaffMenu.openMenu(player, this.plugin);
            return true;
        }
    }
}