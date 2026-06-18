# Linux 轻量部署说明（当前版，至 v2.8）

## 1. 运行环境

首版沿用当前开发环境约束：

```text
OpenJDK 8
Spring Boot 2.7.18
SQLite
```

服务器推荐目录：

```text
/opt/text-legend/
  legend-game.jar
  data/
    game.db
  logs/
  backup/
  application-prod.yml
```

## 2. 打包

在 `game` 目录执行：

```bash
mvn -DskipTests package
```

产物：

```text
target/legend-game-0.1.0-SNAPSHOT.jar
```

## 3. 启动

开发或单次启动：

```bash
java -jar legend-game.jar --spring.profiles.active=prod
```

指定数据库路径：

```bash
GAME_DB_PATH=/opt/text-legend/data/game.db java -jar legend-game.jar --spring.profiles.active=prod
```

默认端口为 `8080`。

## 4. 生产配置

生产配置示例位于：

```text
game/src/main/resources/application-prod.yml
```

可通过环境变量覆盖：

```text
SERVER_PORT
GAME_DB_PATH
LOG_PATH
REDIS_URL
REDIS_DATABASE
GAME_ADMIN_USERNAMES
BATTLE_SESSION_STORE
RANKING_CACHE_TTL_SECONDS
```

常用生产配置建议：

- `SERVER_PORT`：服务端口，默认 `8080`。
- `GAME_DB_PATH`：SQLite 数据库路径，建议放在 `/opt/text-legend/data/game.db`。
- `LOG_PATH`：应用日志路径，默认 `./logs/legend-game.log`。
- `REDIS_URL`：Redis 连接，默认 `redis://127.0.0.1:6379/15`。
- `REDIS_DATABASE`：Redis DB 编号，默认 `15`。
- `GAME_ADMIN_USERNAMES`：管理员用户名列表，默认 `admin`，多账号按逗号分隔。
- `BATTLE_SESSION_STORE`：战斗会话存储，默认 `redis`；无 Redis 的单机环境可切换为 `sqlite`。
- `RANKING_CACHE_TTL_SECONDS`：排行榜短缓存 TTL，默认 `60` 秒。

## 5. systemd 示例

复制示例文件：

```text
game/scripts/text-legend.service.example
```

到服务器：

```bash
sudo cp text-legend.service.example /etc/systemd/system/text-legend.service
sudo systemctl daemon-reload
sudo systemctl enable text-legend
sudo systemctl start text-legend
```

查看状态：

```bash
sudo systemctl status text-legend
```

查看日志：

```bash
journalctl -u text-legend -f
```

## 6. SQLite 备份

备份脚本：

```text
game/scripts/backup-sqlite.sh
```

执行：

```bash
chmod +x backup-sqlite.sh
./backup-sqlite.sh /opt/text-legend/data/game.db /opt/text-legend/backup
```

建议用 crontab 每天备份一次：

```bash
0 3 * * * /opt/text-legend/backup-sqlite.sh /opt/text-legend/data/game.db /opt/text-legend/backup
```

## 7. 健康检查

```bash
curl http://127.0.0.1:8080/api/health
```

期望响应：

```json
{
  "success": true,
  "code": "OK",
  "message": "success",
  "data": {
    "status": "UP",
    "service": "legend-game"
  }
}
```

## 8. 运行注意事项

- SQLite 数据库文件必须放在可写目录。
- 不要把生产库提交到代码仓库。
- 发布前备份 `game.db`。
- 当前版本已经接入 Redis，在线战斗会话、在线状态、BOSS 冷却缓存和排行榜短 TTL 缓存默认写入 Redis 15 号库。
- Redis 默认配置为 `redis://127.0.0.1:6379/15`；资产、战斗日志和完整 actions 仍写入 SQLite。
- 如果部署环境暂时没有 Redis，建议先设置 `BATTLE_SESSION_STORE=sqlite`，并确认排行榜缓存、在线状态和 BOSS 冷却读取异常时会回落到 SQLite 事实数据。
- 排行榜缓存 TTL 可通过 `RANKING_CACHE_TTL_SECONDS` 调整，默认 60 秒。
