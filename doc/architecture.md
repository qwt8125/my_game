# 技术架构与数据设计（当前版，至 v3.7）

## 1. 已定技术路线

后台采用当前本地 Java 环境优先的技术栈：

- OpenJDK 8
- Spring Boot 2.7.18
- SQLite
- Maven
- 原生 HTML/CSS/JavaScript 前端起步

首版采用轻量单体架构：

```text
浏览器
  -> HTTP/SSE
  -> Spring Boot 2 game-server
  -> SQLite
  -> JSON 配置文件
```

后期扩展：

```text
浏览器
  -> HTTP/SSE
  -> game-server
  -> SQLite/PostgreSQL
  -> Redis
  -> JSON 配置文件
```

## 2. 目录约定

```text
F:\paly
  doc\
    PRD.md
    architecture.md
    api.md
    game-rules.md
  game\
    README.md
    pom.xml
    config\
    data\
    scripts\
    src\
      main\
        java\
          com\paly\legend\
        resources\
          application.yml
          db\schema.sql
          config\
      test\
        java\
          com\paly\legend\
    web\
```

## 3. 存储分层

### 3.1 SQLite

SQLite 负责持久化不能丢的数据：

- accounts
- characters
- inventory_items
- equipped_items
- task_progress
- currency_logs
- drop_logs
- login_logs

### 3.2 配置文件

配置文件负责维护游戏静态规则。当前实际配置位于 `game/src/main/resources/config/`：

- `maps.json`
- `map-events.json`
- `npcs.json`
- `monsters.json`
- `items.json`
- `drops.json`
- `tasks.json`
- `levels.json`
- `bosses.json`
- `world-bosses.json`
- `classes.json`
- `skills.json`
- `talents.json`
- `enhancement-rules.json`
- `activities.json`

这些文件可以随版本发布，也可以后续通过后台热重载。

### 3.3 Redis 当前使用边界

Redis 只负责临时、高频、可重建数据：

- online users
- running battle state
- BOSS cooldown cache
- ranking short TTL cache

当前会话 token 仍以 SQLite `auth_tokens` 为事实来源；限流计数属于后续安全增强方向，尚未作为当前实现范围。

当前已使用 Redis 15 号库保存运行中战斗、在线状态、BOSS 冷却缓存和排行榜短 TTL 缓存。战斗会话通过 `BattleSessionStore` 抽象管理；会话结束后的资产、日志和完整 actions 仍写 SQLite；SQLite 实现保留为回退与测试模式。

本地 Redis 连接预留为：

```text
redis://127.0.0.1:6379/15
```

## 4. 数据库表

当前 schema 以 `game/src/main/resources/db/schema.sql` 为准。核心表按职责分为：

- 账号与登录：`accounts`、`auth_tokens`、`login_logs`
- 角色与成长：`characters`、`character_skills`、`character_talents`、`battle_preparations`
- 资产与流水：`inventory_items`、`equipped_items`、`currency_logs`、`drop_logs`
- 战斗与挂机：`battle_sessions`、`battle_logs`、`idle_sessions`
- 地图与任务：`task_progress`、`map_event_states`
- BOSS 与世界 BOSS：`boss_states`、`world_boss_states`、`world_boss_damage_logs`
- 运营与邮件：`mails`、`gm_logs`、`gm_system_logs`
- 活动状态：`activity_claims`
- 公会状态：`guilds`、`guild_members`、`guild_logs`

下方保留关键表结构说明，完整字段以 schema 文件为准。

### 4.1 accounts

| 字段 | 类型 | 说明 |
|---|---|---|
| id | INTEGER | 主键 |
| username | TEXT | 登录名 |
| password_hash | TEXT | 密码哈希 |
| status | INTEGER | 状态，0 正常，1 封禁 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 4.2 characters

| 字段 | 类型 | 说明 |
|---|---|---|
| id | INTEGER | 主键 |
| account_id | INTEGER | 账号 ID |
| nickname | TEXT | 昵称 |
| class | TEXT | 职业 |
| level | INTEGER | 等级 |
| exp | INTEGER | 当前经验 |
| gold | INTEGER | 金币 |
| hp | INTEGER | 生命 |
| attack | INTEGER | 攻击 |
| defense | INTEGER | 防御 |
| attack_speed | INTEGER | 攻击速度 |
| power | INTEGER | 战力 |
| current_map_id | TEXT | 当前地图 |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 4.3 battle_sessions

v0.3 新增，用于在线会话式回合战斗。

| 字段 | 类型 | 说明 |
|---|---|---|
| id | INTEGER | 主键 |
| character_id | INTEGER | 角色 ID |
| map_id | TEXT | 地图配置 ID |
| monster_id | TEXT | 怪物配置 ID |
| status | TEXT | running/finished/cancelled |
| round | INTEGER | 当前回合 |
| player_hp | INTEGER | 玩家当前 HP |
| monster_hp | INTEGER | 怪物当前 HP |
| next_actor | TEXT | player/monster |
| settled | INTEGER | 是否已结算奖励，0 否，1 是 |
| actions_json | TEXT | 已发生行动 JSON |
| result_json | TEXT | 结束奖励和快照 JSON |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |
| finished_at | DATETIME | 结束时间 |

约束：

- 同一角色同一时间只允许一条 `running` 战斗会话。
- `next` 推进行动和结束结算必须放在事务中。
- 奖励结算以 `settled` 做幂等保护。

### 4.4 inventory_items

| 字段 | 类型 | 说明 |
|---|---|---|
| id | INTEGER | 主键 |
| character_id | INTEGER | 角色 ID |
| item_id | TEXT | 配置物品 ID |
| item_type | TEXT | item/equipment/material |
| quantity | INTEGER | 数量 |
| bind_status | INTEGER | 是否绑定 |
| extra_json | TEXT | 装备强化等级与随机词条 JSON |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 4.5 equipped_items

| 字段 | 类型 | 说明 |
|---|---|---|
| id | INTEGER | 主键 |
| character_id | INTEGER | 角色 ID |
| slot | TEXT | 装备部位 |
| inventory_item_id | INTEGER | 背包物品 ID |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 4.6 task_progress

| 字段 | 类型 | 说明 |
|---|---|---|
| id | INTEGER | 主键 |
| character_id | INTEGER | 角色 ID |
| task_id | TEXT | 任务配置 ID |
| status | INTEGER | 0 未完成，1 可领取，2 已领取 |
| progress_json | TEXT | 进度 JSON |
| created_at | DATETIME | 创建时间 |
| updated_at | DATETIME | 更新时间 |

### 4.7 guilds / guild_members / guild_logs / guild_donations / guild_shop_purchases

v3.5 新增公会基础关系，v3.6 补充贡献与商店闭环。

- `guilds`：公会名称、公告、会长角色、成员数、公会总贡献和创建时间。
- `guild_members`：公会成员、身份、贡献和加入时间；`character_id` 唯一，保证一个角色只加入一个公会。
- `guild_logs`：公会创建、加入、退出、踢人、转让和解散等操作日志。
- `guild_donations`：记录角色每日捐献档位、金币消耗和贡献获得，用于每日次数校验和审计。
- `guild_shop_purchases`：记录公会商店购买商品、道具、数量、贡献消耗和金币消耗，用于每日限购和审计。

v3.7 新增：

- `guild_activity_claims`：记录同一角色在同一公会内对同一公会活动的领取状态，避免重复领取。

## 5. 关键设计原则

### 5.1 核心资产必须落库

金币、装备、经验、任务奖励等都必须以 SQLite 为最终事实来源。战斗结算要使用事务，避免玩家重复领取或异常掉落。

### 5.2 配置和玩家数据分离

怪物、装备、地图等静态规则不要写死在数据库里。首版使用配置文件可以让版本迭代更轻，后续也方便做配置后台。

### 5.3 Redis 抽象边界

临时和高频状态通过以下抽象隔离具体实现：

```text
SessionStore
OnlineStore
BattleSessionStore
CooldownStore
RankingStore
```

新增 Redis 能力时，应继续通过抽象接口替换实现，不大改业务代码。

v0.3 的战斗状态通过 `BattleSessionStore` 读写。线上默认使用 Redis 版本保存 `running` 状态；单机或回退场景可切换 SQLite 版本。战斗结束后的资产、日志和完整 actions 仍以 SQLite 为最终事实来源。

在线状态通过短 TTL Redis key 维护，玩家每次带 token 访问接口都会刷新活跃时间。BOSS 冷却采用 Redis 缓存 + SQLite 持久化双写，Redis 用于高频读取，SQLite 保留最终可恢复状态。排行榜通过 `legend:ranking:{type}:{limit}` 短 TTL JSON 缓存减轻 SQLite 高频排序压力，缓存不可用时直接查询 SQLite；v2.9 起新增 `legend:ranking:snapshot:{type}:{limit}` 快照缓存，用于返回榜单来源、生成时间、刷新间隔和下次刷新时间。v3.0 起运营活动通过 `activities.json` 配置化维护；v3.1 起活动领取状态写入 SQLite `activity_claims`，金币与物品仍复用 `currency_logs`、`inventory_items` 和 `drop_logs`。v3.2 起活动效果仍由 `activities.json` 配置驱动，通过 `ActivityEffectService` 聚合 active 活动效果后注入战斗、挂机和世界 BOSS 奖励结算，不新增数据库表。v3.3 起榜单阶段奖励仍由 `activities.json` 的 `rankingRewards` 配置驱动，当前名次通过 SQLite 排序规则实时计算，领取状态继续复用 `activity_claims`。v3.4 起 GM 活动编辑后台仍写回 `activities.json`，保存前复用完整配置校验，操作流水写入 `gm_system_logs`，不新增数据库表。v3.5 起公会基础关系写入 SQLite，`guild_members.character_id` 保证单角色单公会，公会操作流水写入 `guild_logs`。v3.6 起公会贡献和商店由 `guilds.json` 配置驱动，贡献、捐献和购买事实写入 SQLite；v3.7 起公会活动与排行仍直接基于 SQLite 公会总贡献排序和领取状态落库，不引入新的 Redis 事实来源。

### 5.4 日志流水不可忽略

文字页游容易出现装备、金币、经验争议。首版至少保留：

- 金币变化流水
- 物品获得流水
- 登录日志
- 管理员操作日志

## 6. 部署建议

### 6.1 轻量部署

```text
/opt/text-legend/
  game-server
  config/
  data/game.db
  logs/
```

### 6.2 systemd 运行

当前仓库已提供 systemd 示例：

```text
text-legend.service
```

负责开机自启、异常重启和日志托管。

### 6.3 备份策略

- 每天备份 SQLite 数据库文件。
- 发布前备份配置目录。
- 重要操作前导出玩家资产快照。

## 7. 技术选型待定项

后台已确定使用 Java 8。为了保留后续迁移空间，业务层不要直接依赖 SQLite 细节，统一通过 Repository/DAO 层访问数据库。

注意：

- 当前阶段不要求升级本地 JDK。
- 项目代码必须保持 Java 8 语法兼容。
- 暂不使用 record、var、sealed class、switch expression 等新版本 Java 特性。
- 后续需要长期运营或接入新生态库时，再规划 Java 17/21 升级。

推荐分层：

```text
Controller
  -> Service
  -> Repository
  -> SQLite
```

推荐模块：

```text
auth         账号、登录、会话
character   角色、等级、属性
map          地图与怪物配置查询
battle       战斗结算
inventory    背包与物品
equipment    装备穿戴
ranking      排行榜
admin        GM 管理能力，首版可后置
common       通用响应、异常、时间、事务工具
```
