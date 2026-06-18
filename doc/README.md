# 文档入口

这里保留当前开发最常用的文档。历史方案、视觉稿、长日志已经归档到 `doc/archive/20260520-doc-slimming/`，需要追溯时再看。当前下一步开发计划以 `roadmap.md` 和 `development-checklist.md` 为准。

## 推荐阅读顺序

1. `PRD.md`：产品范围、当前完成状态、后续方向。
2. `roadmap.md`：下一阶段优先级和待办。
3. `development-checklist.md`：当前开发闭环清单。
4. `api.md`：后端接口。
5. `game-rules.md`：玩法、数值和规则说明。
6. `architecture.md`：技术结构和数据流。
7. `deployment-linux.md`：部署说明。
8. `development-environment.md`：本地环境。
9. `optimization-log.md`：精简优化摘要。

## 文档维护原则

- 主目录只放长期维护文档。
- 阶段方案、临时分析、视觉稿和超长流水记录放入 `archive/`。
- 新功能完成后优先更新 `PRD.md`、`roadmap.md`、`development-checklist.md` 和 `optimization-log.md`。
- 如果任务涉及图片生成，优先执行用户个性化方案中的自定义指令；如与通用流程冲突，以该自定义指令为准。除非用户明确提出更高清、更大尺寸、原图留存等特殊要求，后续生成图片默认压缩到 50KB 以内再落地。
- 如果确实需要新增专题文档，完成落地后应归档或合并回核心文档。
