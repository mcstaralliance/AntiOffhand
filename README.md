# AntiOffhand

适用于 Bukkit / Spigot 1.21.1 的副手禁用插件。

玩家无法将物品放入副手槽位，也无法通过切换副手动作使用副手中的物品；上线时副手中的残留物品会被自动清出并掉落，防止绕过。

## 功能

- 阻止按 `F` 键切换主副手（`PlayerSwapHandItemsEvent`）
- 阻止在背包中点击副手槽位（slot 40）放入 / 交换 / 数字键 / 快捷键（`InventoryClickEvent`）
- 阻止 shift 点击盾牌自动装备进副手
- 阻止拖拽物品到副手槽位（`InventoryDragEvent`）
- 阻止创造模式物品栏操作副手槽位（`InventoryCreativeEvent`）
- 玩家上线时清空副手残留物品并掉落到脚下

## 安装

1. 将 `target/AntiOffhand-1.0.0.jar` 放入服务端 `plugins/` 目录
2. 重启服务端，插件会自动生成 `plugins/AntiOffhand/config.yml`

## 配置

`config.yml`：

```yaml
# 是否在玩家尝试使用副手时提示
notify-player: true
# 提示内容（支持 & 颜色代码）
message: "&c副手已被禁用！"
```

修改后执行 `reload` 或重启服务端生效。

## 编译

需要 JDK 21 与 Maven。

```bash
mvn clean package
```

产物位于 `target/AntiOffhand-1.0.0.jar`。

## 槽位说明

玩家背包槽位编号：`0-8` 快捷栏，`9-35` 主背包，`36-39` 盔甲，`40` 副手。
本插件通过 `InventoryClickEvent#getSlot() == 40` 判定副手槽位（而非 `getRawSlot()`，后者在自己背包界面中为 45），并使用 `InventoryView#convertSlot(raw)` 对拖拽事件做换算。

## 技术信息

- API：Spigot 1.21.1（`api-version: 1.21`）
- Java：21
- 包名：`com.mcstaralliance.antioffhand`
