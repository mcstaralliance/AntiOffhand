package com.mcstaralliance.antioffhand;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class AntiOffhand extends JavaPlugin {

    private static final int OFFHAND_SLOT = 40; // PlayerInventory 副手槽位

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new OffhandListener(this), this);
        // 兜底：按可配置间隔周期复查副手。部分混合核心的 F 键交换事件触发时机/取消行为
        // 不可靠，周期性校验保证被过滤物品不会留在副手。默认 5 tick（0.25 秒），开销极低。
        long interval = Math.max(1L, (long) getConfig().getInt("check-interval-ticks", 5));
        getServer().getScheduler().runTaskTimer(this, this::enforceOffhandRules, interval, interval);
        getLogger().info("AntiOffhand 已启用。过滤模式：" + filterMode()
                + "，条目数：" + filterItems().size()
                + "，副手校验间隔：" + interval + " tick");
    }

    /**
     * 复查所有在线玩家的副手：若持有被过滤物品，挪回主手（主手为空）或背包（放不下则掉落）。
     */
    private void enforceOffhandRules() {
        for (Player player : getServer().getOnlinePlayers()) {
            PlayerInventory inv = player.getInventory();
            ItemStack off = inv.getItemInOffHand();
            if (!shouldBlock(off)) {
                continue;
            }
            inv.setItemInOffHand(null);
            if (inv.getItemInMainHand().getType().isAir()) {
                inv.setItemInMainHand(off);
            } else {
                Map<Integer, ItemStack> leftover = inv.addItem(off);
                leftover.values().forEach(item ->
                        player.getWorld().dropItemNaturally(player.getLocation(), item));
            }
            if (notifyEnabled()) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message()));
            }
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("AntiOffhand 已关闭。");
    }

    public int offhandSlot() {
        return OFFHAND_SLOT;
    }

    public boolean notifyEnabled() {
        return getConfig().getBoolean("notify-player", true);
    }

    public String message() {
        return getConfig().getString("message", "&c该物品无法放入副手！");
    }

    /**
     * 过滤模式：WHITELIST=仅列表内物品允许放入副手；BLACKLIST=仅列表内物品禁止放入副手。
     */
    public String filterMode() {
        String mode = getConfig().getString("filter.mode", "WHITELIST");
        return mode.equalsIgnoreCase("BLACKLIST") ? "BLACKLIST" : "WHITELIST";
    }

    public List<String> filterItems() {
        return getConfig().getStringList("filter.items");
    }

    /**
     * 判断该物品是否应被禁止放入副手。
     * 白名单模式：列表内 -> 允许，其余 -> 禁止。
     * 黑名单模式：列表内 -> 禁止，其余 -> 允许。
     * 列表项支持精确 id（namespace:key）与通配前缀（namespace:*）。
     */
    public boolean shouldBlock(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }
        boolean match = matchesList(item);
        return "BLACKLIST".equals(filterMode()) ? match : !match;
    }

    private boolean matchesList(ItemStack item) {
        String id = idOf(item).toLowerCase(Locale.ROOT);
        String name = item.getType().name().toLowerCase(Locale.ROOT);
        for (String raw : filterItems()) {
            if (raw == null) {
                continue;
            }
            String e = raw.trim().toLowerCase(Locale.ROOT);
            if (e.isEmpty()) {
                continue;
            }
            if (e.endsWith(":*")) {
                String prefix = e.substring(0, e.length() - 1); // "mod:"
                if (id.startsWith(prefix)) {
                    return true;
                }
            } else if (e.equalsIgnoreCase(id) || e.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    private String idOf(ItemStack item) {
        Material mat = item.getType();
        try {
            // 正常返回 namespace:key（原版为 minecraft:xxx，模组服上混合服务端可能返回 mod:xxx）
            return mat.getKey().toString();
        } catch (Throwable t) {
            // 某些混合服务端对模组物品 getKey 可能异常，退回枚举名
            return mat.name();
        }
    }
}
