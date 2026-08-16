package com.mcstaralliance.antioffhand;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
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
     * 关闭 GUI 时按 F 切换主副手。
     * 注意：PlayerSwapHandItemsEvent 的 getMainHandItem()/getOffHandItem() 语义在
     * Spigot 实现中与 Javadoc 相反，混合服（Arclight/Mohist 等）实现亦不统一。
     * 事件在交换发生前触发且可取消，因此此刻玩家主手里的物品就是「即将进入副手」
     * 的物品——直接读背包真实状态判断，不依赖有歧义的 getter。
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSwapHands(PlayerSwapHandItemsEvent event) {
        ItemStack enteringOffhand = event.getPlayer().getInventory().getItemInMainHand();
        if (plugin.shouldBlock(enteringOffhand)) {
            event.setCancelled(true);
            notify(event.getPlayer());
        }
    }

    /**
     * 背包内一切点击：判断「即将进入副手的物品」是否被过滤。
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        ClickType click = event.getClick();

        // GUI 内按 F：悬停槽位与副手互换，悬停物品会进入副手
        if (click == ClickType.SWAP_OFFHAND) {
            if (plugin.shouldBlock(event.getCurrentItem())) {
                event.setCancelled(true);
                notify(player);
            }
            return;
        }

        // 直接操作副手槽位（slot 40）：判断光标 / 数字键槽位物品是否进入副手
        if (event.getClickedInventory() instanceof PlayerInventory
                && event.getSlot() == plugin.offhandSlot()) {
            ItemStack entering = itemEnteringOffhand(event, player);
            if (plugin.shouldBlock(entering)) {
                event.setCancelled(true);
                notify(player);
            }
            return;
        }

        // shift 点击盾牌：原版会自动装备进副手
        if (click.isShiftClick()
                && event.getClickedInventory() instanceof PlayerInventory) {
            ItemStack item = event.getCurrentItem();
            if (item != null && item.getType().name().equals("SHIELD")
                    && plugin.shouldBlock(item)) {
                event.setCancelled(true);
                notify(player);
            }
        }
    }

    /**
     * 创造模式物品栏操作副手槽位。
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCreativeClick(InventoryCreativeEvent event) {
        if (event.getSlot() == plugin.offhandSlot()
                && plugin.shouldBlock(event.getCursor())) {
            event.setCancelled(true);
            if (event.getWhoClicked() instanceof Player player) {
                notify(player);
            }
        }
    }

    /**
     * 拖拽物品到副手槽位。
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }
        int offhand = plugin.offhandSlot();
        for (int raw : event.getRawSlots()) {
            if (event.getView().getInventory(raw) instanceof PlayerInventory
                    && event.getView().convertSlot(raw) == offhand) {
                ItemStack placed = event.getNewItems().get(raw);
                if (plugin.shouldBlock(placed)) {
                    event.setCancelled(true);
                    notify(player);
                    return;
                }
            }
        }
    }

    /**
     * 上线时把副手中「被过滤」的残留物品清出并掉落；白名单内的物品（如背包）保留。
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        ItemStack offhand = event.getPlayer().getInventory().getItemInOffHand();
        if (offhand != null && !offhand.getType().isAir() && plugin.shouldBlock(offhand)) {
            event.getPlayer().getInventory().setItemInOffHand(null);
            event.getPlayer().getWorld()
                    .dropItemNaturally(event.getPlayer().getLocation(), offhand);
        }
    }

    /**
     * 取出本次点击中「会进入副手槽位」的物品。
     */
    private ItemStack itemEnteringOffhand(InventoryClickEvent event, Player player) {
        if (event.getClick() == ClickType.NUMBER_KEY) {
            int hotbar = event.getHotbarButton();
            if (hotbar >= 0 && hotbar < 9) {
                return player.getInventory().getItem(hotbar);
            }
            return null;
        }
        return event.getCursor();
    }

    private void notify(Player player) {
        if (plugin.notifyEnabled()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.message()));
        }
    }
}
