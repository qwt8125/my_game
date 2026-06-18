# 开发环境约定（当前版）

## 1. 当前环境

本项目当前按本机已有环境开发，不强制升级 JDK。

```text
JDK: OpenJDK 1.8.0_312 Temurin
Maven: 3.6.3
Framework: Spring Boot 2.7.18
Database: SQLite
```

## 2. 版本选择原因

Spring Boot 3.x 需要更高版本 JDK，不适合当前“不升级本地环境”的约束。因此首版使用 Spring Boot 2.7.18。

## 3. 编码约束

- Java 源码保持 Java 8 兼容。
- 不使用 Java 9+ 模块系统。
- 不使用 `var`、`record`、`sealed class`、switch expression。
- 日期时间优先使用 `java.time`。
- Web 参数校验可使用 Spring Boot 2 对应的 Validation 能力。

## 4. 后续升级策略

首版不做 JDK 升级。后续如果需要长期运营，可以单独安排升级任务：

```text
Java 8 + Spring Boot 2.7
  -> Java 17/21 + Spring Boot 3
```

升级时重点检查：

- `javax.*` 到 `jakarta.*` 的包名变化。
- Spring Security 配置变化。
- 第三方依赖版本兼容性。
- 构建和部署脚本。
