# RankBoard

轻量 Fabric 服务端排行榜 mod，目标版本：

- Minecraft `1.21.11`
- Fabric Loader `0.18.4`
- Fabric API `0.141.4+1.21.11`

## 功能

右侧 sidebar 自动轮播四个历史排行榜：

1. 放置榜
2. 挖掘榜
3. 移动榜
4. 死亡榜

数据来自原版世界存档里的 `stats/*.json`，所以会包含装 mod 之前已经存在的历史统计。

## 构建

```bash
./gradlew build
```

构建完成后，mod 文件在：

```text
build/libs/rankboard-1.0.0.jar
```

把这个 jar 放进服务器 `mods/` 目录即可。

## 配置

第一次启动服务器后会生成：

```text
config/rankboard.json
```

默认配置：

```json
{
  "entriesPerPage": 6,
  "pageSeconds": 10,
  "refreshSeconds": 30,
  "compactNumbers": false,
  "includeZeroValues": false,
  "enabled": true,
  "filterMode": "blacklist",
  "players": [],
  "placedTitle": "放置榜",
  "minedTitle": "挖掘榜",
  "movedTitle": "移动榜",
  "deathsTitle": "死亡榜",
  "titleTemplate": "§6{title} §e{page}/{pages}",
  "nameTemplate": "§f{rank}. {name}",
  "valueTemplate": "§7{value}",
  "emptyText": "§7暂无数据"
}
```

说明：

- `entriesPerPage`: 每页显示多少名，建议 `5` 或 `6`
- `pageSeconds`: 每页停留秒数
- `refreshSeconds`: 从原版 stats 重新读取数据的间隔
- `compactNumbers`: 是否把数字显示成 `12.3k`
- `includeZeroValues`: 是否显示数值为 0 的玩家
- `enabled`: 是否启用 sidebar
- `filterMode`: 玩家过滤模式，支持 `off`、`blacklist`、`whitelist`
- `players`: 黑名单或白名单玩家名列表，不区分大小写
- `placedTitle` / `minedTitle` / `movedTitle` / `deathsTitle`: 四页标题
- `titleTemplate`: 右侧标题模板
- `nameTemplate`: 玩家名字行模板
- `valueTemplate`: 数值行模板
- `emptyText`: 没有数据时显示的文本

模板变量：

```text
{title} {page} {pages} {rank} {name} {value}
```

示例：隐藏假人 `bot_1` 和 `bot_2`，并自定义样式：

```json
{
  "filterMode": "blacklist",
  "players": ["bot_1", "bot_2"],
  "titleTemplate": "§b✦ {title} §7({page}/{pages})",
  "nameTemplate": "§f#{rank} §a{name}",
  "valueTemplate": "§8» §e{value}",
  "emptyText": "§7暂无上榜玩家"
}
```

如果只想显示指定玩家：

```json
{
  "filterMode": "whitelist",
  "players": ["Steve", "Alex"]
}
```

原版 sidebar 支持颜色代码和普通 Unicode 符号/emoji，但不支持单独调整字体大小。

## 命令

需要 2 级权限。

```text
/rankboard reload
/rankboard refresh
/rankboard next
/rankboard show placed
/rankboard show mined
/rankboard show moved
/rankboard show deaths
/rankboard status
```

## 注意

放置榜使用原版 `minecraft:used` 里所有方块物品的使用次数汇总，通常等价于历史放置方块数。少数特殊方块或交互可能和真正放置数有轻微差异，这是原版统计口径的限制。
