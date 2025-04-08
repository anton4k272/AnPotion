package com.github;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class BaffMenuListener implements Listener {
    private final AnPotions plugin;
    private Economy economy;

    public BaffMenuListener(AnPotions plugin) {
        this.plugin = plugin;
        this.setupEconomy();
    }

    private void setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = this.plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            this.plugin.getLogger().warning("Не удалось подключиться к Vault!");
        } else {
            this.economy = rsp.getProvider();
        }
    }

    private double getPotionCost() {
        return this.plugin.getConfig().getDouble("potion.cost-per-potion");
    }

    private Set<Integer> getAllowedPotionSlots() {
        List<Integer> slots = this.plugin.getConfig().getIntegerList("potion.slots");
        return new HashSet<>(slots);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            Inventory clickedInventory = event.getInventory();
            String title = ChatColor.translateAlternateColorCodes('&', event.getView().getTitle());
            String menuTitle = ChatColor.translateAlternateColorCodes('&', this.plugin.getConfig().getString("menu.title", "Меню бафовара"));
            if (clickedInventory != null && title.equals(menuTitle)) {
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                    return;
                }

                int slot = event.getSlot();
                Material barrierMaterial = Material.valueOf(this.plugin.getConfig().getString("barrier.material", "BARRIER"));
                Material lightGrayPaneMaterial = Material.valueOf(this.plugin.getConfig().getString("lightGray.material", "LIGHT_GRAY_STAINED_GLASS_PANE"));
                Material grayPaneMaterial = Material.valueOf(this.plugin.getConfig().getString("gray.material", "GRAY_STAINED_GLASS_PANE"));
                Material barrier123Material = Material.valueOf(this.plugin.getConfig().getString("barrier123.material", "BARRIER"));
                int barrier123Slot = this.plugin.getConfig().getInt("barrier123.slot", 49);
                Material bookMaterial = Material.valueOf(this.plugin.getConfig().getString("item.material", "BOOK"));
                int bookSlot = this.plugin.getConfig().getInt("item.slot", 4);
                List<Integer> lightGraySlots = this.plugin.getConfig().getIntegerList("lightGray.slots");
                List<Integer> graySlots = this.plugin.getConfig().getIntegerList("gray.slots");
                List<Integer> forbiddenSlots = this.plugin.getConfig().getIntegerList("forbidden.slots");
                Set<Integer> allowedPotionSlots = this.getAllowedPotionSlots();
                if (!this.isAvailableSlot(slot) && this.isPotion(clickedItem)) {
                    event.setCancelled(true);
                    player.sendMessage(ChatColor.RED + "Вы не можете положить зелья в этот слот!");
                }

                if (slot == barrier123Slot && clickedItem.getType() == barrier123Material) {
                    event.setCancelled(true);
                    return;
                }

                if (clickedItem.getType() == barrierMaterial || clickedItem.getType() == grayPaneMaterial || clickedItem.getType() == lightGrayPaneMaterial) {
                    event.setCancelled(true);
                    return;
                }

                if (clickedItem.getType() == bookMaterial && slot == bookSlot) {
                    event.setCancelled(true);
                    int totalPotions = 0;

                    for (int potionSlot : this.getAllowedPotionSlots()) {
                        ItemStack potionItem = clickedInventory.getItem(potionSlot);
                        if (potionItem != null && (potionItem.getType() == Material.POTION || potionItem.getType() == Material.SPLASH_POTION || potionItem.getType() == Material.LINGERING_POTION)) {
                            totalPotions += potionItem.getAmount();
                        }
                    }

                    int requiredPotionCount = this.plugin.getConfig().getInt("potion.amount", 7);
                    if (totalPotions < requiredPotionCount) {
                        for (String message : this.plugin.getConfig().getStringList("messages.nobaff")) {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                        }

                        return;
                    }

                    this.stackPotions(player, clickedInventory);
                    BaffMenu.updateBookPrice(event.getInventory(), this.plugin);
                } else {
                    if (clickedItem.getType() == Material.POTION || clickedItem.getType() == Material.SPLASH_POTION || clickedItem.getType() == Material.LINGERING_POTION) {
                        return;
                    }

                    event.setCancelled(true);
                }
            }

        }
    }

    private boolean isAvailableSlot(int slot) {
        List<Integer> lightGraySlots = this.plugin.getConfig().getIntegerList("lightGray.slots");
        List<Integer> graySlots = this.plugin.getConfig().getIntegerList("gray.slots");
        return !lightGraySlots.contains(slot) && !graySlots.contains(slot);
    }

    private boolean isPotion(ItemStack item) {
        return item != null && (item.getType() == Material.POTION || item.getType() == Material.SPLASH_POTION || item.getType() == Material.LINGERING_POTION);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            Player player = (Player) event.getWhoClicked();
            String menuTitle = ChatColor.translateAlternateColorCodes('&', this.plugin.getConfig().getString("menu.title", "Меню бафовара"));
            if (event.getView().getTitle().equals(menuTitle)) {
                for (Integer slot : event.getRawSlots()) {
                    if (!this.isAvailableSlot(slot) && this.isPotion(event.getOldCursor())) {
                        event.setCancelled(true);
                        player.sendMessage(ChatColor.RED + "Вы не можете положить зелья в этот слот!");
                        return;
                    }
                }
            }

        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        String menuTitle = ChatColor.translateAlternateColorCodes('&', this.plugin.getConfig().getString("menu.title", "Меню бафовара"));
        if (event.getView().getTitle().equals(menuTitle)) {
            Player player = (Player) event.getPlayer();
            Inventory inventory = event.getInventory();

            for (int slot : this.getAllowedPotionSlots()) {
                ItemStack item = inventory.getItem(slot);
                if (item != null && (item.getType() == Material.POTION || item.getType() == Material.SPLASH_POTION || item.getType() == Material.LINGERING_POTION)) {
                    this.returnPotionsToPlayer(player, item);
                }
            }

            ItemStack stackedPotion = inventory.getItem(49);
            if (stackedPotion != null && (stackedPotion.getType() == Material.POTION || stackedPotion.getType() == Material.SPLASH_POTION || stackedPotion.getType() == Material.LINGERING_POTION)) {
                this.returnPotionsToPlayer(player, stackedPotion);
            }

            if (BaffMenu.updateTasks.containsKey(player)) {
                ((BukkitRunnable) BaffMenu.updateTasks.get(player)).cancel();
                BaffMenu.updateTasks.remove(player);
            }
        }

    }

    private void returnPotionsToPlayer(Player player, ItemStack potion) {
        Map<Integer, ItemStack> excessItems = player.getInventory().addItem(new ItemStack[]{potion});
        if (!excessItems.isEmpty()) {
            for (ItemStack excessItem : excessItems.values()) {
                player.getWorld().dropItem(player.getLocation(), excessItem);
            }
        }

    }

    private void stackPotions(Player player, Inventory inventory) {
        int totalPotions = 0;
        ItemStack potionType = null;
        List<ItemStack> potionsToReturn = new ArrayList<>();
        Set<Integer> allowedSlots = this.getAllowedPotionSlots();
        int requiredPotionAmount = this.plugin.getConfig().getInt("potion.amount");

        for (int slot : allowedSlots) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && (item.getType() == Material.POTION || item.getType() == Material.SPLASH_POTION || item.getType() == Material.LINGERING_POTION)) {
                if (potionType == null) {
                    potionType = item.clone();
                }

                if (item.getType() == potionType.getType() && item.getItemMeta() != null && item.getItemMeta().equals(potionType.getItemMeta())) {
                    totalPotions += item.getAmount();
                    potionsToReturn.add(item);
                    inventory.clear(slot);
                }
            }
        }

        if (totalPotions == requiredPotionAmount) {
            ItemStack stackedPotion = potionType.clone();
            stackedPotion.setAmount(Math.min(totalPotions, 64));
            inventory.setItem(this.plugin.getConfig().getInt("barrier123.slot", 49), stackedPotion);
        } else {
            String barrierMaterialName = this.plugin.getConfig().getString("barrier123.material", "BARRIER");
            Material barrierMaterial = Material.getMaterial(barrierMaterialName.toUpperCase());
            if (barrierMaterial == null) {
                this.plugin.getLogger().warning("Материал барьера не найден: " + barrierMaterialName + ". Используется BARRIER по умолчанию.");
                barrierMaterial = Material.BARRIER;
            }

            ItemStack barrier = new ItemStack(barrierMaterial);
            ItemMeta barrierMeta = barrier.getItemMeta();
            if (barrierMeta != null) {
                barrierMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', this.plugin.getConfig().getString("barrier123.displayName", "&cНедостаточно средств")));
                List<String> lore = this.plugin.getConfig().getStringList("barrier123.lore");
                barrierMeta.setLore(lore.stream().map((line) -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList()));
                barrier.setItemMeta(barrierMeta);
            }

            inventory.setItem(this.plugin.getConfig().getInt("barrier123.slot", 49), barrier);
        }

        if (totalPotions > 0 && potionType != null) {
            double costPerPotion = this.getPotionCost();
            double totalCost = (double) totalPotions * costPerPotion;
            if (this.economy == null) {
                this.setupEconomy();
            }

            double playerBalance = this.economy.getBalance(player);
            if (playerBalance >= totalCost) {
                this.economy.withdrawPlayer(player, totalCost);
                potionType.setAmount(Math.min(totalPotions, 64));
                inventory.setItem(49, potionType);
                player.sendMessage(this.plugin.getColoredMessage("success"));
            } else {
                player.sendMessage(this.plugin.getColoredMessage("nomoney"));

                for (ItemStack potion : potionsToReturn) {
                    player.getInventory().addItem(new ItemStack[]{potion});
                }

                inventory.clear();
            }
        }

    }
}