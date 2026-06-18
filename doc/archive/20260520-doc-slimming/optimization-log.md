# 优化记录

本文件用于记录每次功能优化、体验优化和文档优化的闭环情况。每次优化完成后必须追加一条记录，并同步检查是否需要更新 `doc/PRD.md` 与 `doc/development-checklist.md`。

## 记录模板

```markdown
## YYYY-MM-DD 优化标题

- 优化目标：
- 影响范围：
- 方案文档：
- 完成内容：
- 验证结果：
- PRD 更新：
- 后续问题：
```

## 2026-05-20 v1.6 地图点位详情浮层与行动反馈

- 优化目标：解决地图点位像普通按钮、点击即触发导致预期不清的问题，让玩家先看清点位含义、任务关联和行动结果，再执行交互。
- 影响范围：地图页点位渲染、点位详情浮层、任务目标定位、角色移动反馈、移动端地图浮层样式、静态资源版本号、`doc/map-interaction-optimization-directions-20260520.md`、`doc/PRD.md`、`doc/development-checklist.md`；本轮不新增后端 API。
- 方案文档：新增 `doc/map-interaction-optimization-directions-20260520.md`。
- 完成内容：地图点位改为图标 + 标签式标记；NPC、怪区、探索、奖励和传送点使用不同像素风图标样式；点击点位只选中并打开详情浮层，浮层展示名称、类型、状态、描述、任务目标、冷却、目标地图和可能怪物；浮层按钮再执行对话、探索、传送或进入战斗；任务查看目标和跨地图定位会同步打开目标详情；执行动作时显示前往中状态，角色移动增加方向、行走节奏和到达反馈；移动端浮层做尺寸收缩和边缘避让；静态资源版本号更新为 `20260520-v0520-map-detail`。
- 验证结果：`node --check src/main/resources/static/app.js` 通过；`mvn test` 通过；`mvn package -DskipTests` 在 Spring Boot repackage 阶段失败，原因是 `target/legend-game-0.1.0-SNAPSHOT.jar` 被本地进程占用，无法重命名为 `.jar.original`。
- PRD 更新：已同步 v1.6 地图点位详情浮层与行动反馈能力。
- 后续问题：后续可继续做场景底图层次、点位连线、任务路径高亮、小地图和战斗入口准备面板。

## 2026-05-20 v0.9-v1.5 成长与运营增强连续落地

- 优化目标：按 v0.8 后 backlog 的推荐顺序，连续补齐 GM 运营控制、装备内容密度、强化配置化、消耗品战斗准备、技能栏手动施法、天赋分支和任务剧情提示。
- 影响范围：GM 后台、配置加载与校验、物品/掉落/强化/技能/天赋/任务配置、背包、战斗会话、技能服务、数据库 schema 与迁移、前端背包/技能/天赋/战斗/任务页面、`doc/PRD.md`、`doc/api.md`、`doc/game-rules.md`、`doc/development-checklist.md`。
- 方案文档：新增 `doc/v0.9-v1.5-growth-operations-implementation.md`，并沿用 `doc/requirements-optimization-backlog-20260520-after-v08.md` 的执行顺序。
- 完成内容：GM 支持账号封禁/解封和配置热重载；配置重载改为校验快照后原子替换；装备扩容并补齐图标和掉落分层；强化规则抽到 `enhancement-rules.json`；新增消耗品药水和下一场战斗准备；技能支持材料消耗、1-4 号技能栏和玩家回合手动施法；天赋扩展为生存/输出/机动三分支；任务配置新增剧情说明和行动提示；新增 `scripts/combat-balance-report.js` 数值回归报告。
- 验证结果：`node --check src/main/resources/static/app.js` 通过；`mvn test` 通过；`node scripts/combat-balance-report.js` 通过，当前三职业 1-8 级同等级怪物模拟均为 OK。
- PRD 更新：已同步 v0.9-v1.5 当前完成状态、背包消耗品、GM 配置重载、技能栏手动施法、天赋分支和任务剧情提示。
- 后续问题：后续可继续把数值报告扩展到装备档位、强化等级、天赋加点流派和药水组合。

## 2026-05-20 v0.7/v0.7.1/v0.8 职业、技能与天赋系统落地

- 优化目标：在格子式背包和装备展示规则稳定后，补齐职业、技能、天赋三条成长主线，让战斗从普通攻击循环升级为带职业差异、技能触发和长期加点规划的复古文字页游体验。
- 影响范围：职业配置、技能配置、天赋配置、角色创建、角色属性重算、在线战斗会话、战斗结算、数据库 schema 与迁移、前端职业选择、技能页、天赋页、`doc/api.md`、`doc/game-rules.md`、`doc/PRD.md`、`doc/development-checklist.md`。
- 方案文档：新增并落地 `doc/v0.7-skill-system-plan.md`、`doc/v0.7.1-talent-system-plan.md`、`doc/v0.8-class-system-plan.md`。
- 完成内容：新增战士、法师、道士三职业配置，旧战士成长保持兼容；新增 12 个原创复古风技能，支持主动/被动、学习、升级、金币消耗、触发概率、冷却、破防、持续伤害、治疗和被动属性加成；新增 6 个通用天赋，支持等级派生天赋点、前置条件、加点、重置、属性加成、技能触发加成和金币收益加成；在线战斗会话保存技能临时状态，战斗行动会展示技能触发文案；前端新增职业选择、技能页和天赋页。
- 验证结果：`node --check game/src/main/resources/static/app.js` 通过；`mvn test` 通过；`mvn package -DskipTests` 通过；本地前台启动验证通过，日志确认 Tomcat 监听 8080 且成功加载 maps=8、monsters=15、classes=3、skills=12、talents=6。当前工具环境无法稳定保留后台子进程，未完成浏览器页面点击验证。
- PRD 更新：已同步职业、技能、天赋当前完成状态，更新页面规划、SQLite/配置存储范围、Redis 战斗技能状态说明和后续路线。
- 后续问题：后续可继续扩展手动施法、技能栏、职业专属天赋分支、更多装备词条与技能联动、GM 配置重载和技能/天赋数值后台化。

## 2026-05-20 v0.8 后新增优化清单复核

- 优化目标：在职业、技能、天赋基础闭环完成后，重新复核 doc，形成一份可执行的新增优化清单，避免继续沿用职业系统完成前的旧优先级。
- 影响范围：`doc/PRD.md`、`doc/api.md`、`doc/game-rules.md`、`doc/development-checklist.md`、`doc/requirements-gap-analysis-20260520.md`、`doc/requirements-optimization-backlog-20260520-after-v08.md`。
- 方案文档：新增 `doc/requirements-optimization-backlog-20260520-after-v08.md`。
- 完成内容：确认当前基础系统已经覆盖职业、技能和天赋；将后续优化拆为 P0/P1/P2；P0 聚焦 GM 账号状态、配置重载、配置引用校验和战斗数值回归工具；P1 聚焦装备内容扩容、强化材料配置化、消耗品与战斗准备、技能栏与手动施法、技能材料、天赋职业分支、任务目标显式配置与剧情回看；P2 保留转职、召唤物、仓库、拍卖行、公会、PK 和运营活动。
- 验证结果：本轮为文档复核和清单沉淀，未改业务代码，未运行代码测试。
- PRD 更新：已将后续路线指向新的 v0.8 后 backlog 文档。
- 后续问题：如果立即开工，建议优先做 `v0.9 GM 账号状态、配置重载与配置校验`，它会直接提升后续技能、天赋、装备数值调试效率。

## 2026-05-20 文档再复核与后续路线校准

- 优化目标：在 v0.6 背包容量、悬浮窗、装备对比和已穿戴装备隐藏完成后，重新复核 doc，确认下一阶段应该新增或优化的内容。
- 影响范围：`doc/requirements-gap-analysis-20260520.md`、`doc/development-checklist.md`、`doc/game-rules.md`、`doc/api.md`、`doc/optimization-log.md`。
- 方案文档：更新 `doc/requirements-gap-analysis-20260520.md`，作为当前后续路线依据。
- 完成内容：将已完成的背包容量、丢弃、材料消耗和满包保护从待做项移除；明确下一轮优先级为 `v0.6.1 GM 账号状态与配置重载`；后续依次推进装备内容扩容、强化材料消耗配置化、消耗品与战斗准备最小闭环；补充规则文档中的背包容量、已穿戴装备隐藏、满包处理和强化材料消耗说明；补充 API 文档中的背包列表不返回已穿戴装备、卸下装备满包失败和强化材料内置规则说明。
- 验证结果：本轮为文档复核和校准，未改业务代码，未运行代码测试。
- PRD 更新：PRD 已在前序 v0.6 工作中同步当前完成状态和下一步推荐路线，本轮未发现需要额外调整的 PRD 范围。
- 后续问题：进入实现前建议先新增 `doc/v0.6.1-admin-account-config-plan.md`，再落地 GM 封禁/解封和配置重载。

## 2026-05-20 已穿戴装备从背包隐藏

- 优化目标：让已穿戴装备不再出现在背包格子中，只在人物装备栏展示。
- 影响范围：背包列表接口、背包容量计算、装备卸下校验、静态资源版本号、`doc/PRD.md`、`doc/development-checklist.md`。
- 方案文档：沿用 v0.6 背包规则方向，本次为装备展示边界调整。
- 完成内容：新增未穿戴物品查询和未穿戴格子计数；`GET /api/inventory` 只返回未穿戴物品，容量使用也只计算未穿戴物品；装备栏继续展示已穿戴装备；卸下装备前会检查背包剩余空间，满包时阻止卸下；移除背包浮窗中“当前已穿戴”的分支；静态资源版本号更新到 `20260520-v060-equipped-hidden`。
- 验证结果：`node --check game/src/main/resources/static/app.js` 通过；`mvn test` 通过；`mvn package -DskipTests` 通过。
- PRD 更新：已同步已穿戴装备只在装备栏展示。
- 后续问题：如果后续增加仓库，需要明确已穿戴、背包、仓库三者之间的容量边界。

## 2026-05-20 背包物品悬浮窗与装备对比

- 优化目标：背包格子中的物品描述改为鼠标悬停浮窗展示，并让装备可和当前同部位已穿戴装备做对比。
- 影响范围：背包前端渲染、装备数据读取、背包浮窗样式、静态资源版本号、`doc/PRD.md`、`doc/development-checklist.md`。
- 方案文档：沿用 v0.6 背包体验方向，本次为交互增强，不新增后端接口。
- 完成内容：背包页加载时同步读取当前装备列表；物品格子支持 hover/focus 显示悬浮窗，mouseleave/blur 隐藏；浮窗展示物品名称、品质、类型、部位、强化、属性和售价；装备浮窗按同部位已穿戴装备展示生命、攻击、防御、攻速差值；当前已穿戴装备显示“当前已穿戴”；静态资源版本号更新到 `20260520-v060-inventory-tooltip`。
- 验证结果：`node --check game/src/main/resources/static/app.js` 通过；`mvn test` 通过。
- PRD 更新：已同步背包悬浮窗和装备对比能力。
- 后续问题：如后续新增更多装备属性，可把对比字段抽成统一配置。

## 2026-05-20 v0.6 背包容量、丢弃与材料消耗基础

- 优化目标：把格子式背包从视觉承载升级为有限容量资产管理，并让材料进入强化消耗闭环。
- 影响范围：背包后端接口、背包前端交互、物品入包规则、装备强化、战斗掉落、任务奖励、挂机领取、邮件附件、地图事件奖励、GM 发物品、`doc/api.md`、`doc/PRD.md`、`doc/development-checklist.md`、`doc/v0.6-inventory-capacity-discard-plan.md`。
- 方案文档：沿用 `doc/v0.6-inventory-capacity-discard-plan.md`。
- 完成内容：背包列表返回容量、已用格和剩余格；材料自动堆叠，装备按单件占格；新增丢弃接口和材料批量出售接口；背包页支持丢弃和材料出售全部；装备强化消耗金币和材料；任务、邮件、挂机、地图事件和 GM 发物品在空间不足时阻止领取或发放；战斗掉落空间不足时通过邮件补发。
- 验证结果：`node --check game/src/main/resources/static/app.js` 通过；`mvn test` 通过；`mvn package -DskipTests` 通过。
- PRD 更新：已同步 v0.6 背包规则闭环完成状态和下一步推荐路线。
- 后续问题：可继续做 `v0.6.1 GM 账号状态与配置重载`，或进入 `v0.6.2 装备内容扩容与部位补齐`。

## 2026-05-20 文档一致性复核与背包规则方案

- 优化目标：重新复核现有 doc，确认下一步应该增加或优化的功能，并把结论同步到方案文档和 PRD。
- 影响范围：`doc/PRD.md`、`doc/api.md`、`doc/development-checklist.md`、`doc/requirements-gap-analysis-20260520.md`、`doc/v0.6-grid-inventory-visual-plan.md`、`doc/v0.6-inventory-capacity-discard-plan.md`。
- 方案文档：新增 `doc/v0.6-inventory-capacity-discard-plan.md`，作为下一轮背包容量、丢弃、批量出售、材料消耗和满包处理的实施依据。
- 完成内容：修正 PRD 中邮件仍列为非首版范围的问题；修正 Redis 当前用途描述；补充邮件页、世界 BOSS 页和 GM 地图事件状态管理页面；明确格子式背包视觉已完成但背包规则当时仍未闭环；补充 API 文档中的 v0.6 背包计划接口草案。该计划已在后续 `2026-05-20 v0.6 背包容量、丢弃与材料消耗基础` 中落地。
- 验证结果：本轮为文档复核和计划落地，未改业务代码，未运行代码测试。
- PRD 更新：已同步当前完成能力、文档一致性复核结果和下一步推荐路线。
- 后续问题：下一轮建议直接实现 `v0.6 背包容量、丢弃与材料消耗基础`。

## 2026-05-20 背包格子视觉改为白色主题

- 优化目标：按当前产品主题调整背包视觉，让格子式背包与默认白色界面保持一致，而不是独立暗色面板。
- 影响范围：背包页 CSS、静态资源版本号；本轮不改变背包功能和后端规则。
- 方案文档：沿用 `doc/v0.6-grid-inventory-visual-plan.md`。
- 完成内容：背包面板、格子、物品详情、角标和选中态改用 `var(--panel)`、`var(--panel-2)`、`var(--line)`、`var(--accent)` 等主题变量；静态资源版本号更新到 `20260520-v060-grid-inventory-light`。
- 验证结果：`node --check game/src/main/resources/static/app.js` 通过；`mvn test` 通过；`mvn package -DskipTests` 在 repackage 阶段失败，原因是 `target/legend-game-0.1.0-SNAPSHOT.jar` 被本地 Java 进程或文件句柄占用，无法重命名为 `.jar.original`。
- PRD 更新：本次为视觉主题对齐，不改变产品范围，PRD 无需新增能力描述。
- 后续问题：释放占用该 jar 的本地进程后重新执行 `mvn package -DskipTests`。

## 2026-05-20 v0.6 格子式背包与像素道具图标

- 优化目标：将背包从列表展示升级为类似传奇的格子式背包，并为每个道具补充独立像素卡通风图标。
- 影响范围：背包页前端渲染、背包样式、静态资源版本号、道具图标资产、`doc/v0.6-grid-inventory-visual-plan.md`、`doc/PRD.md`、`doc/development-checklist.md`；本轮不改后端接口和物品规则。
- 方案文档：新增 `doc/v0.6-grid-inventory-visual-plan.md`。
- 完成内容：背包页改为固定格子面板，展示 40 个格子、空槽、品质边框、数量角标和强化等级；点击物品格子后显示详情和穿戴/强化/出售操作；新增 9 个按 `itemId` 命名的像素 SVG 图标资产；静态资源版本号更新到 `20260520-v060-grid-inventory`。
- 验证结果：`node --check game/src/main/resources/static/app.js` 通过；`mvn test` 通过；`mvn package -DskipTests` 通过。
- PRD 更新：已同步格子式背包视觉和像素图标完成状态。
- 后续问题：后续继续补真实背包容量、丢弃接口、批量出售、材料消耗和满包处理规则。

## 2026-05-20 需求深挖与完善点分析

- 优化目标：在 v0.5.13 任务追踪体验闭环后，从 PRD、规则、API、配置和现有模块中识别下一阶段还需要完善的产品缺口。
- 影响范围：`doc/requirements-gap-analysis-20260520.md`、`doc/PRD.md`、`doc/development-checklist.md`；本次只做需求分析和路线收敛，不改业务代码。
- 方案文档：新增 `doc/requirements-gap-analysis-20260520.md`。
- 完成内容：梳理出 P0 欠账包括背包丢弃/容量、GM 封禁/配置重载、文档一致性；P1 短板包括装备内容密度、材料用途、消耗品、任务链回看和任务目标显式配置；推荐近期路线为 v0.5.14 文档一致性校准、v0.6 背包容量/丢弃/材料消耗、v0.6.1 GM 账号状态与配置重载、v0.6.2 装备内容扩容。
- 验证结果：已完成文档检查和配置规模核对，当前配置为地图 8 张、怪物 15 个、任务 25 条、物品 9 个、装备 3 件、BOSS 1 个、世界 BOSS 1 个。
- PRD 更新：已同步需求深挖结论和推荐近期路线。
- 后续问题：如立即开工，建议先在 `doc/` 新增 v0.6 背包资产管理详细计划，再进入实现。

## 2026-05-20 v0.5.13 任务追踪筛选、推荐排序与剧情引导

- 优化目标：把地图任务追踪从“展示任务和定位目标”升级为“推荐玩家下一步该做什么”，减少任务判断成本。
- 影响范围：地图页任务追踪、任务页状态文案、静态资源版本号、`doc/v0.5.13-task-guidance-plan.md`、`doc/PRD.md`、`doc/development-checklist.md`；本轮不新增后端 API。
- 方案文档：沿用 `doc/v0.5.13-task-guidance-plan.md`。
- 完成内容：地图任务追踪新增全部、可领取、进行中、当前地图和跨地图筛选；默认推荐排序优先展示可领取、当前地图可完成、任务链靠前和目标明确的任务；任务卡补充可领取、当前地图、跨地图和前置未完成引导文案；任务页复用同一套状态文案；静态资源版本号更新到 `20260520-v0513-task-guidance`。
- 验证结果：`node --check game/src/main/resources/static/app.js` 通过；`mvn test` 通过；`mvn package -DskipTests` 通过。
- PRD 更新：已同步 v0.5.13 完成状态和任务追踪体验能力。
- 后续问题：后续可继续做任务链专页、剧情日志回看，或把任务目标显式文案扩展到任务配置中。

## 2026-05-20 制定 v0.5.13 任务追踪筛选、推荐排序与剧情引导计划

- 优化目标：检查现有文档后确认 v0.5.12 已完成跨地图任务目标提示，下一步应继续提升任务追踪的决策效率，让玩家知道优先做哪个任务以及为什么做。
- 影响范围：`doc/v0.5.13-task-guidance-plan.md`、`doc/PRD.md`、`doc/development-checklist.md`；本次只制定下一步方案，不改业务代码。
- 方案文档：新增 `doc/v0.5.13-task-guidance-plan.md`。
- 完成内容：明确下一轮范围为任务筛选、推荐排序和任务链引导文案；同步 PRD 当前状态和后续方向；在开发清单中追加 v0.5.13 待办项。
- 验证结果：文档已补充，后续可按 v0.5.13 计划进入实现。
- PRD 更新：已同步推荐下一步为 v0.5.13 任务追踪筛选、推荐排序与剧情引导。
- 后续问题：进入代码实现前需先确认是否仅复用现有任务接口，或为任务配置补充轻量展示文案字段。

## 2026-05-19 v0.5.12 跨地图任务目标提示

- 优化目标：让地图任务追踪在目标不属于当前地图时提示目标地图，并支持一键切换到目标地图继续查看高亮点位。
- 影响范围：地图页任务追踪、地图视图切换、目标点位高亮、`doc/v0.5.12-cross-map-task-target-plan.md`、`doc/PRD.md`、`doc/development-checklist.md`；本轮不新增后端 API。
- 方案文档：新增 `doc/v0.5.12-cross-map-task-target-plan.md`。
- 完成内容：任务卡识别 `targetMapId` 与当前地图不一致的情况，展示目标地图名称和“前往目标地图”按钮；点击后调用现有地图场景加载流程切换地图、刷新列表高亮和任务追踪，并在目标点位存在时移动前端角色标记；当前地图内目标仍保留“查看目标”。
- 验证结果：`node --check game/src/main/resources/static/app.js` 通过；`mvn test` 通过；`mvn package -DskipTests` 通过。
- PRD 更新：已同步 v0.5.12 跨地图任务目标提示能力。
- 后续问题：后续可继续增加任务目标筛选、任务推荐排序和更明确的任务链剧情引导。

## 2026-05-19 v0.5.11 任务目标定位元数据

- 优化目标：将 v0.5.10 的前端轻量目标匹配升级为后端明确返回任务目标地图和点位，降低误判风险。
- 影响范围：任务响应模型、任务服务目标定位推导、地图任务目标高亮、`doc/v0.5.11-task-target-location-plan.md`、`doc/api.md`、`doc/PRD.md`、`doc/development-checklist.md`。
- 方案文档：新增 `doc/v0.5.11-task-target-location-plan.md`。
- 完成内容：`GET /api/tasks` 响应新增 `targetMapId`、`targetPointId`、`targetPointType`；NPC 对话任务返回 NPC 地图和点位，地图探索任务返回事件地图和点位，击杀任务优先返回包含目标怪物的怪区或随机遇怪点位，无法定位点位时回退到目标地图；前端任务目标高亮和“查看目标”优先使用后端定位字段，名称匹配保留为兜底。
- 验证结果：`node --check game/src/main/resources/static/app.js` 通过；`mvn test` 通过；`mvn package -DskipTests` 通过。
- PRD 更新：已同步 v0.5.11 任务目标定位元数据能力。
- 后续问题：后续可以在任务配置中显式声明目标点位，进一步减少推导逻辑。

## 2026-05-19 v0.5.10 地图任务目标点位提示

- 优化目标：让地图任务追踪和地图点位产生直接关联，帮助玩家快速找到任务相关目标。
- 影响范围：地图页任务追踪、地图点位渲染、聚合点位渲染、静态资源版本号、`doc/v0.5.10-map-task-target-hint-plan.md`、`doc/PRD.md`、`doc/development-checklist.md`；本轮不新增后端 API。
- 方案文档：新增 `doc/v0.5.10-map-task-target-hint-plan.md`。
- 完成内容：基于现有任务 `type/targetName` 和当前地图场景点位做前端轻量匹配；NPC 对话任务匹配 NPC 点位，地图探索任务匹配事件点位，击杀任务匹配怪区或随机遇怪点位；任务追踪卡新增“查看目标”按钮；目标点位和包含目标的聚合按钮显示高亮；点击查看目标会移动前端角色标记并追加交互记录。
- 验证结果：`node --check game/src/main/resources/static/app.js` 通过；`mvn test` 通过；`mvn package -DskipTests` 通过。
- PRD 更新：已同步 v0.5.10 地图任务目标点位提示能力。
- 后续问题：后续可继续在后端任务响应中显式返回目标地图和目标点位 ID，替换当前前端轻量匹配。

## 2026-05-19 v0.5.9 地图任务追踪

- 优化目标：让玩家在地图页直接看到当前推荐任务，减少地图页和任务页之间来回切换。
- 影响范围：地图页左侧追踪面板、任务展示复用逻辑、任务领取刷新流程、静态资源版本号、`doc/v0.5.9-map-task-tracker-plan.md`、`doc/PRD.md`、`doc/development-checklist.md`；本轮不新增后端 API。
- 方案文档：新增 `doc/v0.5.9-map-task-tracker-plan.md`。
- 完成内容：地图页新增任务追踪面板，最多展示 3 条任务；优先显示可领取任务，其次显示进行中任务；展示任务名称、状态、目标进度和奖励摘要；可领取任务可直接在地图页领取；进入地图、战斗完成、NPC 对话、地图事件和任务领取后刷新追踪。
- 验证结果：`node --check game/src/main/resources/static/app.js` 通过；`mvn test` 通过；`mvn package -DskipTests` 通过。
- PRD 更新：已同步 v0.5.9 地图任务追踪能力。
- 后续问题：后续可继续做任务目标点位高亮、任务自动定位、更多非奖励型互动点和剧情线索。

## 2026-05-19 v0.5.8 地图缩放与拖拽视角

- 优化目标：在点位聚合之后继续提升小屏幕地图可读性，支持局部放大和拖拽查看。
- 影响范围：地图页工具栏、地图画布 CSS transform、空白点击移动判定、静态资源版本号、`doc/v0.5.8-map-zoom-pan-plan.md`、`doc/PRD.md`、`doc/development-checklist.md`；本轮不新增后端 API。
- 方案文档：新增 `doc/v0.5.8-map-zoom-pan-plan.md`。
- 完成内容：地图工具栏新增放大、缩小和重置视角按钮；地图画布支持 1.0 到 1.8 倍缩放；放大后拖拽空白画布可平移视角；拖拽不会误触发空白点击移动角色；点位、聚合菜单和移动端日志抽屉保持原流程。
- 验证结果：`node --check game/src/main/resources/static/app.js` 通过；`mvn test` 通过；`mvn package -DskipTests` 通过。
- PRD 更新：已同步 v0.5.8 地图缩放与拖拽视角能力。
- 后续问题：后续可继续做双指缩放、滚轮缩放、任务追踪小面板和更多非奖励型互动点。

## 2026-05-19 v0.5.7 地图点位聚合与日志抽屉

- 优化目标：减少地图点位在小屏幕和高密度区域互相遮挡，并把移动端日志升级为底部抽屉。
- 影响范围：地图页点位渲染、地图页移动端日志承载、静态资源版本号、`doc/v0.5.7-map-cluster-log-drawer-plan.md`、`doc/PRD.md`、`doc/development-checklist.md`；本轮不新增后端 API。
- 方案文档：新增 `doc/v0.5.7-map-cluster-log-drawer-plan.md`。
- 完成内容：相近点位合并为“附近 N”聚合按钮，点击后展开附近点位清单，清单点位继续复用原 NPC、地图事件和战斗触发流程；移动端新增底部日志抽屉，收起时展示最近一条交互提示，展开后同步展示交互记录和战斗记录；桌面端继续保留右侧日志栏。
- 验证结果：`node --check game/src/main/resources/static/app.js` 通过；`mvn test` 通过；`mvn package -DskipTests` 通过。
- PRD 更新：已同步 v0.5.7 地图点位聚合与日志抽屉能力。
- 后续问题：后续可继续做地图缩放按钮、真实触屏拖拽视角和更多非奖励型互动点。

## 2026-05-19 v0.5.6 地图交互打磨

- 优化目标：继续改善地图页手机端操作效率，让地图本体优先可见，并增加空白点击移动反馈。
- 影响范围：地图页前端交互、地图页响应式布局、`doc/v0.5.6-map-interaction-polish-plan.md`、`doc/PRD.md`、`doc/development-checklist.md`；本轮不新增后端 API。
- 方案文档：新增 `doc/v0.5.6-map-interaction-polish-plan.md`。
- 完成内容：地图页新增“回到角色”和“显示/隐藏日志”快捷操作；手机端默认收起交互记录和战斗记录区域；点击地图空白区域可移动前端角色标记并追加交互记录；点位按钮点击仍按 NPC、地图事件或战斗原流程执行；同步修正 PRD、架构文档、开发清单和旧计划文档中的历史状态表述。
- 验证结果：`node --check game/src/main/resources/static/app.js` 通过；`mvn test` 通过；`mvn package -DskipTests` 通过。
- PRD 更新：已同步 v0.5.6 地图交互打磨能力。
- 后续问题：后续可继续做点位聚合、地图缩放按钮、底部抽屉式日志和真实触屏拖拽视角。

## 2026-05-19 v0.5.5 玩家界面信息收口与修改密码

- 优化目标：清理普通玩家页面中直接暴露的内部字段和英文枚举，同时登录后增加修改密码入口。
- 影响范围：认证后端接口、账号仓储、顶部全局操作、角色/任务/背包/装备/邮件/战斗展示、`doc/v0.5.5-user-ui-password-plan.md`、`doc/api.md`、`doc/PRD.md`、`doc/development-checklist.md`。
- 方案文档：新增 `doc/v0.5.5-user-ui-password-plan.md`。
- 完成内容：新增 `POST /api/auth/change-password`，校验原密码、新密码长度和新旧密码差异后更新 BCrypt 哈希；登录后顶部新增修改密码弹层；普通玩家页将地图位置、任务锁定、物品品质/类型、装备部位、邮件来源、战斗状态改为用户可理解文案；GM 后台技术字段保持可见。
- 验证结果：`node --check game/src/main/resources/static/app.js` 通过；`mvn test` 通过；`mvn package -DskipTests` 通过。
- PRD 更新：已同步玩家界面信息收口与修改密码能力。
- 后续问题：如后续账号体系扩展，可补充找回密码、强制下线其他设备和二次验证。

## 2026-05-19 v0.5.1 地图事件奖励与每日探索

- 优化目标：继续补强 v0.5 地图事件玩法，让奖励点从单一金币扩展为经验、金币和物品奖励，并补上每日探索点的重置规则。
- 影响范围：地图事件配置、地图事件触发服务、角色成长与背包奖励发放、地图事件状态判断、`doc/v0.5.1-map-event-reward-daily-plan.md`、`doc/api.md`、`doc/PRD.md`、`doc/development-checklist.md`。
- 方案文档：新增 `doc/v0.5.1-map-event-reward-daily-plan.md`。
- 完成内容：`map-events.json` 支持 `rewardExp`、`rewardItems` 和 `resetType=daily`；奖励事件统一发放经验、金币和物品，金币写入 `currency_logs`，物品写入背包与 `drop_logs`，经验可触发升级和等级任务刷新；每日事件触发后将 `next_available_at` 设置为次日 00:00；新增“林地补给”每日探索点，废井奖励扩展为经验、金币和材料。
- 验证结果：`mvn test` 通过；`mvn package -DskipTests` 通过。
- PRD 更新：已同步 v0.5.1 地图事件奖励与每日探索能力。
- 后续问题：更多非奖励型互动点和移动端底部抽屉日志继续后置。

## 2026-05-19 v0.5.2 每日地图探索内容扩充

- 优化目标：把 v0.5.1 的每日探索样板点扩展到全部地图，让不同等级段都有轻量日常收益。
- 影响范围：`items.json`、`map-events.json`、`doc/v0.5.2-daily-map-content-plan.md`、`doc/api.md`、`doc/PRD.md`、`doc/development-checklist.md`。
- 方案文档：新增 `doc/v0.5.2-daily-map-content-plan.md`。
- 完成内容：新增铁矿石、毒囊、骨片、残破符片、赤月尘等材料配置；新增矿洞、毒蛇山谷、矿洞深处、骷髅洞、寺庙外庭、寺庙正殿、赤月峡谷入口每日奖励点；当前 8 张地图均至少有 1 个 `resetType=daily` 的奖励事件，并按地图等级段递增经验、金币和材料收益。
- 验证结果：已通过 JSON 覆盖检查，确认 8 张地图每日点覆盖完整且奖励物品引用存在；`mvn test` 通过；`mvn package -DskipTests` 通过。
- PRD 更新：已同步 v0.5.2 每日探索内容扩充状态。
- 后续问题：更多地图非奖励型互动点和移动端底部抽屉日志继续后置。

## 2026-05-19 v0.5.3 地图事件状态管理

- 优化目标：补齐地图事件状态的 GM 查询与重置能力，方便测试每日点、一次性事件和短冷却事件。
- 影响范围：`MapEventStateRepository`、`AdminService`、`AdminController`、GM 前端页面、`doc/v0.5.3-map-event-admin-plan.md`、`doc/api.md`、`doc/PRD.md`、`doc/development-checklist.md`。
- 方案文档：新增 `doc/v0.5.3-map-event-admin-plan.md`。
- 完成内容：新增 `GET /api/admin/map-event-states`、`POST /api/admin/map-event-states/reset`、`POST /api/admin/map-event-states/reset-all`；GM 页面新增事件状态查询、单个重置和全部重置入口；重置操作删除 `map_event_states` 状态并写入 `gm_logs`。
- 验证结果：`mvn test` 通过；`mvn package -DskipTests` 通过。
- PRD 更新：已同步 v0.5.3 地图事件状态管理能力。
- 后续问题：更多非奖励型互动点和移动端底部抽屉日志继续后置。

## 2026-05-19 v0.5.4 地图事件状态清理

- 优化目标：补齐地图事件状态清理策略，避免每日事件和短冷却事件在长期测试或运营后留下过多无价值状态记录。
- 影响范围：`MapEventStateRepository`、`AdminService`、`AdminController`、GM 前端页面、`doc/v0.5.4-map-event-state-cleanup-plan.md`、`doc/api.md`、`doc/PRD.md`、`doc/development-checklist.md`。
- 方案文档：新增 `doc/v0.5.4-map-event-state-cleanup-plan.md`。
- 完成内容：新增 `POST /api/admin/map-event-states/cleanup`；按角色清理超过保留天数且不在冷却中的可重复事件状态；保留 `completed=1` 的一次性完成状态；GM 页面新增保留天数输入和清理过期状态按钮；清理操作写入 `gm_logs`。
- 验证结果：`mvn test` 通过；`mvn package -DskipTests` 通过。
- PRD 更新：已同步 v0.5.4 地图事件状态清理能力。
- 后续问题：更多非奖励型互动点和移动端底部抽屉日志继续后置。

## 2026-05-19 v0.5 地图 NPC 任务交互最小闭环

- 优化目标：按 `doc/v0.5-map-task-interaction-plan.md` 落地 v0.5 第一轮最小闭环，让地图、NPC、任务和点位交互从方案进入可运行实现。
- 影响范围：地图/怪物/任务/NPC/事件配置，地图场景与交互接口，任务前置与新任务类型，角色地图位置字段，前端地图页，`doc/PRD.md`、`doc/api.md`、`doc/development-checklist.md`。
- 方案文档：沿用 `doc/v0.5-map-task-interaction-plan.md` 中的“方案二：轻量可交互地图”和“建议优先实现的最小闭环”。
- 完成内容：地图扩展到 8 张，普通怪物扩展到 15 个，任务扩展到 25 个；新增 `npcs.json`、`map-events.json`；新增地图场景、NPC 对话、地图事件触发接口；任务支持 `talk_npc`、`explore_event` 和 `preTaskIds`；角色记录 `currentNodeId/lastX/lastY`；前端地图页升级为三栏黑白/灰阶点位场景，支持点击 NPC/事件点、角色轻量移动、任务推进和战斗复用。
- 验证结果：`mvn test` 通过；后续继续做本地服务与浏览器烟测。
- PRD 更新：已同步 v0.5 基础版完成状态、内容数量、接口能力和后续边界。
- 后续问题：事件一次性状态、每日探索冷却、地图事件状态表、完整格子探索和更丰富像素资源继续后置。

## 2026-05-19 v0.5 地图移动端交互优化

- 优化目标：解决可交互地图需要来回拖动滚动条的问题，并让地图页更适合手机端操作。
- 影响范围：`game/src/main/resources/static/index.html`、`game/src/main/resources/static/app.js`、`game/src/main/resources/static/app.css`。
- 方案文档：沿用 `doc/v0.5-map-task-interaction-plan.md` 的“大地图 + 点位移动 + 像素角色模型”方向，本次只优化前端交互承载方式。
- 完成内容：地图画布从真实像素尺寸滚动容器改为按地图宽高比例自适应缩放的固定视口；点位和玩家位置继续按后端坐标映射为百分比；移动端地图列表改为横向滑动卡片；手机端缩小点位、角色模型和日志高度，减少地图被日志挤出首屏的问题；静态资源版本号更新为 `20260519-v05-map-responsive`。
- 验证结果：`mvn test` 通过；`mvn package -DskipTests` 因当前已有 Java 进程占用 `target/legend-game-0.1.0-SNAPSHOT.jar` 导致 repackage 无法重命名 jar，未擅自停止用户进程。
- PRD 更新：本次为已完成 v0.5 页面交互承载方式优化，未改变玩法范围和接口能力。
- 后续问题：可以继续补点位聚合、地图缩放按钮、点击空白处移动、底部抽屉式日志和真实触屏拖拽视角。

## 2026-05-19 v0.5 地图事件状态基础版

- 优化目标：承接 v0.5 P3，给地图事件补充角色维度状态，让一次性奖励点、短冷却探索点和后续每日玩法有事实数据来源。
- 影响范围：`map_event_states` 数据表与迁移、地图事件状态仓储、地图场景接口、地图事件触发接口、地图点位前端状态展示、`doc/PRD.md`、`doc/api.md`、`doc/development-checklist.md`。
- 方案文档：沿用 `doc/v0.5-map-task-interaction-plan.md` 的“v0.5 P3：事件状态与可重复玩法”。
- 完成内容：新增 `map_event_states` 表，记录 `character_id/event_id/trigger_count/last_triggered_at/next_available_at/completed`；事件触发前校验一次性完成和冷却状态，触发后写入状态；场景接口返回 `completed/coolingDown/nextAvailableAt/statusText`；前端点位支持已完成和冷却中样式；配置中废井改为一次性奖励点，矿轨和骷髅洞石门增加短冷却。
- 验证结果：`mvn test` 通过。
- PRD 更新：已同步 v0.5 事件状态能力。
- 后续问题：更多非奖励型互动点后置。

## 2026-05-18 方案文档化与 PRD 同步规则

- 优化目标：落实“每次生成的方案都要落地到 doc，并同步 PRD”的项目协作规则，避免后续计划只停留在对话记录中。
- 影响范围：`doc/PRD.md`、`doc/development-checklist.md`、`doc/optimization-log.md`。
- 方案文档：本次为流程规则更新，直接落入 PRD、开发清单和优化记录模板；后续功能方案应新增或更新对应 `doc/*-plan.md` 或专题章节。
- 完成内容：在 PRD 的开发状态章节新增方案文档化硬约束；在开发清单新增方案落地和 PRD 同步检查项；在优化记录模板中新增“方案文档”字段。
- 验证结果：文档规则已落地，未涉及代码运行。
- PRD 更新：已同步新增方案落地与 PRD 同步要求，并更新当前 v0.4 世界 BOSS 完成后的后续方向描述。
- 后续问题：后续所有新计划执行前必须先确认对应 doc 位置，并在实施完成后更新清单与优化记录。

## 2026-05-18 v0.5 地图、NPC、任务与可交互地图方案

- 优化目标：为“完善地图和任务系统，增加地图内 NPC、任务量和可交互地图/遇怪操作”形成多个可选方案，并按项目规则落地到 doc。
- 影响范围：`doc/v0.5-map-task-interaction-plan.md`、`doc/PRD.md`、`doc/development-checklist.md`。
- 方案文档：已新增 `doc/v0.5-map-task-interaction-plan.md`，包含内容扩容优先、轻量可交互地图、格子探索地图三个方案。
- 完成内容：推荐路线为“方案二：轻量可交互地图”，并确认第一版采用“大地图 + 点位移动 + 像素角色模型”；先扩充地图/怪物/任务，再新增 NPC、地图点位、探索事件和任务前置链；格子探索地图后置；补充 `doc/v0.5-interactive-map-visual.html`、`doc/v0.5-interactive-map-visual.svg` 和 `doc/v0.5-interactive-map-pixel-visual.svg` 作为视觉稿。
- 验证结果：本次为方案和视觉稿输出，未改业务代码，未执行运行测试；浏览器插件不允许直接访问本地 `file://` HTML，已补充 SVG 快照用于对话内查看。
- PRD 更新：已将地图探索、NPC 交互、v0.5 后续方向和方案文档引用同步到 PRD。
- 后续问题：进入实现时优先定义 NPC、地图事件点、角色当前位置和像素资源字段；完整格子自由移动继续后置。

## 2026-05-15 建立优化闭环前提条件

- 优化目标：明确后续每次优化都需要留痕，并同步维护 PRD。
- 影响范围：`doc/PRD.md`、`doc/development-checklist.md`、`doc/optimization-log.md`。
- 完成内容：新增优化记录文件，并在 PRD 与开发清单中补充优化闭环约束。
- 验证结果：文档规则已落地，后续优化可按模板追加记录。
- PRD 更新：已在 PRD 的“开发状态与清单”章节加入优化前提条件。
- 后续问题：后续每次代码或体验优化完成后，应继续追加本文件记录。

## 2026-05-15 登录首页纯净化

- 优化目标：未登录首页只保留登录注册主界面，不展示顶部功能按钮或游戏功能导航。
- 影响范围：`game/src/main/resources/static/index.html`、`game/src/main/resources/static/app.css`、`game/src/main/resources/static/app.js`、`doc/PRD.md`。
- 完成内容：未登录状态隐藏侧边栏导航，移除登录页右上角主题切换按钮，并让主布局在登录状态下占满整屏。
- 验证结果：已刷新 `http://localhost:8080/`，未登录 DOM 仅包含登录标题、说明、账号密码输入框、登录和注册按钮。
- PRD 更新：已在 PRD 的“当前已完成”中补充登录首页体验优化，并同步调整主题切换能力描述。
- 后续问题：如需进一步纯净化，可继续压缩登录卡片中的说明文案。

## 2026-05-15 游戏内顶部操作右置

- 优化目标：登录后将切换主题和退出按钮放到界面右上角，减少左侧导航干扰。
- 影响范围：`game/src/main/resources/static/index.html`、`game/src/main/resources/static/app.css`、`doc/PRD.md`、`doc/development-checklist.md`、`doc/optimization-log.md`。
- 完成内容：移除侧边栏底部操作区，在顶部栏右侧新增刷新、切换主题和退出按钮组，并补充移动端换行样式。
- 验证结果：已构建并刷新 `http://localhost:8080/`，登录后主界面顶部显示刷新、切换主题和退出按钮，左侧栏仅保留功能导航。
- PRD 更新：已在 PRD 的“当前已完成”中补充游戏内顶部操作优化。
- 后续问题：如后续按钮增加，可考虑改成图标按钮或更多菜单。

## 2026-05-15 全局操作脱离角色栏

- 优化目标：将切换主题和退出按钮放到整体界面的全局右上角，而不是角色标题栏内。
- 影响范围：`game/src/main/resources/static/index.html`、`game/src/main/resources/static/app.css`、`game/src/main/resources/static/app.js`、`doc/PRD.md`、`doc/development-checklist.md`、`doc/optimization-log.md`。
- 完成内容：新增全局操作区 `globalActions`，切换主题和退出按钮固定在视口右上角；当前内容标题栏仅保留刷新按钮，并为静态资源增加版本参数避免浏览器缓存旧脚本。
- 验证结果：已构建并刷新 `http://localhost:8080/`，`globalActions` 可见，角色标题栏内不再包含切换主题和退出按钮。
- PRD 更新：已将顶部操作描述调整为整体界面右上角。
- 后续问题：移动端如右上角空间不足，可改为右上角更多菜单。

## 2026-05-15 制定 v0.2 详细计划

- 优化目标：明确 v0.2 不只是任务领取，还包括成长目标、资源消耗、挂机收益、BOSS 和简易运营能力。
- 影响范围：`doc/v0.2-plan.md`、`doc/PRD.md`、`doc/development-checklist.md`、`doc/optimization-log.md`。
- 完成内容：新增 v0.2 详细计划文档，拆分 P0/P1/P2 范围、推荐开发顺序、发布验收标准、不做范围和风险点。
- 验证结果：文档已补充，后续 v0.2 开发可按 `doc/v0.2-plan.md` 执行。
- PRD 更新：已在 PRD 的 v0.2 方向中补充任务系统，并引用详细计划文档。
- 后续问题：进入 v0.2 实施前，建议先从任务系统开始。

## 2026-05-15 v0.2 任务系统基础版

- 优化目标：完成 v0.2 第一项任务系统，让玩家可以查看任务、推进进度并领取奖励。
- 影响范围：任务配置加载、任务接口、战斗结算、前端任务页、`doc/api.md`、`doc/PRD.md`、`doc/development-checklist.md`、`doc/v0.2-plan.md`。
- 完成内容：新增 `GET /api/tasks`、`POST /api/tasks/{taskId}/claim`，支持击杀怪物和达到等级两类任务，奖励支持经验、金币和物品；前端新增任务导航与任务页。
- 验证结果：`mvn test` 和 `mvn package -DskipTests` 通过；接口烟测完成注册、建角、任务列表、击杀 3 次小鸡推进任务、领取奖励；浏览器确认任务入口、任务页、进度、奖励和领取按钮可见。
- PRD 更新：已在 PRD 当前完成能力中补充任务系统基础版。
- 后续问题：后续装备强化完成后，可扩展穿戴或强化类任务。

## 2026-05-15 v0.2 P0/P1 功能完成

- 优化目标：按 v0.2 计划完成装备强化、挂机收益、BOSS 刷新和简易 GM 后台。
- 影响范围：装备、挂机、BOSS、GM 后端接口，前端导航与页面，数据库 schema 与迁移，`doc/api.md`、`doc/PRD.md`、`doc/development-checklist.md`、`doc/v0.2-plan.md`。
- 完成内容：装备强化支持 +1 到 +3 并扣金币刷新战力；挂机支持开始、状态和收益领取，最多累计 8 小时；BOSS 支持配置、状态查看、挑战、冷却和额外奖励；GM 支持管理员角色查询、发金币、发物品并写日志。
- 验证结果：`mvn test`、`mvn package -DskipTests` 通过；接口烟测确认任务、挂机、BOSS 列表、GM 查询、GM 发放、装备强化可用；浏览器确认挂机、BOSS、装备强化入口可见。
- PRD 更新：已在 PRD 当前完成能力和 v0.2 方向中补充 v0.2 P0/P1 完成状态。
- 后续问题：P2 的邮件、Redis、排行榜性能优化仍按计划后置到 v0.3 或后续版本。

## 2026-05-15 制定 v0.3 战斗体验计划

- 优化目标：遍历现有 doc 后重新确定后续开发方向，把下一阶段从泛化的 Redis/邮件收敛为攻击速度、在线会话式回合战斗和离线挂机后台结算。
- 影响范围：`doc/v0.3-plan.md`、`doc/PRD.md`、`doc/game-rules.md`、`doc/api.md`、`doc/architecture.md`、`doc/development-checklist.md`、`doc/optimization-log.md`。
- 完成内容：新增 v0.3 详细计划；补充 `attackSpeed` 属性、战力公式、在线战斗 start/next/status 接口、战斗会话存储、前端实时回合展示、挂机后台结算边界和验收标准。
- 验证结果：已完成文档一致性检查，确认 v0.2 P0/P1 已完成，剩余高价值主线应优先推进 v0.3 战斗体验。
- PRD 更新：已将 v0.3 方向调整为战斗体验与在线状态优先，并引用 `doc/v0.3-plan.md`。
- 后续问题：进入代码实现时需要处理已有角色攻击速度默认值、战力重算、战斗会话幂等、防重复点击和旧战斗接口兼容。

## 2026-05-15 v0.3 会话式回合战斗基础版

- 优化目标：把普通在线战斗和 BOSS 战斗从一次性返回整场结果，升级为按攻击速度一轮一轮实时推进。
- 影响范围：角色属性、怪物配置、装备配置、战力公式、战斗会话表、普通战斗接口、BOSS 战斗接口、前端地图/BOSS 战斗页、`doc/PRD.md`、`doc/api.md`、`doc/architecture.md`、`doc/development-checklist.md`、`doc/v0.3-plan.md`。
- 完成内容：新增 `attackSpeed`；新增 `battle_sessions`；实现 `POST /api/battles/start`、`POST /api/battles/{battleId}/next`、`GET /api/battles/{battleId}` 和 `POST /api/bosses/{bossId}/start`；前端按 `suggestedDelayMs` 自动推进并展示双方 HP、攻速和实时日志；保留旧 `/api/battles/fight` 兼容接口；预留本地 Redis 配置 `redis://127.0.0.1:6379/15`。
- 验证结果：`mvn test`、`mvn package -DskipTests` 通过；本地新版服务启动成功；接口烟测确认 `POST /api/battles/start` 只返回开场状态，连续调用 `POST /api/battles/{battleId}/next` 分别返回玩家攻击和怪物反击，actions 数量逐步增加，不再一次性返回整场。
- PRD 更新：已在 PRD 当前完成能力和 v0.3 方向中补充会话式回合战斗基础版。
- 后续问题：Redis Store、邮件系统、排行榜缓存、WebSocket/SSE 当时仍作为 P2 后续增强；该记录之后已继续完成 Redis 战斗会话存储。

## 2026-05-15 v0.3 文档状态校准

- 优化目标：再次遍历 doc，修正 v0.3 已完成能力与后续计划之间的状态不一致。
- 影响范围：`doc/PRD.md`、`doc/v0.3-plan.md`、`doc/api.md`、`doc/deployment-linux.md`、`doc/optimization-log.md`。
- 完成内容：将 v0.3 P0/P1 在计划文档中标记为完成；将 PRD 当前剩余主线改为 v0.3 P2；补充 BOSS `start` 推荐接口；调整部署文档中 Redis 说明为“预留但不强依赖”。
- 验证结果：文档检查确认旧“一次性战斗”只保留为兼容接口说明，后续主线集中在 Redis Store、邮件/补偿、排行榜缓存和 WebSocket/SSE。
- PRD 更新：已同步当前完成状态和下一步主线。
- 后续问题：进入下一轮代码开发前，建议从 Redis Store 或邮件/补偿二选一启动。

## 2026-05-15 v0.3 Redis 战斗会话存储

- 优化目标：将在线战斗会话从 SQLite 临时态迁到本地 Redis 15 号库，减少高频状态读写对数据库的压力。
- 影响范围：`game/pom.xml`、`game/src/main/resources/application.yml`、`game/src/main/resources/application-prod.yml`、`game/src/main/java/com/paly/legend/battle/*`、`game/src/main/java/com/paly/legend/common/DatabaseMigration.java`、`doc/PRD.md`、`doc/architecture.md`、`doc/v0.3-plan.md`、`doc/development-checklist.md`、`doc/optimization-log.md`。
- 完成内容：新增 `BattleSessionStore` 抽象；保留 SQLite 实现作为回退；新增 Redis 实现并默认启用；显式指定 Redis 15 号库；重新打包启动后验证在线战斗会话 key 落在 `legend:battle:session:*` 与 `legend:battle:character:running:*`。
- 验证结果：`mvn test`、`mvn package -DskipTests` 通过；健康检查通过；战斗烟测确认 `start` 和 `next` 仍按回合推进；Redis 15 号库可查到战斗会话 key。
- PRD 更新：已补充当前在线战斗会话使用 Redis 存储的说明。
- 后续问题：下一步继续做在线状态、BOSS 冷却和排行榜缓存，或转向邮件/补偿承接。

## 2026-05-15 v0.3 在线状态与 BOSS 冷却缓存

- 优化目标：继续完善 Redis P2，将玩家在线状态和 BOSS 冷却状态迁移到 Redis，降低高频状态查询对 SQLite 的压力。
- 影响范围：认证拦截器、在线状态服务、GM 角色列表、BOSS 状态仓库、前端 GM 列表展示、`doc/PRD.md`、`doc/api.md`、`doc/architecture.md`、`doc/deployment-linux.md`、`doc/development-checklist.md`、`doc/v0.3-plan.md`、`doc/optimization-log.md`。
- 完成内容：玩家每次带 token 请求会刷新 `legend:online:account:*` 短 TTL 在线状态；GM 角色列表展示在线/离线和最后活跃时间；BOSS 冷却读取优先走 `legend:boss:cooldown:*`，并与 SQLite 双写保持可恢复。
- 验证结果：`mvn test`、`mvn package -DskipTests` 通过；本地服务启动成功；接口烟测确认战斗继续按回合推进，BOSS 列表可读；Redis 15 号库可查到 `legend:online:account:*` 和 `legend:boss:cooldown:*`。
- PRD 更新：已补充在线状态和 BOSS 冷却缓存已切到 Redis 的说明。
- 后续问题：v0.3 P2 剩余排行榜缓存、邮件/补偿、WebSocket/SSE。

## 2026-05-15 v0.3 Redis 排行榜缓存

- 优化目标：继续完成 v0.3 P2 的 Redis 线路，为等级榜、战力榜和财富榜增加短 TTL 缓存，降低排行榜高频查询对 SQLite 排序的压力。
- 影响范围：新增 `RankingCacheService`；`RankingService` 优先读写 Redis，缓存未命中或 Redis 异常时回落 SQLite；新增 `game.ranking.cache-ttl-seconds` / `RANKING_CACHE_TTL_SECONDS` 配置。
- 完成内容：排行榜缓存 key 使用 `legend:ranking:{type}:{limit}`，默认 TTL 60 秒；缓存作为性能优化，不改变 SQLite 事实数据来源。
- 验证结果：`mvn test`、`mvn package -DskipTests` 通过；前台启动 jar 已到达 `Tomcat started on port(s): 8080`；当前沙箱内后台持久启动会在命令结束后退出，需用户授权后再保持 `http://localhost:8080/` 常驻验证 Redis key。
- PRD 更新：已补充排行榜短 TTL 缓存已接入 Redis，并更新 v0.3 P2 剩余范围。
- 后续问题：邮件/补偿系统和 WebSocket/SSE 推送仍未完成；后续玩家规模上来后可将排行榜升级为 Sorted Set 或定时榜单快照。

## 2026-05-15 v0.3 邮件/补偿基础版

- 优化目标：完成 v0.3 P2 的邮件/补偿基础闭环，让 GM 发放不只直接改资产，也可以通过邮件附件让玩家主动领取。
- 影响范围：新增 `mail` 后端模块、`mails` 数据表、GM 发送邮件接口、玩家邮件列表与领取接口、前端邮件页、GM 后台发送邮件表单、`doc/api.md`、`doc/PRD.md`、`doc/v0.3-plan.md`、`doc/development-checklist.md`、`doc/optimization-log.md`。
- 完成内容：新增 `GET /api/mails`、`POST /api/mails/{mailId}/claim`、`POST /api/admin/send-mail`；邮件附件支持金币和单个物品；领取时写入角色金币、背包、金币流水和掉落日志；邮件状态更新为已领取。
- 验证结果：`mvn test`、`mvn package -DskipTests` 通过。
- PRD 更新：已补充邮件/补偿基础版已完成，并将 v0.3 P2 剩余主线收敛为 WebSocket/SSE 推送。
- 后续问题：邮件系统后续可扩展全服邮件、过期时间、已读/删除和批量补偿。

## 2026-05-18 v0.3 SSE 战斗行动推送

- 优化目标：将在线普通战斗和 BOSS 战斗从前端定时 HTTP 轮询升级为服务端 SSE 推送，减少前端轮询控制复杂度并提升实时战斗体感。
- 影响范围：`BattleController` 新增战斗事件流接口，前端战斗页接入流式读取，`doc/api.md`、`doc/PRD.md`、`doc/v0.3-plan.md`、`doc/development-checklist.md`。
- 完成内容：新增 `GET /api/battles/{battleId}/stream`，服务端先推送当前状态，再按 `suggestedDelayMs` 自动推进并推送 `state/action/finished` 事件；前端使用带 `Authorization` 的 `fetch` 读取 SSE 流，流不可用时自动回退旧 `POST /api/battles/{battleId}/next` 轮询。
- 验证结果：`mvn test` 通过；后续继续执行打包和浏览器烟测。
- PRD 更新：已补充 SSE 战斗行动推送完成状态，并将 v0.3 P2 基础闭环标记完成。
- 后续问题：邮件系统可继续扩展全服邮件、过期时间和批量补偿；排行榜可在玩家规模上升后升级 Sorted Set 或定时榜单。

## 2026-05-18 v0.4 世界 BOSS 基础版

- 优化目标：进入 v0.4 玩法扩展，优先实现世界 BOSS 基础版，复用现有 BOSS、会话式战斗、SSE、邮件和排行能力。
- 影响范围：世界 BOSS 配置、状态与伤害日志表、世界 BOSS 后端接口、战斗结算分支、前端世界 BOSS 页面、PRD/API/规则/开发清单文档。
- 完成内容：新增世界 BOSS 列表、挑战、伤害排行；挑战结束累计伤害；击败后进入冷却并通过邮件发放排行奖励；普通 `/api/bosses` 流程保持不变。
- 验证结果：`mvn test` 通过；本地服务启动与接口/前端烟测继续执行；不执行打包和部署包导出。
- PRD 更新：已补充 v0.4 世界 BOSS 基础版完成状态。
- 后续问题：玩家规模上来后再评估 Redis Sorted Set 世界 BOSS 排行榜、公会协作、运营活动和拍卖行。

## 2026-05-18 v0.3 邮件/补偿进阶版

- 优化目标：补齐邮件系统的运营能力，支持全服邮件、批量补偿、过期时间、已读和删除。
- 影响范围：`mails` 表字段与迁移、邮件后端接口、GM 发邮件接口、玩家邮件页、GM 后台表单、`doc/api.md`、`doc/PRD.md`、`doc/v0.3-plan.md`、`doc/development-checklist.md`。
- 完成内容：邮件新增 `read_at`、`deleted`、`expires_at`；GM 发送邮件支持单人、批量角色 ID 和全服目标；玩家可标记已读、领取附件和删除已处理邮件；过期邮件不可领取。
- 验证结果：`mvn test` 和 `mvn package -DskipTests` 通过；本地 18080 服务重启需要后台启动授权，当前授权通道返回 503，待重新授权后执行启动与接口烟测。
- PRD 更新：已将邮件/补偿能力从基础版更新为进阶版。
- 后续问题：排行榜可在玩家规模上升后升级 Sorted Set 或定时榜单；v0.4 可进入公会、世界 BOSS、拍卖行和运营活动配置。
