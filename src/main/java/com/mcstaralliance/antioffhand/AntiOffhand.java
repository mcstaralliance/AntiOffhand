package com.mcstaralliance.antioffhand;

import org.bukkit.plugin.java.JavaPlugin;

public final class AntiOffhand extends JavaPlugin {

    private static final int OFFHAND_SLOT = 40; // PlayerInventory off-hand slot index

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new OffhandListener(this), this);
        getLogger().info("AntiOffhand 已启用：副手放置与切换操作均被禁止。");
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
        return getConfig().getString("message", "&c副手已被禁用！");
    }
}
