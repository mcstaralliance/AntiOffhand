# AntiOffhand

适用于 Bukkit / Spigot 1.21.1 的副手禁用插件。

玩家无法将物品放入副手槽位，也无法通过切换副手动作使用副手中的物品；上线时副手中「被过滤」的残留物品会被自动清出并掉落，防止绕过。

支持白名单 / 黑名单过滤：可按物品 id 精确或按 mod 命名空间通配匹配，默认黑名单已配置为只禁止「精妙背包 (Sophisticated Backpacks)」mod 的物品放入副手。

## 功能

- 阻止按 `F` 键切换主副手（`PlayerSwapHandItemsEvent`）
- 阻止背包 GUI 内按 `F`（`InventoryClickEvent` + `ClickType.SWAP_OFFHAND`）
- 阻止在背包中点击副手槽位（slot 40）放入 / 交换 / 数字键（`InventoryClickEvent`）
- 阻止 shift 点击盾牌自动装备进副手
- 阻止拖拽物品到副手槽位（`InventoryDragEvent`）
- 阻止创造模式物品栏操作副手槽位（`InventoryCreativeEvent`）
- 玩家上线时清出副手中被过滤的残留物品并掉落到脚下（白名单内物品保留）
- 白名单 / 黑名单物品过滤（配置驱动，代码不绑定具体 mod）

## 安装

1. 将 `target/AntiOffhand-1.2.1.jar` 放入服务端 `plugins/` 目录
2. 重启服务端，插件会自动生成 `plugins/AntiOffhand/config.yml`

## 配置

`config.yml`：

```yaml
notify-player: true
message: "&c该物品无法放入副手！"

filter:
  # WHITELIST = 仅列表内物品允许放入副手，其余禁止
  # BLACKLIST = 仅列表内物品禁止放入副手，其余允许
  mode: BLACKLIST
  # 支持精确 id（namespace:key）与通配前缀（namespace:*）
  items:
    - "sophisticatedbackpacks:*"
```

- 物品 id 取自 `Material#getKey()`（形如 `minecraft:shield`、`sophisticatedbackpacks:backpack`）；混合服务端上模组物品若无法取到 key，会退回 `Material#name()` 做匹配
- 通配 `mod:*` 可匹配该命名空间下所有物品
- 默认配置即「只禁止精妙背包 mod 物品进入副手」；如需改为「只允许精妙背包」，把 `mode` 改为 `WHITELIST` 即可

修改后执行 `reload` 或重启服务端生效。

## 编译

需要 JDK 21 与 Maven。

```bash
mvn clean package
```

产物位于 `target/AntiOffhand-1.2.1.jar`。

## 槽位说明

玩家背包槽位编号：`0-8` 快捷栏，`9-35` 主背包，`36-39` 盔甲，`40` 副手。
本插件通过 `InventoryClickEvent#getSlot() == 40` 判定副手槽位（而非 `getRawSlot()`，后者在自己背包界面中为 45），并使用 `InventoryView#convertSlot(raw)` 对拖拽事件做换算。

## 技术信息

- API：Spigot 1.21.1（`api-version: 1.21`）
- Java：21
- 包名：`com.mcstaralliance.antioffhand`
