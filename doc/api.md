# API 接口设计（当前版，至 v3.4）

## 当前接口总览

实际接口以 `game/src/main/java/com/paly/legend/**/*Controller.java` 为准，本文按当前控制器能力整理：

- 健康检查：`GET /api/health`
- 账号：`POST /api/auth/register`、`POST /api/auth/login`、`GET /api/auth/me`、`POST /api/auth/change-password`
- 角色：`POST /api/characters`、`GET /api/characters/me`、`GET /api/characters/classes`
- 地图与交互：`GET /api/maps`、`GET /api/maps/{mapId}`、`GET /api/maps/{mapId}/scene`、`POST /api/npcs/{npcId}/talk`、`POST /api/map-events/{eventId}/trigger`
- 战斗：`POST /api/battles/fight`、`POST /api/battles/start`、`POST /api/battles/encounter/start`、`POST /api/battles/{battleId}/next`、`POST /api/battles/{battleId}/skill`、`GET /api/battles/{battleId}`、`GET /api/battles/{battleId}/stream`
- 背包与装备：`GET /api/inventory`、`POST /api/inventory/{inventoryItemId}/sell`、`POST /api/inventory/{inventoryItemId}/discard`、`POST /api/inventory/materials/sell`、`POST /api/inventory/{inventoryItemId}/use`、`GET /api/equipment`、`POST /api/equipment/equip`、`POST /api/equipment/unequip`、`POST /api/equipment/enhance`、`POST /api/equipment/reroll-affixes`、`POST /api/equipment/decompose`
- 成长：`GET /api/skills`、`POST /api/skills/learn`、`POST /api/skills/upgrade`、`POST /api/skills/slot`、`GET /api/talents`、`POST /api/talents/upgrade`、`POST /api/talents/reset`
- 任务、挂机、排行榜、活动：`GET /api/tasks`、`POST /api/tasks/{taskId}/claim`、`GET /api/idle/status`、`POST /api/idle/start`、`POST /api/idle/claim`、`GET /api/rankings/level`、`GET /api/rankings/power`、`GET /api/rankings/gold`、`GET /api/rankings/{type}/snapshot`、`GET /api/activities`、`POST /api/activities/{activityId}/claim`
- BOSS：`GET /api/bosses`、`POST /api/bosses/{bossId}/start`、`POST /api/bosses/{bossId}/fight`、`GET /api/world-bosses`、`GET /api/world-bosses/{bossId}/rankings`、`POST /api/world-bosses/{bossId}/start`
- 邮件与 GM：`GET /api/mails`、`POST /api/mails/{mailId}/claim`、`POST /api/mails/{mailId}/read`、`POST /api/mails/{mailId}/delete`、`GET /api/admin/characters`、`POST /api/admin/grant-gold`、`POST /api/admin/grant-item`、`POST /api/admin/send-mail`、`GET /api/admin/activities`、`POST /api/admin/activities/{activityId}`、`GET /api/admin/map-event-states`、`POST /api/admin/map-event-states/reset`、`POST /api/admin/map-event-states/reset-all`、`POST /api/admin/map-event-states/cleanup`、`POST /api/admin/account-status`、`POST /api/admin/config/reload`

## 世界 BOSS 接口补充

### 世界 BOSS 列表

```text
GET /api/world-bosses
```

返回世界 BOSS 配置、状态、当前血量、最大血量、刷新时间、参与门槛和前 10 名伤害排行。

### 开始世界 BOSS 挑战

```text
POST /api/world-bosses/{bossId}/start
```

响应同在线战斗会话。世界 BOSS 复用 `/api/battles/{battleId}/stream` SSE 行动推送；战斗结束后只累计伤害，不直接发放普通战斗经验、金币或掉落。

### 世界 BOSS 伤害排行

```text
GET /api/world-bosses/{bossId}/rankings
```

返回当前轮次前 10 名伤害排行。世界 BOSS 被击败后，系统按排行通过邮件发放奖励，并让该 BOSS 进入冷却。

## 1. 通用约定

接口统一以 `/api` 为前缀。

健康检查接口：

```text
GET /api/health
```

该接口不需要登录，用于部署、运维和 smoke 检查。

响应格式：

```json
{
  "success": true,
  "code": "OK",
  "message": "success",
  "data": {}
}
```

错误响应：

```json
{
  "success": false,
  "code": "AUTH_INVALID_PASSWORD",
  "message": "账号或密码错误",
  "data": null
}
```

登录后前端通过请求头传递 token：

```text
Authorization: Bearer <token>
```

## 2. 账号接口

### 2.1 注册

```text
POST /api/auth/register
```

请求：

```json
{
  "username": "player001",
  "password": "123456"
}
```

响应：

```json
{
  "accountId": 1
}
```

### 2.2 登录

```text
POST /api/auth/login
```

请求：

```json
{
  "username": "player001",
  "password": "123456"
}
```

响应：

```json
{
  "token": "login-token",
  "expiresAt": "2026-05-14T12:00:00"
}
```

### 2.3 当前账号

```text
GET /api/auth/me
```

响应：

```json
{
  "accountId": 1,
  "username": "player001",
  "characterCreated": true
}
```

### 2.4 修改密码

```text
POST /api/auth/change-password
```

请求头：

```text
Authorization: Bearer <token>
```

请求：

```json
{
  "oldPassword": "123456",
  "newPassword": "654321"
}
```

说明：

- 新密码长度沿用注册密码规则：6 到 32 位。
- 原密码错误返回 `AUTH_OLD_PASSWORD_INVALID`。
- 新密码与原密码相同返回 `AUTH_PASSWORD_UNCHANGED`。
- 修改成功后当前 token 保持可用。

## 3. 角色接口

### 3.1 创建角色

```text
POST /api/characters
```

请求：

```json
{
  "nickname": "无名刀客",
  "className": "warrior"
}
```

响应：

```json
{
  "characterId": 10001
}
```

### 3.2 查看角色

```text
GET /api/characters/me
```

响应：

```json
{
  "id": 10001,
  "nickname": "无名刀客",
  "className": "warrior",
  "level": 1,
  "exp": 0,
  "gold": 100,
  "hp": 120,
  "attack": 12,
  "defense": 4,
  "attackSpeed": 100,
  "power": 440,
  "currentMapId": "map_novice_field",
  "currentNodeId": "npc_village_chief",
  "lastX": 260,
  "lastY": 530
}
```

### 3.3 职业列表

```text
GET /api/characters/classes
```

返回可创建职业。当前包含战士、法师、道士；旧角色默认兼容战士。

## 3.4 技能接口

### 3.4.1 技能列表

```text
GET /api/skills
```

返回当前职业可学习技能、当前等级、学习/升级消耗、目标类型、触发概率、冷却、系数和描述。

v1.3 起响应补充：

- `skillSlot`：主动技能所在技能栏，`0` 表示不上栏。
- `materialCosts`：学习或升级所需材料。

### 3.4.2 学习技能

```text
POST /api/skills/learn
```

请求：

```json
{
  "skillId": "skill_power_slash"
}
```

### 3.4.3 升级技能

```text
POST /api/skills/upgrade
```

请求同学习技能。学习和升级都会消耗金币和配置材料；被动技能会刷新角色属性和战力。

### 3.4.4 配置技能栏

```text
POST /api/skills/slot
```

请求：

```json
{
  "skillId": "skill_power_slash",
  "skillSlot": 1
}
```

说明：`skillSlot` 范围为 0 到 4，0 表示移出技能栏；同一槽位被新技能占用时，旧技能会自动移出该槽位。

## 3.5 天赋接口

### 3.5.1 天赋列表

```text
GET /api/talents
```

返回总天赋点、已用点、可用点、重置消耗和天赋节点列表。

### 3.5.2 天赋加点

```text
POST /api/talents/upgrade
```

请求：

```json
{
  "talentId": "talent_sturdy_body"
}
```

### 3.5.3 重置天赋

```text
POST /api/talents/reset
```

说明：当前重置消耗 100 金币，重置后返还全部已用天赋点并刷新角色属性。

## 4. 地图接口

### 4.1 地图列表

```text
GET /api/maps
```

响应：

```json
[
  {
    "id": "map_novice_field",
    "name": "新手村外",
    "requiredLevel": 1,
    "recommendedPower": 0
  }
]
```

### 4.2 地图详情

```text
GET /api/maps/{mapId}
```

响应包含地图怪物、进入等级和推荐战力。

### 4.3 地图场景

```text
GET /api/maps/{mapId}/scene
```

响应包含地图基础信息、玩家当前位置、NPC 点位、地图事件点位和兼容怪物列表。

```json
{
  "id": "map_novice_field",
  "name": "新手村外",
  "requiredLevel": 1,
  "recommendedPower": 0,
  "width": 1600,
  "height": 1000,
  "backgroundSprite": "field_gray",
  "locked": false,
  "player": {
    "mapId": "map_novice_field",
    "nodeId": "npc_village_chief",
    "x": 260,
    "y": 530
  },
  "npcs": [
    {
      "id": "npc_village_chief",
      "name": "村长",
      "type": "npc",
      "x": 260,
      "y": 530,
      "sprite": "npc_chief",
      "locked": false,
      "statusText": "可对话",
      "taskIds": ["task_talk_chief"]
    }
  ],
  "events": [
    {
      "id": "event_novice_beast_area",
      "name": "野兽出没区",
      "type": "monster_area",
      "x": 840,
      "y": 420,
      "sprite": "wolf_area",
      "locked": false,
      "completed": false,
      "coolingDown": false,
      "statusText": "可交互",
      "nextAvailableAt": null,
      "targetMonsterIds": ["monster_chicken", "monster_deer", "monster_wolf"],
      "nextEventIds": ["event_novice_old_well"]
    }
  ],
  "monsters": []
}
```

说明：`nextEventIds` 用于前端绘制同地图点位连线；任务目标路径仍优先使用任务接口返回的 `targetMapId/targetPointId/targetPointType`。

点位类型：

- `npc`：NPC 对话点。
- `monster_area`：指定怪区，触发后由前端调用现有战斗接口。
- `random_encounter`：随机遇怪点，触发后返回一个怪物。
- `portal`：地图传送点。
- `reward`：奖励或探索点。

### 4.4 NPC 对话

```text
POST /api/npcs/{npcId}/talk
```

响应：

```json
{
  "npcId": "npc_village_chief",
  "npcName": "村长",
  "mapId": "map_novice_field",
  "dialogueLines": ["村外的动静越来越不对劲，先从附近的野兽查起。"],
  "taskIds": ["task_talk_chief", "task_first_blood"]
}
```

说明：对话会更新角色当前位置，并推进 `talk_npc` 类型任务。

### 4.5 地图事件触发

```text
POST /api/map-events/{eventId}/trigger
```

响应：

```json
{
  "eventId": "event_novice_old_well",
  "eventName": "废井",
  "action": "reward",
  "message": "井沿有新鲜抓痕，你从砖缝里找到几枚旧铜钱。获得 10 经验，12 金币，兽骨 x1。",
  "mapId": "map_novice_field",
  "expGained": 10,
  "goldGained": 12,
  "levelBefore": 1,
  "levelAfter": 1,
  "currentExp": 10,
  "currentGold": 112,
  "power": 440,
  "items": [
    {
      "itemId": "mat_beast_bone",
      "name": "兽骨",
      "quantity": 1
    }
  ]
}
```

`action` 说明：

- `battle`：返回 `mapId`、`monsterId`，前端继续调用 `POST /api/battles/start`。
- `portal`：返回 `targetMapId` 并更新角色地图位置。
- `reward`：返回 `expGained`、`goldGained`、`items`、等级与当前角色资产快照。
- `message`：仅返回探索文本。

说明：事件会更新角色当前位置，并推进 `explore_event` 类型任务。

事件状态说明：

- 非重复事件触发后写入 `map_event_states`，后续场景接口返回 `completed=true`、`statusText=已完成`。
- 带冷却事件触发后返回 `coolingDown=true`、`statusText=冷却中` 和 `nextAvailableAt`。
- `resetType=daily` 的每日事件触发后写入次日 00:00 作为 `nextAvailableAt`，次日自动恢复为可交互。
- 冷却中或已完成的一次性事件再次触发会返回业务错误。

v0.5.2 起，每张地图至少配置 1 个 `resetType=daily` 的奖励点。每日点仍使用 `reward` action，奖励结构与普通奖励事件一致。

## 5. 战斗接口

### 5.1 普通打怪兼容接口

```text
POST /api/battles/fight
```

说明：该接口为 v0.1/v0.2 一次性结算整场战斗的兼容接口。v0.3 在线战斗优先使用战斗会话接口。

请求：

```json
{
  "mapId": "map_novice_field",
  "monsterId": "monster_chicken"
}
```

响应：

```json
{
  "win": true,
  "rounds": 2,
  "expGained": 8,
  "goldGained": 5,
  "bonusExp": 1,
  "bonusGold": 1,
  "drops": [
    {
      "itemId": "item_wood_sword",
      "name": "木剑",
      "quantity": 1
    }
  ],
  "character": {
    "level": 1,
    "exp": 8,
    "gold": 105,
    "power": 440
  }
}
```

### 5.2 启动在线战斗

```text
POST /api/battles/start
```

请求：

```json
{
  "mapId": "map_novice_field",
  "monsterId": "monster_chicken"
}
```

响应：

```json
{
  "battleId": 90001,
  "status": "running",
  "round": 1,
  "nextActor": "player",
  "suggestedDelayMs": 1000,
  "player": {
    "hp": 120,
    "maxHp": 120,
    "attack": 12,
    "defense": 4,
    "attackSpeed": 100
  },
  "monster": {
    "id": "monster_chicken",
    "name": "小鸡",
    "hp": 30,
    "maxHp": 30,
    "attack": 4,
    "defense": 1,
    "attackSpeed": 80
  },
  "enemies": [
    {
      "id": "enemy_1",
      "monsterId": "monster_chicken",
      "name": "小鸡",
      "row": "front",
      "alive": true,
      "currentTarget": true,
      "hp": 30,
      "maxHp": 30,
      "attack": 4,
      "defense": 1,
      "attackSpeed": 80
    }
  ],
  "actions": []
}
```

v2.5 起在线战斗响应增加敌方阵列 `enemies`。普通单怪战斗也会返回 1 个敌方单位；自动遭遇会按数量返回多个敌方单位，而不是只展示 `怪物 x 数量` 的聚合名。

### 5.2.1 启动自动遭遇战斗

```text
POST /api/battles/encounter/start
```

请求：

```json
{
  "mapId": "map_novice_field",
  "eventId": "event_novice_beast_area"
}
```

说明：

- `eventId` 可选但推荐传入。传入后，后端会按地图事件配置读取遭遇数量范围、精英概率、休整间隔和 `encounterMonsters` 权重表。
- 自动遭遇会生成敌方阵列 `enemies`，数量由地图事件配置决定。
- 遭遇怪物死亡、选目标、技能命中和结算规则沿用在线战斗会话。

### 5.3 启动 BOSS 在线战斗

```text
POST /api/bosses/{bossId}/start
```

响应同普通在线战斗会话。BOSS 战斗结束胜利后进入刷新冷却，并按 `rewardMultiplier` 结算额外经验和金币。

### 5.4 推进下一次行动

```text
POST /api/battles/{battleId}/next
```

响应：

```json
{
  "battleId": 90001,
  "status": "running",
  "round": 1,
  "nextActor": "monster",
  "suggestedDelayMs": 1240,
  "action": {
    "actor": "player",
    "action": "attack",
    "targetType": "single",
    "target": "enemy_1",
    "damage": 11,
    "targetHpAfter": 19,
    "targets": [
      {
        "targetId": "enemy_1",
        "name": "小鸡",
        "damage": 11,
        "hpAfter": 19
      }
    ],
    "message": "你普通攻击小鸡，造成 11 点伤害。"
  },
  "player": {},
  "monster": {},
  "enemies": []
}
```

说明：

- v2.6 起 `POST /api/battles/{battleId}/next` 可选传入 `{ "targetId": "enemy_2" }`，普通攻击和单体技能优先命中该存活敌人；未传或目标已死亡时自动回落到第一个存活敌人。
- `POST /api/battles/{battleId}/skill` 同样可选传入 `targetId`。
- `targetType` 当前支持 `single`、`front_row`、`back_row`、`random3`、`all`。
- `targets` 是本次行动实际命中的目标列表，普通攻击通常只有 1 个目标，群攻技能可返回多个目标。
- 怪物回合中，多个存活敌人会各自生成行动日志，前端可按 `actor=monster` 和 `targets` 展示逐个攻击。

战斗结束时响应增加结算字段：

```json
{
  "battleId": 90001,
  "status": "finished",
  "win": true,
  "round": 3,
  "expGained": 8,
  "goldGained": 5,
  "drops": [],
  "character": {
    "level": 1,
    "exp": 8,
    "gold": 105,
    "power": 370
  }
}
```

### 5.4.1 手动释放技能

```text
POST /api/battles/{battleId}/skill
```

请求：

```json
{
  "skillId": "skill_power_slash"
}
```

说明：

- 只能在玩家行动回合释放。
- 技能必须已学习、为主动技能、已放入技能栏，且不在冷却中。
- 释放后同样推进一次玩家行动，并刷新技能冷却。
- 战斗响应会返回 `skills`，用于前端展示技能栏、槽位、冷却、目标类型和是否可释放。

### 5.5 查询在线战斗状态

```text
GET /api/battles/{battleId}
```

用于页面刷新后恢复战斗状态。响应包含战斗状态、双方当前 HP、下一行动方、已发生 actions 和是否已结算。

### 5.6 在线战斗 SSE 推送

```text
GET /api/battles/{battleId}/stream
```

说明：用于在线战斗页优先接收服务端推送。请求仍需要携带 `Authorization: Bearer <token>`。服务端会先推送当前 `state`，再按 `suggestedDelayMs` 自动推进战斗并推送 `action` 事件，战斗结束时推送 `finished` 事件后关闭连接。

事件示例：

```text
event: action
data: {"battleId":90001,"status":"running","round":1}
```

前端约束：

- SSE 流不可用或中断时，回退为调用 `POST /api/battles/{battleId}/next`。
- `next` 接口仍保留为兼容、回归测试和异常兜底。
- 当角色配置了技能栏时，前端会在玩家回合暂停自动推进，等待玩家手动释放技能或点击立即推进。

约束：

- 同一角色同一时间只允许一场 `running` 在线战斗。
- `next` 每次只推进一次行动，后端需要防重复请求。
- 奖励只允许在 `finished` 状态结算一次。
- 离线挂机不调用在线战斗会话接口。

## 6. 背包接口

### 6.1 背包列表

```text
GET /api/inventory
```

v0.6 起背包列表返回带容量的包装结构；前端按 `capacity` 渲染格子数量。

```json
{
  "capacity": 40,
  "usedSlots": 8,
  "remainingSlots": 32,
  "items": []
}
```

说明：

- `capacity` 当前默认 40 格。
- `usedSlots` 按背包行数计算，装备每件占 1 格，同类材料堆叠占 1 格。
- 材料入包时自动堆叠；装备入包时按数量拆成多件。
- 背包列表只返回未穿戴物品；已穿戴装备只通过 `GET /api/equipment` 展示。
- 背包容量、已用格和剩余格只按未穿戴物品计算。
- v2.4 起装备物品会返回 `setId`、`setName`，用于前端展示套装归属。
- v2.7 起装备物品会返回随机词条 `affixes`、词条后的 `hp`、`attack`、`defense`、`attackSpeed` 和 `skillTriggerBonus`。

### 6.2 出售物品

```text
POST /api/inventory/{inventoryItemId}/sell
```

请求：

```json
{
  "quantity": 1
}
```

### 6.3 丢弃物品

```text
POST /api/inventory/{inventoryItemId}/discard
```

请求：

```json
{
  "quantity": 1
}
```

规则：

- 材料和未穿戴装备可以丢弃。
- 已穿戴装备禁止丢弃。
- 丢弃数量不能超过当前拥有数量。
- 丢弃会写入 `drop_logs`，`source_type=discard`，数量为负数，便于排查。

### 6.4 批量出售材料

```text
POST /api/inventory/materials/sell
```

请求：

```json
{
  "items": [
    {
      "inventoryItemId": 50001,
      "quantity": 3
    }
  ]
}
```

规则：第一版只支持材料批量出售，装备批量出售后置，避免误卖。

### 6.5 使用消耗品

```text
POST /api/inventory/{inventoryItemId}/use
```

响应：

```json
{
  "inventoryItemId": 50001,
  "itemId": "consumable_attack_tonic",
  "name": "勇力药",
  "remainingQuantity": 2,
  "bonusHp": 0,
  "bonusAttack": 6,
  "bonusDefense": 0,
  "bonusAttackSpeed": 0
}
```

说明：当前消耗品用于下一场战斗准备，可叠加多瓶效果；战斗开始后一次性生效并清空准备状态。

## 7. 装备接口

### 7.1 已穿戴装备

```text
GET /api/equipment
```

响应包含角色当前装备属性、已穿戴装备列表和套装奖励列表：

```json
{
  "items": [
    {
      "inventoryItemId": 50001,
      "itemId": "item_redmoon_blade",
      "name": "赤月刃",
      "slot": "weapon",
      "setId": "set_redmoon",
      "setName": "赤月套装",
      "enhanceLevel": 0,
      "affixes": [
        {
          "stat": "attack",
          "value": 8
        }
      ],
      "skillTriggerBonus": 0,
      "hp": 18,
      "attack": 25,
      "defense": 2,
      "attackSpeed": 10
    }
  ],
  "setBonuses": [
    {
      "setId": "set_redmoon",
      "setName": "赤月套装",
      "pieces": 2,
      "requiredPieces": 2,
      "active": true,
      "hp": 40,
      "attack": 8,
      "defense": 0,
      "attackSpeed": 0
    }
  ],
  "hp": 220,
  "attack": 45,
  "defense": 18,
  "attackSpeed": 118,
  "power": 924
}
```

### 7.2 穿戴装备

```text
POST /api/equipment/equip
```

请求：

```json
{
  "inventoryItemId": 50001
}
```

### 7.3 卸下装备

```text
POST /api/equipment/unequip
```

请求：

```json
{
  "slot": "weapon"
}
```

说明：

- v0.6 起已穿戴装备不占用背包格，也不出现在背包列表。
- 卸下装备会把该装备放回背包；如果背包剩余空间不足，接口返回 `INVENTORY_FULL`，装备保持穿戴状态。

### 7.4 强化装备

```text
POST /api/equipment/enhance
```

请求：

```json
{
  "inventoryItemId": 50001
}
```

响应：

```json
{
  "inventoryItemId": 50001,
  "enhanceLevel": 1,
  "goldCost": 20,
  "materialCosts": [
    {
      "itemId": "mat_beast_bone",
      "name": "兽骨",
      "quantity": 1
    }
  ],
  "currentGold": 80,
  "hp": 130,
  "attack": 17,
  "defense": 5,
  "attackSpeed": 100,
  "power": 496
}
```

说明：

- v0.6 起强化消耗金币和材料。
- 当前内置规则：+1 消耗 `mat_beast_bone x1`，+2 消耗 `mat_iron_ore x2`，+3 消耗 `mat_iron_ore x3` 和 `mat_bone_shard x1`。
- v1.1 起强化消耗读取 `config/enhancement-rules.json`，当前支持 +1 到 +5，可按等级、品质和材料成本继续扩展。
- 材料不足返回 `EQUIPMENT_ENHANCE_MATERIAL_NOT_ENOUGH`。

### 7.5 重铸装备词条

```text
POST /api/equipment/reroll-affixes
```

请求：

```json
{
  "inventoryItemId": 50001
}
```

响应：

```json
{
  "inventoryItemId": 50001,
  "materialCost": {
    "itemId": "mat_common_essence",
    "name": "粗糙装备精华",
    "quantity": 1
  },
  "affixes": [
    {
      "stat": "hp",
      "value": 6
    }
  ],
  "skillTriggerBonus": 0,
  "hp": 16,
  "attack": 0,
  "defense": 2,
  "attackSpeed": 0,
  "power": 486
}
```

说明：

- 装备生成、战斗掉落、任务奖励、地图奖励、邮件附件和 GM 发放都会为非堆叠装备写入初始随机词条。
- 重铸消耗 `config/equipment-affixes.json` 中对应品质的装备精华，消耗不足返回 `EQUIPMENT_ENHANCE_MATERIAL_NOT_ENOUGH`。
- 当前词条支持 `hp`、`attack`、`defense`、`attackSpeed` 和 `skillTriggerBonus`。
- 已穿戴装备重铸后会立即重算角色属性；未穿戴装备只更新该装备的词条和展示属性。
- 重铸材料消耗会写入 `drop_logs`，`source_type=equipment_affix_reroll`，数量为负数。

### 7.6 分解装备

```text
POST /api/equipment/decompose
```

请求：

```json
{
  "inventoryItemId": 50001
}
```

响应：

```json
{
  "inventoryItemId": 50001,
  "itemId": "item_cloth_armor",
  "name": "布衣",
  "enhanceLevel": 0,
  "materials": [
    {
      "itemId": "mat_common_essence",
      "name": "粗糙装备精华",
      "quantity": 1
    }
  ]
}
```

说明：

- 仅允许分解背包中的未穿戴装备，已穿戴装备返回 `EQUIPMENT_DECOMPOSE_EQUIPPED`。
- 普通、精良、稀有、史诗/传说装备分别返还对应装备精华。
- 史诗和传说装备基础返还 2 个精华；其他品质基础返还 1 个精华。
- 强化等级会额外增加返还数量，例如 +3 普通装备返还 `1 + 3` 个粗糙装备精华。

## 8. 排行榜接口

### 8.1 等级榜

```text
GET /api/rankings/level?limit=20
```

### 8.2 战力榜

```text
GET /api/rankings/power?limit=20
```

### 8.3 财富榜

```text
GET /api/rankings/gold?limit=20
```

### 8.4 榜单快照

```text
GET /api/rankings/level/snapshot?limit=20
GET /api/rankings/power/snapshot?limit=20
GET /api/rankings/gold/snapshot?limit=20
```

响应：

```json
{
  "type": "level",
  "title": "等级榜",
  "limit": 20,
  "source": "database",
  "generatedAt": "2026-05-25T10:31:22",
  "nextRefreshAt": "2026-05-25T10:32:22",
  "refreshIntervalSeconds": 60,
  "secondsUntilRefresh": 60,
  "entries": [
    {
      "rank": 1,
      "characterId": 1,
      "nickname": "player001",
      "level": 10,
      "power": 1200,
      "gold": 5000,
      "value": 10
    }
  ]
}
```

说明：`source` 为 `database` 或 `cache`。旧数组接口继续保留，快照接口用于前端展示刷新元信息和后续运营化榜单扩展。

## 9. 活动接口

### 9.1 活动列表

```text
GET /api/activities
```

响应：

```json
[
  {
    "id": "activity_growth_sprint",
    "name": "新服成长冲刺",
    "type": "growth",
    "status": "active",
    "tag": "限时",
    "summary": "提升等级和战力，优先解锁高阶地图与世界 BOSS 参与门槛。",
    "description": "活动期间推荐优先完成主线任务、自动遭遇和装备重铸，快速冲刺 10 级内容。",
    "startAt": "2026-05-25 00:00",
    "endAt": "2026-06-01 00:00",
    "priority": 10,
    "targetView": "tasks",
    "rewardGold": 300,
    "rewardItems": [
      {
        "itemId": "consumable_hero_elixir",
        "name": "英雄秘药",
        "quantity": 1
      }
    ],
    "effects": [
      {
        "type": "battle_exp",
        "percent": 20,
        "description": "战斗经验 +20%"
      }
    ],
    "rankingRewards": [
      {
        "rankingType": "level",
        "maxRank": 10,
        "currentRank": 1,
        "eligible": true,
        "rewardGold": 500,
        "rewardItems": [
          {
            "itemId": "mat_rare_essence",
            "name": "稀有装备精华",
            "quantity": 1
          }
        ],
        "description": "等级榜前 10 名可领取阶段奖励"
      }
    ],
    "claimed": false,
    "claimable": true
  }
]
```

说明：活动状态支持 `active`、`upcoming`、`ended`。只有 `active` 且未领取、存在奖励的活动会返回 `claimable=true`。`effects` 为活动玩法加成，当前支持 `battle_exp`、`battle_gold`、`drop_rate`、`idle_exp`、`idle_gold` 和 `world_boss_gold`。`rankingRewards` 为榜单阶段奖励，当前支持 `level`、`power` 和 `gold` 三类榜单；`currentRank` 和 `eligible` 按当前角色实时计算。

### 9.2 领取活动奖励

```text
POST /api/activities/{activityId}/claim
```

响应：

```json
{
  "activityId": "activity_growth_sprint",
  "rankingType": null,
  "currentRank": 0,
  "goldGained": 300,
  "currentGold": 400,
  "items": [
    {
      "itemId": "consumable_hero_elixir",
      "name": "英雄秘药",
      "quantity": 1
    }
  ]
}
```

说明：领取会写入 `activity_claims`，同一角色同一活动只能领取一次；金币奖励写入 `currency_logs`，物品奖励写入背包和 `drop_logs`。背包空间不足时返回 `INVENTORY_FULL`。

## 10. 公会接口

### 10.1 公会列表

```text
GET /api/guilds?limit=20
```

返回按成员数排序的公会摘要，包含公会 ID、名称、公告、会长角色、成员数、公会总贡献和创建时间。

### 10.2 当前公会

```text
GET /api/guilds/me
```

未加入公会时返回 `inGuild=false`；已加入公会时返回公会详情、本人身份、个人贡献、公会总贡献、成员列表、每日捐献选项、公会商店商品和公会活动目标。

### 10.3 创建公会

```text
POST /api/guilds
```

请求：

```json
{
  "name": "烟火盟",
  "notice": "每日上线清日常"
}
```

规则：名称 2-12 个字符且唯一，公告最多 80 个字符，创建者自动成为会长。

### 10.4 加入公会

```text
POST /api/guilds/{guildId}/join
```

当前角色未加入公会时可加入目标公会。

### 10.5 退出或解散公会

```text
POST /api/guilds/leave
```

普通成员可直接退出；会长在成员数大于 1 时必须先转让会长；会长是最后一名成员时，退出会解散公会。

### 10.6 踢出成员

```text
POST /api/guilds/members/{characterId}/kick
```

仅会长可操作，不能踢出自己。

### 10.7 转让会长

```text
POST /api/guilds/members/{characterId}/transfer
```

仅会长可操作，目标必须是当前公会成员。

### 10.8 公会捐献

```text
POST /api/guilds/donate
```

请求：

```json
{
  "donationId": "small_gold"
}
```

说明：捐献选项来自 `guilds.json`。捐献会扣除金币，增加个人贡献和公会总贡献，并写入 `guild_donations`、`currency_logs` 和 `guild_logs`；同一捐献档位按配置限制每日次数。

### 10.9 公会商店

```text
GET /api/guilds/shop
```

返回当前角色可见的公会商店商品，包含商品 ID、道具、数量、贡献消耗、金币消耗、今日限购、已购买次数和 `canBuy`。

### 10.10 公会商店购买

```text
POST /api/guilds/shop/buy
```

请求：

```json
{
  "shopItemId": "guild_potion_pack"
}
```

说明：购买会校验已加入公会、个人贡献、金币、每日限购和背包容量；成功后扣除贡献和金币，发放道具，写入 `guild_shop_purchases`、`drop_logs`、`currency_logs` 和 `guild_logs`。

### 10.11 公会贡献排行

```text
GET /api/guilds/rankings?limit=20
```

返回按公会总贡献倒序、成员数倒序和公会 ID 正序排列的公会排行条目，包含名次、公会 ID、名称、会长、成员数、总贡献和 `mine` 标记。

### 10.12 公会活动

```text
GET /api/guilds/activities
```

返回当前角色所在公会的活动目标，包含活动 ID、名称、说明、标签、目标贡献、当前贡献、进度百分比、金币奖励、道具奖励、是否达成、是否已领取和是否可领取。活动配置来自 `guilds.json`。

### 10.13 公会活动奖励领取

```text
POST /api/guilds/activities/{activityId}/claim
```

说明：领取会校验已加入公会、公会总贡献是否达到活动目标、是否重复领取和背包容量；成功后发放金币和道具，写入 `guild_activity_claims`、`currency_logs`、`drop_logs` 和 `guild_logs`。同一角色在同一公会内同一活动只能领取一次。

## 11. 任务接口

### 11.1 任务列表

```text
GET /api/tasks
```

响应：

```json
[
  {
    "id": "task_first_blood",
    "name": "初试身手",
    "story": "初试身手的线索已经浮现，按目标推进主线。",
    "guide": "击败指定怪物达到数量。",
    "type": "kill_monster",
    "targetId": "monster_chicken",
    "targetName": "小鸡",
    "targetMapId": "map_novice_field",
    "targetPointId": "event_novice_beast_area",
    "targetPointType": "monster_area",
    "targetCount": 3,
    "targetLevel": 0,
    "currentCount": 1,
    "status": 0,
    "statusText": "进行中",
    "locked": false,
    "preTaskIds": [],
    "rewards": {
      "exp": 30,
      "gold": 20,
      "items": []
    }
  }
]
```

状态说明：

- `0`：进行中
- `1`：可领取
- `2`：已领取

补充说明：

- `locked=true` 表示前置任务未完成，前端可以显示锁定状态。
- v0.5 起任务类型支持 `kill_monster`、`level_reach`、`talk_npc`、`explore_event`。
- v0.5.11 起任务响应返回 `targetMapId`、`targetPointId`、`targetPointType`，用于地图页任务目标定位和点位高亮；等级任务等没有地图点位的任务可以为空。
- v1.5 起任务响应返回 `story` 和 `guide`，用于任务页与地图任务追踪展示剧情说明和行动提示。
- v2.8 起任务页基于 `status`、`locked`、`preTaskIds`、`story`、`guide` 和目标定位字段渲染推荐下一步、任务链总览和剧情回看；技能装配建议复用 `GET /api/skills`，不新增任务接口。

### 11.2 领取任务奖励

```text
POST /api/tasks/{taskId}/claim
```

响应：

```json
{
  "taskId": "task_first_blood",
  "expGained": 30,
  "goldGained": 20,
  "levelBefore": 1,
  "levelAfter": 2,
  "currentExp": 10,
  "currentGold": 120,
  "power": 270,
  "items": []
}
```

## 12. 挂机接口

### 12.1 挂机状态

```text
GET /api/idle/status
```

响应会返回 `estimatedBonusExp` 和 `estimatedBonusGold`，用于展示当前 active 活动对挂机预估收益的加成。

### 12.2 开始挂机

```text
POST /api/idle/start
```

请求：

```json
{
  "mapId": "map_novice_field",
  "monsterId": "monster_chicken"
}
```

### 12.3 领取挂机收益

```text
POST /api/idle/claim
```

响应会返回 `bonusExp` 和 `bonusGold`，表示本次挂机领取中来自活动效果的额外经验和金币。

## 13. BOSS 接口

### 13.1 BOSS 列表

```text
GET /api/bosses
```

### 13.2 启动 BOSS 在线战斗

```text
POST /api/bosses/{bossId}/start
```

推荐前端使用该接口进入会话式回合战斗。

### 13.3 挑战 BOSS 兼容接口

```text
POST /api/bosses/{bossId}/fight
```

说明：该接口保留 v0.2 一次性结算 BOSS 战斗能力。v0.3 在线体验优先使用 `POST /api/bosses/{bossId}/start`。

## 14. 管理接口

管理员账号通过 `game.admin.usernames` 配置，默认 `admin`。

### 14.1 角色列表

```text
GET /api/admin/characters?limit=20
```

响应补充在线状态：

```json
[
  {
    "id": 10001,
    "accountId": 1,
    "nickname": "无名刀客",
    "level": 3,
    "exp": 20,
    "gold": 120,
    "power": 520,
    "online": true,
    "lastActiveAt": "2026-05-15T16:50:00"
  }
]
```

### 14.2 发放金币

```text
POST /api/admin/grant-gold
```

请求：

```json
{
  "characterId": 10001,
  "gold": 100
}
```

### 14.3 发放物品

```text
POST /api/admin/grant-item
```

请求：

```json
{
  "characterId": 10001,
  "itemId": "item_wood_sword",
  "quantity": 1
}
```

### 14.4 发送邮件/补偿

```text
POST /api/admin/send-mail
```

请求：

```json
{
  "characterId": 10001,
  "characterIds": [10001, 10002],
  "all": false,
  "title": "维护补偿",
  "content": "感谢参与测试，发放少量补偿。",
  "gold": 100,
  "itemId": "item_wood_sword",
  "quantity": 1,
  "expiresAt": "2026-05-20T12:00"
}
```

说明：

- `characterId`、`characterIds`、`all=true` 三种目标方式任选一种；`all=true` 会给当前所有角色各生成一封个人邮件。
- `gold` 和 `itemId + quantity` 至少填写一类附件。
- 邮件只创建待领取附件，玩家领取后才真正写入角色金币、背包、金币流水和掉落日志。
- `expiresAt` 可选，支持 `yyyy-MM-ddTHH:mm` 或 `yyyy-MM-dd HH:mm:ss`，过期后不可领取附件。

### 14.5 查询地图事件状态

```text
GET /api/admin/map-event-states?characterId=10001
```

响应：

```json
[
  {
    "eventId": "event_novice_daily_cache",
    "eventName": "林地补给",
    "mapId": "map_novice_field",
    "mapName": "新手村外",
    "type": "reward",
    "resetType": "daily",
    "repeatable": true,
    "cooldownSeconds": 0,
    "triggerCount": 1,
    "lastTriggeredAt": "2026-05-19T09:30:00",
    "nextAvailableAt": "2026-05-20T00:00:00",
    "completed": false
  }
]
```

### 14.6 重置单个地图事件状态

```text
POST /api/admin/map-event-states/reset
```

请求：

```json
{
  "characterId": 10001,
  "eventId": "event_novice_daily_cache"
}
```

说明：删除该角色指定事件在 `map_event_states` 中的状态记录，并写入 `gm_logs`。

### 14.7 重置全部地图事件状态

```text
POST /api/admin/map-event-states/reset-all
```

请求：

```json
{
  "characterId": 10001
}
```

说明：删除该角色全部地图事件状态，并写入 `gm_logs`。用于测试、排障或运营调试。

### 14.8 清理过期地图事件状态

```text
POST /api/admin/map-event-states/cleanup
```

请求：

```json
{
  "characterId": 10001,
  "keepDays": 7
}
```

说明：删除该角色中已经超过保留天数、且不在冷却中的可重复事件状态；`completed=1` 的一次性完成状态不会被删除。操作写入 `gm_logs`。

### 14.9 封禁或解封账号

```text
POST /api/admin/account-status
```

请求：

```json
{
  "accountId": 1,
  "disabled": true
}
```

说明：管理员不能封禁当前登录的管理员账号；操作写入 GM 日志。

### 14.10 热重载配置

```text
POST /api/admin/config/reload
```

响应：

```json
{
  "success": true,
  "message": "配置重载成功",
  "summary": "maps=8, monsters=15, items=..."
}
```

说明：服务端会先解析并校验全部配置，校验失败时返回失败信息且不覆盖当前已生效配置。

### 14.11 GM 活动配置列表

```text
GET /api/admin/activities
```

响应为当前 `activities.json` 的活动配置列表，字段与玩家侧活动列表的基础配置一致，但不附带当前角色领取状态、当前名次或物品名称补全。

### 14.12 更新 GM 活动配置

```text
POST /api/admin/activities/{activityId}
```

请求：

```json
{
  "id": "activity_redmoon_hunt",
  "name": "赤月刷装周",
  "type": "drop",
  "status": "upcoming",
  "tag": "预告",
  "summary": "围绕赤月峡谷深处和赤月祭坛的高阶装备追求。",
  "description": "活动期间赤月地图掉落概率提升。",
  "startAt": "2026-06-01 00:00",
  "endAt": "2026-06-08 00:00",
  "priority": 8,
  "targetView": "maps",
  "rewardGold": 0,
  "rewardItems": [
    {
      "itemId": "mat_rare_essence",
      "quantity": 2
    }
  ],
  "effects": [
    {
      "type": "drop_rate",
      "percent": 30,
      "description": "战斗掉落概率 +30%"
    }
  ],
  "rankingRewards": []
}
```

响应：

```json
{
  "success": true,
  "message": "活动配置已保存并生效",
  "summary": "maps=10, monsters=19, items=38, ..."
}
```

说明：路径中的 `activityId` 为准，不能通过请求体修改活动 ID。保存前会复用完整配置校验，校验失败时保留旧内存配置；保存成功后写回 `activities.json`，同步当前运行内存快照，并写入 `gm_system_logs`。

## 15. 邮件接口

### 15.1 邮件列表

```text
GET /api/mails?limit=30
```

响应：

```json
[
  {
    "id": 1,
    "title": "维护补偿",
    "content": "感谢参与测试，发放少量补偿。",
    "attachmentGold": 100,
    "attachmentItemId": "item_wood_sword",
    "attachmentItemName": "木剑",
    "attachmentQuantity": 1,
    "status": 0,
    "claimable": true,
    "read": false,
    "expired": false,
    "expiresAt": "2026-05-20 12:00:00",
    "sourceType": "gm",
    "createdAt": "2026-05-15 18:05:00",
    "claimedAt": null
  }
]
```

### 15.2 领取邮件附件

```text
POST /api/mails/{mailId}/claim
```

响应：

```json
{
  "goldGained": 100,
  "itemId": "item_wood_sword",
  "itemName": "木剑",
  "quantity": 1,
  "currentGold": 220
}
```

### 15.3 标记邮件已读

```text
POST /api/mails/{mailId}/read
```

### 15.4 删除邮件

```text
POST /api/mails/{mailId}/delete
```

说明：未领取且未过期的附件邮件不能删除；需先领取附件，或等邮件过期后删除。
