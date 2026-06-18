# 优化摘要

完整历史记录已归档到 `doc/archive/20260520-doc-slimming/optimization-log.md`。

## 2026-06-17 v3.7 公会活动与公会排行

- 目标：在公会贡献与商店之后，补齐公会维度的轻量协作目标和竞争展示，让公会总贡献具备更明确的运营价值。
- 完成：`guilds.json` 新增公会活动目标配置，配置加载阶段校验活动 ID、名称、目标贡献、金币奖励和道具引用。
- 完成：新增 `guild_activity_claims`，用于记录同一角色在同一公会内同一活动的领取状态，老库通过 `DatabaseMigration` 自动建表。
- 完成：新增 `GET /api/guilds/rankings`、`GET /api/guilds/activities` 和 `POST /api/guilds/activities/{activityId}/claim`；排行按总贡献展示，活动按公会总贡献达标后发放金币和道具，并防重复领取。
- 完成：公会页展示公会活动目标、进度、奖励、领取按钮和公会贡献排行，捐献/购买/领取后刷新角色与公会状态。
- 完成：`smoke-test.js` 覆盖公会活动达标、排行条目、活动奖励领取、防重复领取和奖励入账。
- 验证：`node --check src/main/resources/static/app.js`、`node --check scripts/smoke-test.js`、`node scripts/combat-balance-report.js`、`mvn test` 和 `SMOKE_START_SERVER=1 SMOKE_PORT=18081 node scripts/smoke-test.js` 通过。
- 后续问题：`node scripts/regression-check.js --quick` 本轮在 Maven 资源复制阶段遇到本地 `target/classes/application-prod.yml` 文件锁；独立验证已通过，需关闭本地 IDE/占用句柄后重跑统一快速回归。

## 2026-06-17 v3.6 公会贡献与商店

- 目标：在公会基础闭环之后补齐轻量日常贡献和兑换入口，让公会关系链具备每日回访和资源消耗价值。
- 完成：新增 `guilds.json`，配置每日金币捐献档位和公会商店商品，配置加载阶段校验捐献、商品、道具引用、消耗和限购。
- 完成：新增 `guild_donations`、`guild_shop_purchases`，并为 `guilds` 补充 `total_contribution`；老库通过 `DatabaseMigration` 自动补列建表。
- 完成：新增 `POST /api/guilds/donate`、`GET /api/guilds/shop` 和 `POST /api/guilds/shop/buy`；捐献扣金币、加个人贡献和公会总贡献，商店购买校验贡献、金币、每日限购和背包容量后发奖。
- 完成：公会页展示总贡献、我的贡献、每日捐献选项和公会商店，购买/捐献后刷新角色与公会状态。
- 完成：`smoke-test.js` 覆盖公会捐献、每日次数、商店购买、贡献扣减和背包道具入账。
- 验证：`node --check game/src/main/resources/static/app.js`、`node --check game/scripts/smoke-test.js` 和 `mvn test` 通过。

## 2026-06-10 v3.5 公会基础闭环

- 目标：在活动和排行榜之后补齐轻量多人关系链，为后续公会贡献、公会商店、公会活动和公会排行打基础。
- 完成：新增 `guilds`、`guild_members`、`guild_logs` 三张表，并在 `DatabaseMigration` 中补齐老库迁移兜底。
- 完成：新增公会接口，覆盖公会列表、当前公会、创建、加入、退出/解散、踢出成员和转让会长。
- 完成：前端新增“公会”导航页，未入会时可创建或加入公会；已入会时展示公告、会长、成员数、成员列表和会长管理操作。
- 完成：`smoke-test.js` 增加公会状态机覆盖，包括创建、列表、加入、踢人、再次加入、转让、退出和最后解散。
- 验证：`node --check src/main/resources/static/app.js`、`node --check scripts/smoke-test.js` 和 `mvn test` 通过；`node scripts/regression-check.js --quick` 本轮在 Maven 资源复制阶段遇到 `target/classes/application-prod.yml` 访问拒绝，诊断显示本地 Java 进程占用 jar，关闭本地 Spring Boot/IDE Java 进程后需重跑统一回归。
- 后续问题：当时建议继续做每日捐献、贡献值、公会商店或公会活动；已分别在 v3.6 和 v3.7 补齐。

## 2026-06-03 v3.4 GM 活动编辑后台

- 目标：把运营活动从只读 JSON 配置推进到 GM 后台可编辑、可校验、可热生效的低风险运营工具。
- 完成：新增 `GET /api/admin/activities` 和 `POST /api/admin/activities/{activityId}`，管理员可查看并保存活动配置；路径活动 ID 优先生效，不允许通过请求体改 ID。
- 完成：`GameConfigService` 支持 `game.config.location` 读取路径，并新增活动配置保存能力；保存前复用完整配置校验，保存成功后写回 `activities.json`、同步运行内存快照并记录 `gm_system_logs`。
- 完成：前端 GM 页新增活动编辑区，支持编辑活动状态、类型、目标入口、名称、标签、优先级、时间、文案、直接奖励、玩法加成和榜单奖励。
- 完成：`smoke-test.js` 覆盖管理员读取活动、保存活动标签、确认生效并还原原配置。
- 验证：`node --check src\main\resources\static\app.js`、`node --check scripts\smoke-test.js`、`mvn test`、`SMOKE_START_SERVER=1 SMOKE_PORT=18081 node scripts\smoke-test.js` 和 `node scripts\combat-balance-report.js` 通过；数值体检错误 0、提醒 0。
- 后续问题：真实浏览器点击验收仍因当前环境缺少 Playwright/Chrome/Edge 执行器未完成；P2 玩法方向继续保留公会、拍卖行、PK 和排行榜 Redis Sorted Set/定时快照落库。

## 2026-05-27 v3.3 榜单阶段奖励

- 目标：把“排行榜试炼”从普通活动奖励推进到按榜单名次达成后领取阶段奖励，形成轻量竞争活动闭环。
- 完成：新增活动 `rankingRewards` 配置，支持榜单类型、名次门槛、金币奖励、物品奖励和描述。
- 完成：新增排行榜当前名次查询，按榜单字段倒序、角色 ID 正序计算当前角色名次。
- 完成：活动列表返回榜单奖励、当前名次和达成状态；前端活动页展示榜单阶段奖励。
- 完成：活动领取会校验榜单名次，达成后发放阶段奖励并复用 `activity_claims` 防重复领取。
- 完成：配置加载和数值体检脚本校验榜单奖励配置；smoke 覆盖榜单奖励元数据、资格和领取结果。
- 验证：`node --check src\main\resources\static\app.js`、`node --check scripts\smoke-test.js`、`node scripts\combat-balance-report.js`、`mvn test` 和自启动 smoke 已通过。
- 后续问题：公会、拍卖行和 PK 仍是后续方向；GM 活动编辑后台已在 v3.4 补齐。

## 2026-05-27 v3.2 运营活动效果系统

- 目标：把活动从“可领取奖励”推进到“可影响核心循环”，让运营配置能实际改变战斗、挂机和世界 BOSS 收益。
- 完成：新增活动 `effects` 配置和 `ActivityEffectService`，支持 `battle_exp`、`battle_gold`、`drop_rate`、`idle_exp`、`idle_gold`、`world_boss_gold` 六类效果。
- 完成：活动列表响应返回 `effects`；前端活动页展示玩法加成，战斗和挂机提示展示活动额外收益，世界 BOSS 列表展示邮件金币加成。
- 完成：战斗结算接入活动经验/金币加成，掉落判定接入掉落概率加成；挂机预估和领取接入活动经验/金币加成；世界 BOSS 击败后的排行邮件金币接入活动加成。
- 完成：配置加载和数值体检脚本校验活动效果类型与百分比；smoke 覆盖活动效果元数据和战斗 bonus 生效。
- 验证：`node --check src\main\resources\static\app.js`、`node --check scripts\smoke-test.js`、`node scripts\combat-balance-report.js` 和 `mvn test` 已通过；完整快速回归见本轮最终验证。
- 后续问题：当时榜单阶段奖励和 GM 活动编辑后台仍是后续运营方向；已分别在 v3.3 和 v3.4 补齐。

## 2026-05-25 v3.1 运营活动奖励领取

- 目标：把活动从只读宣发入口推进到最小奖励闭环，并保证同一角色不可重复领取。
- 完成：新增 `activity_claims` 表，按 `character_id + activity_id` 唯一约束保存领取状态、金币和物品 JSON 摘要。
- 完成：新增 `ActivityService`、`ActivityRepository`、`ActivityResponse` 和 `ActivityClaimResponse`；`GET /api/activities` 返回 `claimed`、`claimable` 和带名称的奖励预览。
- 完成：新增 `POST /api/activities/{activityId}/claim`，仅允许领取 `active` 活动，发放金币和物品，写入 `currency_logs`、`drop_logs`，并在发奖前检查背包容量。
- 完成：前端活动页展示“领取奖励”和“已领取”状态；领取后刷新角色和活动状态。
- 完成：`smoke-test.js` 覆盖活动领取、奖励内容、状态持久化和重复领取失败。
- 验证：`node scripts\regression-check.js --quick` 通过，覆盖前端 JS 语法检查、smoke 脚本语法检查、数值体检和 `mvn test`；完整回归见本轮最终验证。
- 后续问题：当时活动仍未接入周期性掉落加成、榜单阶段奖励或 GM 活动编辑后台；掉落和收益加成已在 v3.2 补齐，榜单阶段奖励和 GM 活动编辑后台已分别在 v3.3、v3.4 补齐。

## 2026-05-25 v3.0 运营活动配置入口

- 目标：继续推进 P2 多人与运营方向，先建立可热重载、低风险的活动宣发入口，为后续活动奖励和榜单赛季铺路。
- 完成：新增 `activities.json`，维护活动名称、类型、状态、时间、描述、奖励预览、优先级和目标页面。
- 完成：新增 `ActivityConfig`、`GET /api/activities` 和配置校验，校验活动状态、奖励物品引用、奖励数量和目标页面。
- 完成：前端新增“活动”导航和活动页，展示活动状态、类型、时间、说明、奖励预览，并支持跳转到目标页面。
- 完成：`smoke-test.js` 增加活动列表和活动元数据断言。
- 验证：`node scripts\regression-check.js --quick` 通过，覆盖前端 JS 语法检查、smoke 脚本语法检查、数值体检和 `mvn test`；数值体检错误 0、提醒 0。
- 后续问题：活动当前为只读宣发入口，后续可扩展领取状态、发奖、防重复领取、掉落加成和榜单阶段奖励。

## 2026-05-25 v2.9 排行榜快照元信息

- 目标：推进 P2 多人与运营方向的低风险第一步，让排行榜具备可运营的刷新元信息，同时保留旧接口兼容性。
- 完成：新增 `RankingSnapshotResponse` 和 `GET /api/rankings/level|power|gold/snapshot`，返回榜单类型、标题、来源、生成时间、下次刷新时间、刷新间隔和 entries。
- 完成：排行榜快照使用独立 Redis key `legend:ranking:snapshot:{type}:{limit}`；Redis 不可用时仍直接查询 SQLite 并返回数据库来源。
- 完成：前端排行榜页改用快照接口，展示“缓存快照/实时查询”、生成时间、刷新间隔和下次刷新时间；旧 `GET /api/rankings/level|power|gold` 数组接口继续保留。
- 完成：`smoke-test.js` 增加排行榜快照元数据断言。
- 验证：`node scripts\regression-check.js --quick` 和 `node scripts\regression-check.js` 均通过；完整回归覆盖自启动 smoke、`mvn test`、jar 锁诊断和 `mvn package -DskipTests`，数值体检错误 0、提醒 0。
- 后续问题：P2 后续可按玩家规模继续做运营活动、公会、拍卖行、PK，或将排行榜升级为 Redis Sorted Set/定时快照落库。

## 2026-05-25 P1.1 前台 image2 像素资产重制收尾

- 完成：执行 `powershell -NoProfile -ExecutionPolicy Bypass -File scripts\compress-generated-assets.ps1 -MaxBytes 51200`，将 `items/consumable_hero_elixir.png` 从约 1.25MB 压缩到 40,244 bytes。
- 完成：复核生成 PNG 总数 76、超 50KB 为 0，当前最大 PNG 为 47,330 bytes。
- 完成：补充长期规则，后续生成图片默认压缩到 50KB 以内；只有用户明确要求更高清、更大尺寸、保留原图等特殊场景时才突破该限制。
- 验证：`node scripts\regression-check.js --quick` 通过，覆盖前端 JS 语法检查、smoke 脚本语法检查、数值体检和 `mvn test`；数值体检错误 0、提醒 0。
- 后续问题：浏览器点击验收仍建议在下一轮 UI 调整或启动本地服务时顺带覆盖背包、装备、技能、地图和战斗入口；功能方向可进入 P2 多人与运营。

## 2026-05-22 P1.1 前台 image2 像素资产重制

- 目标：按用户自定义毛利图片生成接口，将前台人物图像、怪物图像、物品图标、技能图标和 NPC 头像统一重生为像素风、卡通风格资产。
- 已完成：新增 `game/scripts/generate-image2-assets.js`，按配置自动生成品牌、职业、怪物、物品、技能、NPC 资产；支持从本地自定义指令读取主/备用图片接口，只在内存中使用 key，并支持 `--skip-errors` 断点续跑。
- 已完成：新增 `game/scripts/compress-generated-assets.ps1`，把生成 PNG 重采样压缩到 50KB 以内；当日仍遗留 1 张英雄秘药图标待压缩，已在 2026-05-25 收尾。
- 已完成：已生成 76/76 张项目资产，包括 1 个品牌徽记、3 个职业头像、19 个怪物、38 个物品、12 个技能图标、3 个 NPC 头像。
- 已完成：前端已接入 `assets/generated/` 资产，职业预览、角色面板、怪物列表、战斗敌方阵列、地图点位详情、背包/装备、技能页和战斗技能按钮优先使用生成图片；未生成资产继续保留旧 SVG 或隐藏兜底。
- 已完成：`items/consumable_hero_elixir.png`（英雄秘药）已在 2026-05-25 压缩到 40,244 bytes。
- 验证：2026-05-25 已复核资产统计，当前总数 76、已生成 76、缺失 0、超 50KB 为 0；`node scripts\regression-check.js --quick` 已通过。

## 2026-05-22 P1 任务体验与新手引导

- 完成：任务页新增“推荐下一步”面板，按任务状态、目标位置和当前进度推荐领取奖励或前往目标；同时读取技能列表，提示可学习、可装配或可升级的技能。
- 完成：任务页新增任务链总览，按任务顺序展示已完成、可领取、进行中和锁定状态，并可点击切换剧情回看。
- 完成：任务页新增剧情回看区域，复用 `story`、`guide`、目标地图和奖励信息展示主线任务说明；任务列表补充“前往目标”和“回看剧情”入口。
- 完成：`smoke-test.js` 增加任务剧情、行动提示、目标定位和前置任务链断言，覆盖 `task_first_blood` 与 `task_first_weapon` 的关键引导元数据。
- 验证：`node --check src\main\resources\static\app.js`、`node --check scripts\smoke-test.js`、`node scripts\combat-balance-report.js`、`mvn test`、`node scripts\regression-check.js --quick`、`node scripts\regression-check.js` 通过；数值体检错误 0、提醒 0。
- 验收备注：Codex 内置浏览器访问本地 mock 页面时被安全策略阻止，本轮未完成浏览器点击验收；桌面端和移动端主流程仍建议人工点一遍任务页。
- 后续问题：下一步进入 P2 多人与运营方向，或继续细化新手分步教学与职业流派推荐。

## 2026-05-22 P0.2 装备随机词条与精华材料用途

- 完成：新增装备词条配置 `equipment-affixes.json`，装备生成、掉落、任务奖励、地图奖励、邮件附件和 GM 发放都会为装备写入随机词条。
- 完成：背包列表、装备列表和悬浮详情展示随机词条；生命、攻击、防御、攻速词条参与角色属性和战力计算，技能触发词条参与战斗技能触发概率。
- 完成：新增 `POST /api/equipment/reroll-affixes`，消耗对应品质装备精华重铸随机词条；分解产出的精华形成明确消耗入口。
- 完成：数值体检覆盖词条范围、重铸材料引用、装备掉落等级层级和品质掉率上限；smoke 覆盖 GM 发放装备生成词条、分解产出精华、重铸消耗精华和已穿戴装备词条展示。
- 验证：`node scripts/regression-check.js` 通过，覆盖前端 JS 语法检查、smoke 脚本语法检查、数值体检、`mvn test`、自启动 smoke、jar 锁诊断和 `mvn package -DskipTests`；数值体检错误 0、提醒 0。
- 后续问题：P1 任务剧情回看、任务链总览和新手引导已在同日完成，下一步转入 P2 多人与运营方向或继续细化新手分步教学。

## 2026-05-22 P0.1 前后排技能规则与职业联动

- 完成：新增 `back_row` 技能目标类型，后排技能优先命中存活后排；后排清空后回落到存活目标。
- 完成：法师火符术调整为后排技能；技能列表响应和前端技能页补充目标类型展示；战斗日志继续通过 `targetType` 和 `targets` 展示实际命中。
- 完成：配置校验和数值体检脚本支持 `back_row`；smoke 新增法师 2 级学习火符术、三单位遭遇中越过前排命中后排的断言。
- 完成：文档规则补充图片生成约束：遇到图片生成任务时优先执行用户个性化方案中的自定义指令。
- 验证：`node --check src/main/resources/static/app.js`、`node --check scripts/smoke-test.js`、`node --check scripts/combat-balance-report.js`、`mvn test`、`SMOKE_START_SERVER=1 SMOKE_PORT=18081 node scripts/smoke-test.js`、`node scripts/regression-check.js --quick`、`mvn package -DskipTests` 通过；数值体检错误 0、提醒 0。
- 后续问题：下一步进入 P0.2 装备随机词条与精华材料用途。

## 2026-05-22 文档事实校准与下一阶段计划

- 目标：减少核心文档中的历史版本噪音，让下一轮开发直接按当前事实开工。
- 完成：更新 README、PRD、API、架构、部署、规则和环境文档；PRD 尾部历史流水收敛为产品级下一阶段方向；API 增加当前接口总览、健康检查和自动遭遇接口说明；架构文档校准 HTTP/SSE、JSON 配置、Redis 边界和当前 schema 表清单；部署文档补齐生产环境变量与 Redis/SQLite 回退说明。
- 完成：将 `doc/roadmap.md` 的下一阶段建议细化为 P0.1 前后排技能规则、P0.2 装备随机词条、P1 任务体验和 P2 多人运营方向，并同步 `doc/development-checklist.md`。
- 验证：文档结构复核完成；本次未改业务代码，未运行代码回归。
- 后续问题：下一轮建议优先实现 P0.1 前后排技能规则与职业联动。

## 2026-05-21 v2.6 手动选目标

- 完成：`POST /api/battles/{battleId}/next` 和 `/skill` 支持可选 `targetId`，普通攻击和单体技能优先命中选中敌人。
- 完成：前端敌方阵列卡片支持点击选中，并在普通攻击和手动技能释放时带上目标。
- 完成：目标已死亡或未选择时自动回落到第一个存活敌人，群攻技能继续按 `targetType` 自动命中。
- 完成：smoke 增加选择第二个敌人后普通攻击必须命中该目标的断言。
- 验证：`node scripts/regression-check.js --quick` 通过。
- 后续问题：下一步建议补前后排技能规则和更多职业技能联动。

## 2026-05-21 v2.5 实时战斗敌方阵列

- 完成：在线战斗响应新增 `enemies` 敌方阵列，自动遭遇按数量生成多个敌方单位，不再只依赖 `怪物 x 数量` 聚合展示。
- 完成：行动日志新增 `targetType` 和 `targets`，普通攻击命中当前单体目标，群体技能可展示多个实际命中目标。
- 完成：技能配置新增 `targetType`，当前支持 `single`、`front_row`、`random3`、`all`；雷光术接入随机多体，星火燎原接入全体群攻。
- 完成：前端战斗面板升级为我方状态 + 敌方阵列 + 结构化行动日志，展示前排/后排、当前目标、存活数和命中明细。
- 完成：smoke 回归增加敌方阵列与行动命中目标断言。
- 验证：`node scripts/regression-check.js --quick` 和 `node scripts/regression-check.js` 通过；全量回归覆盖自启动 smoke、Maven 测试、打包与 jar 锁诊断。
- 后续问题：下一步建议增加手动选目标、前后排技能规则和更多职业技能联动。

## 2026-05-21 v2.4 装备分解与套装闭环

- 完成：新增装备分解接口，未穿戴装备可分解为对应品质装备精华，强化等级会提高返还数量，并写入掉落日志。
- 完成：新增粗糙、精良、稀有、史诗装备精华材料；赤月高阶装备接入赤月套装 2 件和 4 件奖励。
- 完成：角色属性重算纳入已激活套装奖励；装备页展示套装件数与激活属性，背包详情和悬浮窗展示套装归属。
- 完成：smoke 回归覆盖 GM 发装备、穿戴后背包移除和装备分解精华入包；数值体检新增套装与分解精华配置检查。
- 验证：`node scripts/regression-check.js --quick` 和 `node scripts/regression-check.js` 通过；全量回归覆盖自启动 smoke、Maven 测试、打包与 jar 锁诊断。
- 后续问题：下一步建议推进装备随机词条、精华材料用途和更多掉落层级。

## 2026-05-21 v2.3 固定回归流程

- 完成：新增 `scripts/regression-check.js`，统一执行前端 JS 语法检查、smoke 脚本语法检查、数值体检脚本语法检查、配置与数值体检、`mvn test`、自启动 smoke、打包前后 jar 锁诊断和 `mvn package -DskipTests`。
- 完成：支持 `--quick` 跳过 smoke 与打包，支持 `--skip-smoke` 和 `--skip-package` 分别跳过对应阶段；可用 `REGRESSION_SMOKE_PORT` 指定 smoke 端口。
- 完成：更新 `game/README.md`，将早期工作区说明改为当前技术栈、目录结构和统一回归命令。
- 验证：`node scripts/regression-check.js --quick` 和 `node scripts/regression-check.js` 通过；全量回归覆盖自启动 smoke、数值体检、Maven 测试、打包与 jar 锁诊断。
- 后续问题：下一步建议扩展装备词条、套装、更多掉落层级和材料用途。

## 2026-05-21 v2.2 地图导航体验增强

- 完成：地图事件点位下发 `nextEventIds`，前端根据点位关系绘制地图连线。
- 完成：地图页新增任务目标路径线、选中点位路径线、小地图和按地图类型区分的轻量场景层次。
- 完成：静态资源版本号更新到 `20260521-v22-map-navigation`。
- 验证：`node --check src/main/resources/static/app.js`、`node scripts/combat-balance-report.js`、`mvn test`、`SMOKE_START_SERVER=1 SMOKE_PORT=18081 node scripts/smoke-test.js`、`mvn package -DskipTests` 通过；浏览器点击验证当时未完成，已转为后续人工验收项。
- 后续问题：固定回归流程已在 v2.3 落地，下一步建议推进装备词条、套装和材料用途扩展。

## 2026-05-20 文档提纯精简

- 目标：减少 `doc/` 主目录噪音，让日常维护只看核心文档。
- 完成：历史方案、需求深挖、视觉稿和长日志已移动到 `doc/archive/20260520-doc-slimming/`；新增 `README.md`、`roadmap.md`；重建精简版 `development-checklist.md` 和 `optimization-log.md`。
- 影响：不改业务代码。

## 2026-05-20 v1.7 自动化回归与数值体检

- 完成：新增 `scripts/smoke-test.js`，可自启动临时 Spring Boot 服务和临时 SQLite 库，覆盖注册、登录、建角、地图场景、任务、技能、天赋、排行榜、战斗、GM 配置重载、GM 发装备、穿戴后背包移除。
- 完成：新增 `scripts/package-diagnostics.ps1`，用于检查目标 jar 是否被占用并列出 Java 进程，不自动结束进程。
- 完成：扩展 `scripts/combat-balance-report.js`，除职业对怪物战斗模拟外，新增掉落、强化材料、天赋前置、地图事件引用、装备档位、消耗品和 1-10 级节奏检查。
- 验证：`node --check scripts/smoke-test.js`、`node --check scripts/combat-balance-report.js`、`SMOKE_START_SERVER=1 SMOKE_PORT=18081 node scripts/smoke-test.js`、`node scripts/combat-balance-report.js`、`mvn test`、`mvn package -DskipTests` 通过；数值体检仅提示 9-10 级暂无同级普通怪。

## 2026-05-20 v1.8 9-10 级内容承接

- 完成：新增红月峡谷深处、赤月祭坛 2 张地图。
- 完成：新增红月守卫、深渊法徒、祭坛骑士、赤月祭司 4 个 9-10 级怪物。
- 完成：新增红月战甲、深渊头盔、魂火手镯、赤月魔戒、深渊余烬、赤月魔核、英雄秘药及对应像素图标。
- 完成：新增 9-10 级掉落、地图事件、传送点、每日采集点和主线任务承接。
- 完成：数值体检增加任务前置、任务目标和任务奖励引用检查。
- 验证：`node scripts/combat-balance-report.js` 错误 0、提醒 0；`mvn test`、`SMOKE_START_SERVER=1 SMOKE_PORT=18081 node scripts/smoke-test.js`、`mvn package -DskipTests` 通过。

## 2026-05-20 v1.9 自动随机遭遇

- 完成：新增 `/api/battles/encounter/start`，支持按地图与怪物范围随机选择怪物，随机 1-5 只，按概率出现精英遭遇。
- 完成：遭遇战以一个战斗单位呈现，例如 `Elite 鸡 x3`，血量、攻击、防御、攻速和奖励按数量/精英倍率缩放。
- 完成：地图怪区详情新增 Auto hunt，战斗结束后间隔倒计时自动开始下一场，可随时停止。
- 完成：smoke 回归加入随机遭遇覆盖。
- 验证：`node --check src/main/resources/static/app.js`、`node --check scripts/smoke-test.js`、`mvn test`、`SMOKE_START_SERVER=1 SMOKE_PORT=18081 node scripts/smoke-test.js`、`node scripts/combat-balance-report.js`、`mvn package -DskipTests` 通过。

## 2026-05-20 v2.0 遭遇参数配置化

- 完成：地图事件新增 `encounterMinCount`、`encounterMaxCount`、`encounterEliteChance`、`encounterIntervalSeconds`。
- 完成：后端地图场景接口下发遭遇参数，前端 Auto hunt 不再写死数量、精英概率和休整间隔。
- 完成：新手、矿洞、毒蛇、骷髅、红月深处、赤月祭坛分别配置不同遭遇强度。
- 完成：配置加载校验与数值体检脚本新增遭遇参数范围检查。
- 验证：`node --check src/main/resources/static/app.js`、`node --check scripts/combat-balance-report.js`、`node scripts/combat-balance-report.js`、`mvn test`、`SMOKE_START_SERVER=1 SMOKE_PORT=18081 node scripts/smoke-test.js`、`mvn package -DskipTests` 通过。

## 2026-05-20 v2.1 怪物权重表

- 完成：地图事件新增 `encounterMonsters` 权重表，自动遭遇优先按事件权重抽取怪物。
- 完成：`/api/battles/encounter/start` 支持接收 `eventId`，后端按事件校验地图归属、读取数量/精英/间隔配置，并保留旧版怪物列表兜底。
- 完成：地图场景接口下发权重表，前端 Auto hunt 发起遭遇时携带点位事件 ID。
- 完成：配置加载和数值体检脚本新增权重合法性、怪物存在性、怪物地图归属和 `???` 文案占位检查。
- 完成：修复 9-10 级地图、怪物、物品、任务和地图事件中的问号占位文案。
- 验证：`node --check src/main/resources/static/app.js`、`node --check scripts/combat-balance-report.js`、`node scripts/combat-balance-report.js`、`mvn test`、`SMOKE_START_SERVER=1 SMOKE_PORT=18081 node scripts/smoke-test.js`、`mvn package -DskipTests`、`powershell -NoProfile -ExecutionPolicy Bypass -File scripts/package-diagnostics.ps1` 通过。

## 2026-05-20 v1.6 地图点位详情浮层与行动反馈

- 完成：地图点位改为图标 + 标签式标记；点击点位先打开详情浮层；浮层展示状态、描述、任务目标、冷却、目标地图和可能怪物；浮层按钮再执行对话、探索、传送或战斗；任务定位会同步打开目标详情；角色移动增加方向、行走和到达反馈。
- 验证：`node --check src/main/resources/static/app.js` 通过；`mvn test` 通过；`mvn package -DskipTests` 因本地 jar 占用在 repackage 阶段失败。

## 2026-05-20 v0.9-v1.5 成长与运营增强

- 完成：GM 账号封禁/解封、配置热重载、配置校验增强、装备扩容、强化配置化、消耗品战斗准备、技能材料、技能栏手动施法、天赋分支、任务剧情与行动提示。
- 验证：`node --check src/main/resources/static/app.js`、`mvn test`、`node scripts/combat-balance-report.js` 通过。

## 2026-05-20 v0.7-v0.8 职业、技能与天赋

- 完成：战士、法师、道士三职业；主动/被动技能；技能学习与升级；在线战斗技能触发；天赋点、天赋加点、重置和属性加成。

## 2026-05-20 v0.6 背包增强

- 完成：格子式背包、像素道具图标、容量、材料堆叠、丢弃、材料出售、强化材料消耗、满包保护、装备悬浮窗对比、已穿戴装备从背包隐藏。

## 2026-05-19 至 2026-05-20 v0.5 地图与任务增强

- 完成：地图 NPC、地图事件、每日探索、事件状态管理、点位聚合、缩放拖拽、任务追踪、目标高亮、跨地图任务目标、任务筛选与剧情引导。

## 2026-05-15 至 2026-05-18 基础系统

- 完成：MVP、任务、挂机、BOSS、GM、会话式战斗、Redis 临时状态、排行榜缓存、邮件补偿、SSE 战斗推送、世界 BOSS。
