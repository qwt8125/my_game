# Game Code Workspace

此目录是文字传奇页游的实际代码工作区。

## 当前技术栈

- OpenJDK 8
- Spring Boot 2.7.18
- Maven
- SQLite
- Redis 可选，用于在线战斗、在线状态、BOSS 冷却和排行榜短 TTL 缓存
- 原生 HTML/CSS/JavaScript 前端

## 目录结构

```text
game\
  pom.xml              Maven 工程配置
  data\                SQLite 本地数据
  scripts\             回归、部署、备份和诊断脚本
  src\
    main\
      java\            后端源码
      resources\
        config\        地图、怪物、装备、任务、掉落、技能、天赋等配置
        db\            SQLite schema
        static\        前端静态资源
```

## 常用验证命令

推荐日常直接跑统一回归：

```powershell
node scripts\regression-check.js
```

快速检查，不启动 smoke、不打包：

```powershell
node scripts\regression-check.js --quick
```

只跳过打包：

```powershell
node scripts\regression-check.js --skip-package
```

单项命令仍可直接运行：

```powershell
node --check scripts\smoke-test.js
node --check scripts\combat-balance-report.js
node scripts\combat-balance-report.js
mvn test
mvn package -DskipTests
```

自启动临时服务并跑接口 smoke：

```powershell
$env:SMOKE_START_SERVER='1'; $env:SMOKE_PORT='18081'; node scripts\smoke-test.js
```

检查打包 jar 是否被本地 Java 进程占用：

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File scripts\package-diagnostics.ps1
```
