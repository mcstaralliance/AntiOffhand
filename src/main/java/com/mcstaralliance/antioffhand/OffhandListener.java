package com.mcstaralliance.antioffhand;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public final class OffhandListener implements Listener {

    private final AntiOffhand plugin;

    public OffhandListener(AntiOffhand plugin) {
        this.plugin = plugin;
    }

    /**
     * 阻止按 F 键切换主副手。
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        event.setCancelled(true);
        notify(event.getPlayer());
    }

    /**
     * 阻止一切把物品放入副手槽位的点击操作：
     * - 直接点击副手槽位（放入、交换、数字键、快捷键等）
     * - shift 点击盾牌/图腾等会自动装备到副手的物品
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        // 在背包 GUI 里按 F 键：触发的是 SWAP_OFFHAND 点击类型，
        // 会把鼠标悬停的槽位与副手槽互换，PlayerSwapHandItemsEvent 不会触发，必须在这里拦。
        if (event.getClick() == ClickType.SWAP_OFFHAND) {
            event.setCancelled(true);
            notify(player);
            return;
        }

        // 直接点击玩家背包中的副手槽位（slot 40）
        if (event.getClickedInventory() instanceof PlayerInventory
                && event.getSlot() == plugin.offhandSlot()) {
            event.setCancelled(true);
            notify(player);
            return;
        }

        // shift 点击盾牌：原版行为是直接装备进副手，直接取消
        if (event.getClick().isShiftClick()
                && event.getClickedInventory() instanceof PlayerInventory) {
            ItemStack item = event.getCurrentItem();
            if (item != null && item.getType().name().equals("SHIELD")) {
                event.setCancelled(true);
                notify(player);
            }
        }
    }

    /**
     * 阻止创造模式物品栏中操作副手槽位。
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCreativeClick(InventoryCreativeEvent event) {
        if (event.getSlot() == plugin.offhandSlot()) {
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof Player player) {
                notify(player);
            }
        }
    }

    /**
     * 阻止把物品拖拽到副手槽位。
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        for (int raw : event.getRawSlots()) {
            if (event.getView().getInventory(raw) instanceof PlayerInventory
                    && event.getView().convertSlot(raw) == plugin.offhandSlot()) {
                event.setCancelled(true);
                notify(player);
                return;
            }
        }
    }

    /**
     * 玩家进入服务器时清掉副手中残留的物品，防止下线时绕过。
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        ItemStack offhand = event.getPlayer().getInventory().getItemInOffHand();
        if (offhand != null && !offhand.getType().isAir()) {
            event.getPlayer().getInventory().setItemInOffHand(null);
            event.getPlayer().getWorld()
                    .dropItemNaturally(event.getPlayer().getLocation(), offhand);
        }
    }

    private void notify(Player player) {
        if (plugin.notifyEnabled()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.message()));
        }
    }
}
