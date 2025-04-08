package com.github;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.scheduler.BukkitRunnable;

public class BaffMenu {
    static final Map<Player, BukkitRunnable> updateTasks = new HashMap<>();

    public BaffMenu() {
    }

    public static void openMenu(Player player, AnPotions plugin) {
        String menuTitle = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("menu.title", "Меню бафовара"));
        int menuSize = plugin.getConfig().getInt("menu.size", 54);
        Inventory menu = Bukkit.createInventory((InventoryHolder) null, menuSize, menuTitle);
        setupMenu(menu, plugin);
        player.openInventory(menu);
        startUpdateTask(player, menu, plugin);
    }

    private static void setupMenu(Inventory menu, AnPotions plugin) {
        Set<Integer> allowedPotionSlots = plugin.getConfig().getIntegerList("potion.slots").stream().collect(Collectors.toSet());
        String barrierMaterialName = plugin.getConfig().getString("barrier.material", "BARRIER");
        Material barrierMaterial = Material.getMaterial(barrierMaterialName.toUpperCase());
        if (barrierMaterial == null) {
            plugin.getLogger().warning("Материал барьера не найден: " + barrierMaterialName + ". Используется BARRIER по умолчанию.");
            barrierMaterial = Material.BARRIER;
        }

        ItemStack barrier = new ItemStack(barrierMaterial);
        ItemMeta barrierMeta = barrier.getItemMeta();
        if (barrierMeta != null) {
            barrierMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("barrier.displayName", "")));
            List<String> lore = plugin.getConfig().getStringList("barrier.lore");
            if (lore != null) {
                barrierMeta.setLore(lore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList()));
            }

            barrier.setItemMeta(barrierMeta);
        }

        for (int slot : plugin.getConfig().getIntegerList("barriers")) {
            if (slot >= 0 && slot < 54) {
                menu.setItem(slot, barrier);
            } else {
                plugin.getLogger().warning("Некорректный слот для барьера: " + slot);
            }
        }

        int barrier123Slot = plugin.getConfig().getInt("barrier123.slot", 49);
        if (barrier123Slot >= 0 && barrier123Slot < 54) {
            String barrier123MaterialName = plugin.getConfig().getString("barrier123.material", "BARRIER");
            Material barrier123Material = Material.getMaterial(barrier123MaterialName.toUpperCase());
            if (barrier123Material == null) {
                plugin.getLogger().warning("Материал барьера не найден: " + barrier123MaterialName + ". Используется BARRIER по умолчанию.");
                barrier123Material = Material.BARRIER;
            }

            ItemStack barrier123 = new ItemStack(barrier123Material);
            ItemMeta barrier123Meta = barrier123.getItemMeta();
            if (barrier123Meta != null) {
                barrier123Meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("barrier123.displayName", "")));
                List<String> lore = plugin.getConfig().getStringList("barrier123.lore");
                if (lore != null) {
                    barrier123Meta.setLore(lore.stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList()));
                }

                barrier123.setItemMeta(barrier123Meta);
            }

            menu.setItem(barrier123Slot, barrier123);
        } else {
            plugin.getLogger().warning("Некорректный слот для барьера: " + barrier123Slot);
        }

        String bookMaterialName = plugin.getConfig().getString("item.material", "BOOK");
        Material bookMaterial = Material.getMaterial(bookMaterialName.toUpperCase());
        if (bookMaterial == null) {
            plugin.getLogger().warning("Материал книги не найден: " + bookMaterialName + ". Используется BOOK по умолчанию.");
            bookMaterial = Material.BOOK;
        }

        int bookSlot = plugin.getConfig().getInt("item.slot", 4);
        int customModelData = plugin.getConfig().getInt("item.customModelData", 1);
        ItemStack book = new ItemStack(bookMaterial);
        ItemMeta bookMeta = book.getItemMeta();
        if (bookMeta != null) {
            bookMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("item.displayName", "&6Стакаем зелья")));
            bookMeta.setLore(plugin.getConfig().getStringList("item.lore").stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList()));
            // Проверка, если метод setCustomModelData доступен
            if (bookMeta instanceof Damageable) {
                bookMeta.setCustomModelData(customModelData);
            }
            book.setItemMeta(bookMeta);
        }

        menu.setItem(bookSlot, book);
        String lightGrayMaterial = plugin.getConfig().getString("panels.lightGray.material", "LIGHT_GRAY_STAINED_GLASS_PANE");
        String lightGrayDisplayName = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("panels.lightGray.display_name", ""));
        ItemStack lightGrayPane = new ItemStack(Material.valueOf(lightGrayMaterial));
        ItemMeta lightGrayMeta = lightGrayPane.getItemMeta();
        if (lightGrayMeta != null) {
            lightGrayMeta.setDisplayName(lightGrayDisplayName);
            lightGrayPane.setItemMeta(lightGrayMeta);
        }

        String grayMaterial = plugin.getConfig().getString("panels.gray.material", "GRAY_STAINED_GLASS_PANE");
        String grayDisplayName = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("panels.gray.display_name", ""));
        ItemStack grayPane = new ItemStack(Material.valueOf(grayMaterial));
        ItemMeta grayMeta = grayPane.getItemMeta();
        if (grayMeta != null) {
            grayMeta.setDisplayName(grayDisplayName);
            grayPane.setItemMeta(grayMeta);
        }

        for (int slot : plugin.getConfig().getIntegerList("panels.lightGray.slots")) {
            if (slot >= 0 && slot < 54) {
                menu.setItem(slot, lightGrayPane);
            } else {
                plugin.getLogger().warning("Некорректный слот для светло-серой панели: " + slot);
            }
        }

        for (int slot : plugin.getConfig().getIntegerList("panels.gray.slots")) {
            if (slot >= 0 && slot < 54) {
                menu.setItem(slot, grayPane);
            } else {
                plugin.getLogger().warning("Некорректный слот для серой панели: " + slot);
            }
        }

        loadCustomItems(menu, plugin);
    }

    private static void loadCustomItems(Inventory menu, AnPotions plugin) {
        FileConfiguration config = plugin.getConfig();
        ConfigurationSection itemsSection = config.getConfigurationSection("customItems");
        if (itemsSection != null) {
            for (String key : itemsSection.getKeys(false)) {
                String materialName = itemsSection.getString(key + ".material", "STONE");
                Material material = Material.getMaterial(materialName.toUpperCase());
                if (material != null) {
                    ItemStack item = new ItemStack(material);
                    ItemMeta meta = item.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', itemsSection.getString(key + ".displayName", "")));
                        meta.setLore(itemsSection.getStringList(key + ".lore").stream().map(line -> ChatColor.translateAlternateColorCodes('&', line)).collect(Collectors.toList()));
                        item.setItemMeta(meta);
                    }

                    List<Integer> slots = itemsSection.getIntegerList(key + ".slots");
                    if (!slots.isEmpty()) {
                        for (Integer slot : slots) {
                            if (slot >= 0 && slot < 54) {
                                menu.setItem(slot, item);
                            } else {
                                plugin.getLogger().warning("Некорректный слот для кастомного предмета " + key + " не указаны.");
                            }
                        }
                    } else {
                        plugin.getLogger().warning("Слоты для кастомного предмета " + key + " не указаны.");
                    }
                } else {
                    plugin.getLogger().warning("Материал для кастомного предмета " + key + " не найден: " + materialName);
                }
            }
        }
    }

    private static void startUpdateTask(Player player, Inventory menu, AnPotions plugin) {
        if (updateTasks.containsKey(player)) {
            updateTasks.get(player).cancel();
        }

        BukkitRunnable task = new UpdateTask(player, menu, plugin); // замените `UpdateTask` на реальное имя класса
        task.runTaskTimer(plugin, 0L, 15L);
        updateTasks.put(player, task);
    }

    public static void updateBookPrice(Inventory inventory, AnPotions plugin) {
        double costPerPotion = plugin.getConfig().getDouble("potion.cost-per-potion");
        int requiredSlots = plugin.getConfig().getInt("potion.amount");
        String materialName = plugin.getConfig().getString("item.material", "BOOK");
        Material material = Material.getMaterial(materialName);
        if (material == null) {
            plugin.getLogger().warning("Материал из конфигурации не найден: " + materialName);
        } else {
            int totalPotions = 0;
            boolean allSlotsFilled = true;

            for (int i = 10; i <= 34; ++i) {
                ItemStack potionItem = inventory.getItem(i);
                if (potionItem == null || (potionItem.getType() != Material.POTION && potionItem.getType() != Material.SPLASH_POTION && potionItem.getType() != Material.LINGERING_POTION)) {
                    allSlotsFilled = false;
                } else {
                    totalPotions += potionItem.getAmount();
                }
            }

            if (totalPotions < requiredSlots) {
                allSlotsFilled = false;
            }

            ItemStack bookItem = inventory.getItem(4);
            if (bookItem != null && bookItem.getType() == material) {
                ItemMeta meta = bookItem.getItemMeta();
                if (meta != null) {
                    List<String> loreConfig = plugin.getConfig().getStringList("item.lore");
                    List<String> lore = new ArrayList<>();

                    for (String line : loreConfig) {
                        lore.add(ChatColor.translateAlternateColorCodes('&', line));
                    }

                    meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("item.displayName", "&6Стакаем зелья")));
                    meta.setLore(lore);
                    bookItem.setItemMeta(meta);
                    inventory.setItem(4, bookItem);
                }
            }
        }
    }

    // Пример класса UpdateTask, который расширяет BukkitRunnable
    private static class UpdateTask extends BukkitRunnable {
        private final Player player;
        private final Inventory menu;
        private final AnPotions plugin;

        public UpdateTask(Player player, Inventory menu, AnPotions plugin) {
            this.player = player;
            this.menu = menu;
            this.plugin = plugin;
        }

        @Override
        public void run() {
            // Логика обновления меню
        }
    }
}