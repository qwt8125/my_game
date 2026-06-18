# 开发清单

## 当前完成

- [x] 账号、登录、修改密码、角色创建。
- [x] 地图、怪物、NPC、地图事件、任务追踪和点位详情浮层。
- [x] 会话式战斗、SSE 推送、挂机、BOSS、世界 BOSS。
- [x] 背包、装备、格子式 UI、像素图标、容量、丢弃、材料消耗、满包保护。
- [x] 职业、技能、技能栏、手动施法、天赋分支。
- [x] 邮件、补偿、GM 发放、账号封禁/解封、配置热重载。
- [x] 核心文档精简：历史方案已归档，主目录保留长期维护文档。
- [x] 自动化 smoke 回归、打包锁诊断、配置与数值体检脚本。
- [x] 9-10 级内容承接：红月峡谷深处、赤月祭坛、怪物、掉落、任务、地图事件和道具图标。
- [x] 在线战斗自动随机遭遇：怪物范围随机、数量随机、精英遭遇、战斗结束后间隔续战。
- [x] 自动遭遇参数配置化：数量范围、精英概率、休整间隔按地图事件维护。
- [x] 自动遭遇权重配置化：同一点位内不同怪物出现概率按 `encounterMonsters` 维护。
- [x] 配置体检增强：检查遭遇权重、怪物地图归属和中文文案占位。
- [x] 地图导航体验增强：场景层次、点位连线、任务目标路径和小地图。
- [x] 固定回归流程：`scripts/regression-check.js` 统一执行静态检查、数值体检、Maven 测试、自启动 smoke、打包和 jar 锁诊断。
- [x] 装备经济基础闭环：装备分解、装备精华材料、赤月套装奖励和套装属性战力计算。
- [x] 实时战斗面板升级：敌方阵列、行动命中目标、单体/多体/群体技能展示。
- [x] 手动选目标：敌方单位可点击选中，普通攻击和单体技能优先命中选中目标。
- [x] 前后排技能规则与职业联动：新增 `back_row` 目标类型，法师火符术可越过前排命中后排，技能页和战斗日志展示目标类型。
- [x] 装备随机词条与精华重铸：装备生成写入随机词条，背包/装备页展示词条，装备精华可用于重铸，词条属性参与战力和技能触发。
- [x] 任务体验与新手引导：任务页推荐下一步、任务链总览、剧情回看、技能装配建议和前往目标入口。
- [x] 前台 image2 像素资产重制：已生成 76/76 张，全部 PNG 已压缩到 50KB 内；后续生成图片默认按 50KB 内落地，除非用户另有特殊要求。
- [x] P2 排行榜快照元信息：新增榜单快照接口和前端刷新信息展示，保留旧排行榜数组接口兼容已有调用。
- [x] P2 运营活动配置入口：新增活动 JSON 配置、活动列表接口和前端活动页，支持状态、时间、奖励预览和目标入口。
- [x] P2 运营活动奖励领取：新增 `activity_claims`、领取接口、前端领取状态、金币流水、掉落日志和防重复领取校验。
- [x] P2 运营活动效果系统：活动 `effects` 支持战斗经验/金币、掉落概率、挂机经验/金币和世界 BOSS 邮件金币加成，前端展示玩法加成。
- [x] P2 榜单阶段奖励：活动 `rankingRewards` 支持榜单类型、名次门槛和阶段奖励，活动页展示当前名次和达成状态，领取复用 `activity_claims` 防重复。
- [x] P2 GM 活动编辑后台：新增管理员活动配置列表与保存接口，GM 页可编辑活动状态、时间、文案、奖励、加成和榜单奖励，保存前校验并写回 `activities.json`。
- [x] P2 公会基础闭环：新增公会、成员和日志表；玩家可创建、加入、退出公会，会长可踢人和转让会长，前端新增公会页。
- [x] P2 公会贡献与商店：新增 `guilds.json`、每日捐献、个人贡献、公会总贡献、公会商店、每日限购、贡献/金币消耗和背包发奖。
- [x] P2.1 公会活动与公会排行：新增公会贡献排行榜、`guilds.json` 公会活动目标、`guild_activity_claims` 领取状态、前端公会页展示和 smoke 覆盖。

## 当前验证

- [x] `node --check src/main/resources/static/app.js`
- [x] `node --check scripts/smoke-test.js`
- [x] `node --check scripts/combat-balance-report.js`
- [x] `node scripts/combat-balance-report.js`：错误 0，提醒 0 条。
- [x] `SMOKE_START_SERVER=1 SMOKE_PORT=18081 node scripts/smoke-test.js`：覆盖注册、登录、建角、地图、任务剧情/引导/前置链元数据、技能、天赋、运营活动元数据、活动奖励领取与防重复领取、活动战斗加成、榜单阶段奖励、排行榜快照元数据、战斗、随机遭遇敌方阵列、手动选目标、行动命中目标、法师后排技能、GM 配置重载、GM 活动编辑保存/还原、GM 发装备、随机词条生成、穿戴后背包移除、装备分解和词条重铸。
- [x] `mvn test`
- [x] `mvn package -DskipTests`
- [x] `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/package-diagnostics.ps1`
- [x] `node scripts/regression-check.js --quick`
- [x] `node scripts/regression-check.js`
- [x] 2026-06-10 v3.5 单项验证：`node --check src/main/resources/static/app.js`、`node --check scripts/smoke-test.js`、`mvn test`。
- [x] 2026-06-17 v3.6 单项验证：`node --check src/main/resources/static/app.js`、`node --check scripts/smoke-test.js`、`mvn test`。
- [x] 2026-06-17 v3.7 单项验证：`node --check src/main/resources/static/app.js`、`node --check scripts/smoke-test.js`、`node scripts/combat-balance-report.js`、`mvn test`、`SMOKE_START_SERVER=1 SMOKE_PORT=18081 node scripts/smoke-test.js`。
- [x] image2 资产统计：总数 76、已生成 76、缺失 0、超 50KB 为 0；最大 PNG 47,330 bytes，`consumable_hero_elixir.png` 40,244 bytes。
- [ ] 2026-06-17 v3.7 统一快速回归：`node scripts/regression-check.js --quick` 本轮在 Maven 资源复制阶段遇到本地 `target/classes/application-prod.yml` 访问拒绝；独立 `mvn test` 和自启动 smoke 已通过，`package-diagnostics.ps1` 显示 jar 写锁仍存在但未发现 Java 进程，需关闭本地 IDE/占用句柄后重跑。
- [ ] 前端浏览器点击验证：Codex 内置浏览器本轮访问本地 mock 页面被安全策略阻止；作为人工验收项，重点验证任务页推荐卡、任务链、剧情回看、小地图、路径线、点位详情、移动端布局和战斗入口。

## 下一步

- [x] P0.1 前后排技能规则与职业联动：补配置、战斗目标规则、前端日志展示和 smoke 覆盖。
- [x] P0.2 装备随机词条与精华材料用途：补词条生成、展示、消耗入口、掉落层级和数值体检。
- [x] P1 任务剧情回看、任务链总览和新手引导：补页面入口、状态文案、推荐技能装配和 smoke 元数据验收。
- [x] P1.1 前台 image2 像素资产重制收尾：已压缩 `consumable_hero_elixir.png` 到 50KB 内，并通过 `node scripts/regression-check.js --quick`。
- [x] P2 多人与运营方向第一项：排行榜快照元信息。
- [x] P2 运营活动配置入口：活动配置、接口、前端活动页和 smoke 覆盖。
- [x] P2 运营活动奖励领取：领取状态、防重复领取、奖励发放和 smoke 覆盖。
- [x] P2 运营活动效果系统：活动加成配置、活动页展示、战斗/挂机/世界 BOSS 结算接入和 smoke 覆盖。
- [x] P2 榜单阶段奖励：榜单名次计算、活动页展示、达成校验、奖励发放和 smoke 覆盖。
- [x] P2 GM 活动编辑后台：管理员活动配置列表、保存写回、配置校验、GM 页编辑区和 smoke 保存/还原覆盖。
- [x] P2 公会基础闭环：公会创建、加入、退出、踢人、转让会长、解散和 smoke 状态机覆盖。
- [x] P2 公会贡献与商店：捐献配置、贡献累计、商店配置、购买校验、前端公会页和 smoke 覆盖。
- [x] P2.1 公会活动与公会排行：公会贡献排行榜、活动目标配置、达标领取、防重复领取、前端展示和 smoke 覆盖。
- [ ] P2 后续方向：按玩家规模选择更高阶公会商店、拍卖行、PK 或排行榜 Redis Sorted Set/定时快照落库。

## 文档规则

- 主目录只维护核心文档。
- 阶段计划、长日志、一次性分析和视觉稿放到 `archive/`。
- 新功能落地后同步更新 `PRD.md`、`roadmap.md`、本清单和 `optimization-log.md`。
- 后续生成图片默认控制在 50KB 以内；只有用户明确要求更高清、更大尺寸、保留原图等特殊场景时才突破该限制。
