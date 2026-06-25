# TrailMate Ubuntu 22.04 Docker Compose 部署方案

本文档用于在一台 Ubuntu 22.04 64 位服务器上部署 TrailMate 服务端。当前方案使用 Docker Compose 一键编排：

- `trailmate-server`：Spring Boot API，默认对外端口 `8080`。
- `postgres`：PostgreSQL 16，作为账号、身份、会话、审计等核心数据的事实源。
- `redis`：Redis 7，用于短信验证码 TTL、一次性消费、重发冷却和请求限流。

当前认证能力状态：

- 微信登录链路已接入服务端接口。真实微信登录需要 Android 端配置 `TRAILMATE_WECHAT_APP_ID`，服务端配置 `TRAILMATE_WECHAT_MODE=http` 和真实微信 `AppSecret`。
- 手机号登录接口、验证码校验、Redis 短期存储、PostgreSQL 账号/会话持久化已经具备。真实短信发送仍是 `NoopSmsCodeSender`，公测前必须替换为短信服务商适配器。
- 发送成功/失败的短信尝试会写入 PostgreSQL，验证码明文不会写入数据库。

## 1. 安装 Docker

```bash
sudo apt-get update
sudo apt-get install -y ca-certificates curl gnupg jq
sudo install -m 0755 -d /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg
sudo chmod a+r /etc/apt/keyrings/docker.gpg
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | \
  sudo tee /etc/apt/sources.list.d/docker.list > /dev/null
sudo apt-get update
sudo apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin
```

验证 Docker：

```bash
docker --version
docker compose version
```

## 2. 准备代码与配置

```bash
cd /opt
sudo git clone <your-repo-url> TrailMate
sudo chown -R "$USER":"$USER" TrailMate
cd TrailMate
cp .env.example .env
```

编辑 `.env`：

```dotenv
TRAILMATE_SERVER_PORT=8080

# 国内镜像源如果拉不到 alpine tag，使用标准 tag 更稳。
TRAILMATE_POSTGRES_IMAGE=postgres:16
TRAILMATE_REDIS_IMAGE=redis:7
TRAILMATE_JDK_IMAGE=eclipse-temurin:17-jdk
TRAILMATE_JRE_IMAGE=eclipse-temurin:17-jre

# 默认在 Docker 构建阶段编译服务端。网络慢时可切到 Dockerfile.prebuilt。
TRAILMATE_SERVER_DOCKERFILE=trailmate-server/Dockerfile

# Compose 会把 ./offline-basemaps/pmtiles 挂载到容器这个目录，用于服务端下载 PMTiles 文件。
TRAILMATE_OFFLINE_BASEMAP_PMTILES_DIRECTORY=/app/offline-basemaps/pmtiles

TRAILMATE_DB_NAME=trailmate
TRAILMATE_DB_USER=trailmate
TRAILMATE_DB_PASSWORD=<strong-postgres-password>

TRAILMATE_WECHAT_MODE=http
TRAILMATE_WECHAT_APP_ID=<wechat-open-platform-android-app-id>
TRAILMATE_WECHAT_APP_SECRET=<wechat-open-platform-app-secret>

TRAILMATE_AUTH_SMS_CODE_STORE_MODE=redis
TRAILMATE_REDIS_PASSWORD=<strong-redis-password>

# 仅限内网冒烟测试。生产环境必须留空。
TRAILMATE_AUTH_SMS_CODE_FIXED_CODE=
```

说明：

- 如果只是内网验证服务端接口，微信可暂时使用 `TRAILMATE_WECHAT_MODE=preview`。
- 如果短信服务商还没有接入，可临时设置 `TRAILMATE_AUTH_SMS_CODE_FIXED_CODE=123456` 做手机号登录冒烟测试。测试完成后必须清空。

## 3. 启动服务

```bash
docker compose config
docker compose pull postgres redis
docker compose up -d --build
docker compose ps
```

健康检查：

```bash
curl http://127.0.0.1:8080/actuator/health
```

预期返回包含：

```json
{"status":"UP"}
```

Docker Compose 部署模式会打开 PostgreSQL 和 Redis health check。本地纯内存 jar 运行模式默认关闭 DB/Redis health check。

装备目录接口冒烟检查：

```bash
curl -s http://127.0.0.1:8080/api/v1/gear/catalog/categories
curl -s "http://127.0.0.1:8080/api/v1/gear/catalog/search?q=beta"
```

预期返回中包含 `头灯`、`登山杖` 等分类，以及 `cat_rain_arcteryx_beta_lt`。

离线底图目录接口冒烟检查：

```bash
curl -s "http://127.0.0.1:8080/api/v1/offline-basemaps/pmtiles/catalog?minLongitude=120.05&minLatitude=30.10&maxLongitude=120.25&maxLatitude=30.35"
```

预期返回中包含 `pmtiles_hangzhou_westlake_osm_v1` 和 `downloadUrl`。当前这是 PMTiles
目录元数据，不代表服务器已经内置真实底图文件。Compose 会把宿主机目录：

```text
/opt/TrailMate/offline-basemaps/pmtiles
```

只读挂载到容器内：

```text
/app/offline-basemaps/pmtiles
```

如果你已经准备好真实 PMTiles 文件，例如：

```text
/opt/TrailMate/offline-basemaps/pmtiles/hangzhou-westlake.pmtiles
```

则可以直接验证下载 URL：

```bash
curl -I http://127.0.0.1:8080/offline-basemaps/pmtiles/hangzhou-westlake.pmtiles
```

生产环境也可以改为对象存储或 CDN，但 `downloadUrl` 必须保持为 Android 可下载地址。

### 3.1 镜像拉取失败处理

如果看到类似错误：

```text
failed to resolve reference "docker.io/library/postgres:16-alpine"
docker.io/library/postgres:16-alpine: not found
```

或者：

```text
dial tcp ... registry-1.docker.io:443: i/o timeout
```

先确认 Docker 镜像源：

```bash
cat /etc/docker/daemon.json
docker info | grep -A10 "Registry Mirrors"
```

阿里云 ECS 推荐使用阿里云容器镜像服务 ACR 的个人镜像加速地址。配置后重启 Docker：

```bash
mkdir -p /etc/docker
cp /etc/docker/daemon.json /etc/docker/daemon.json.bak 2>/dev/null || true

cat > /etc/docker/daemon.json <<'EOF'
{
  "registry-mirrors": [
    "https://你的阿里云镜像加速地址"
  ]
}
EOF

systemctl daemon-reload
systemctl restart docker
```

本项目默认使用更常见的标准 tag：

```dotenv
TRAILMATE_POSTGRES_IMAGE=postgres:16
TRAILMATE_REDIS_IMAGE=redis:7
TRAILMATE_JDK_IMAGE=eclipse-temurin:17-jdk
TRAILMATE_JRE_IMAGE=eclipse-temurin:17-jre
```

如果你的镜像源仍然拉不到，可以在 `.env` 中换成你自己的私有镜像仓库地址，例如：

```dotenv
TRAILMATE_POSTGRES_IMAGE=<your-registry>/library/postgres:16
TRAILMATE_REDIS_IMAGE=<your-registry>/library/redis:7
TRAILMATE_JDK_IMAGE=<your-registry>/library/eclipse-temurin:17-jdk
TRAILMATE_JRE_IMAGE=<your-registry>/library/eclipse-temurin:17-jre
```

然后重新执行：

```bash
docker compose build --pull trailmate-server
docker compose pull postgres redis
docker compose up -d --build
```

### 3.2 Gradle 下载太慢处理

默认 `trailmate-server/Dockerfile` 会在 Docker build 阶段执行：

```bash
./gradlew :trailmate-server:bootJar --no-daemon
```

如果服务器访问 `services.gradle.org` 很慢，可能卡在下载 Gradle 分发包。此时推荐改用“预构建 jar”方式：

1. 在开发机或网络更好的机器上构建服务端 jar：

```bash
./gradlew :trailmate-server:bootJar
```

生成文件：

```text
trailmate-server/build/libs/trailmate-server-0.1.0.jar
```

2. 上传这个 jar 到服务器同一路径：

```bash
mkdir -p /opt/TrailMate/trailmate-server/build/libs
scp trailmate-server/build/libs/trailmate-server-0.1.0.jar \
  root@<server-ip>:/opt/TrailMate/trailmate-server/build/libs/
```

3. 在服务器 `.env` 中切换 Dockerfile：

```dotenv
TRAILMATE_SERVER_DOCKERFILE=trailmate-server/Dockerfile.prebuilt
```

4. 构建并启动：

```bash
cd /opt/TrailMate
docker compose build trailmate-server
docker compose up -d
docker compose ps
```

这种方式不会在 Docker build 里下载 Gradle，只会把已有 jar 放进 JRE 运行镜像。

## 4. 数据库与数据持久化

### 4.1 PostgreSQL 用途

PostgreSQL 是 TrailMate 服务端的核心持久化数据库，当前认证和装备目录模块会使用这些表：

| 表名 | 用途 |
| --- | --- |
| `app_user` | TrailMate 统一用户账号 |
| `user_phone_identity` | 已验证手机号身份 |
| `user_wechat_identity` | 已验证微信身份 |
| `auth_refresh_token` | refresh token 哈希、轮换和撤销状态 |
| `auth_sms_code_attempt` | 短信验证码发送尝试记录，不保存验证码明文 |
| `user_consent` | 隐私、服务条款、地图 SDK 授权证据 |
| `auth_audit_event` | 登录、验证码请求、微信登录等认证审计事件 |
| `user_onboarding_profile` | 登录后收集的基础运动、户外、身体和负重档案，作为路线评估证据 |
| `gear_catalog_item` | 服务端维护的品牌装备目录，含类别、品牌、型号、重量、标签、图片 URL |
| `user_gear_inventory` | 用户拥有的装备关系；未删除行必须引用 `gear_catalog_item.catalog_item_id` |

完整表结构见：

[docs/database/trailmate-auth-schema.md](../database/trailmate-auth-schema.md)

装备图片不以二进制写入 PostgreSQL。数据库保存 `image_url` 和 `image_attribution`，图片文件应放在对象存储、CDN 或图床中。

离线底图目录当前是服务端内存种子，后续生产化时建议新增数据库表保存区域、边界框、
zoom 范围、文件大小、SHA-256 和 `download_url`。PMTiles 二进制文件不建议写入 PostgreSQL，
应放在 `/opt/TrailMate/offline-basemaps/pmtiles`、对象存储、CDN 或独立静态文件服务中。

首个迁移文件：

```text
trailmate-server/src/main/resources/db/migration/V1__create_auth_schema.sql
```

服务端依赖 Flyway。Compose 模式下 `trailmate-server` 会连接 PostgreSQL，并在启动时执行数据库迁移。

### 4.2 数据库连接配置

`docker-compose.yml` 中服务端使用以下连接：

```yaml
SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/${TRAILMATE_DB_NAME:-trailmate}
SPRING_DATASOURCE_USERNAME: ${TRAILMATE_DB_USER:-trailmate}
SPRING_DATASOURCE_PASSWORD: ${TRAILMATE_DB_PASSWORD:-trailmate_dev_password}
TRAILMATE_AUTH_PERSISTENCE_MODE: jdbc
TRAILMATE_GEAR_PERSISTENCE_MODE: jdbc
TRAILMATE_USER_PROFILE_PERSISTENCE_MODE: jdbc
```

`TRAILMATE_AUTH_PERSISTENCE_MODE=jdbc` 会启用：

- `JdbcAuthAccountRepository`
- `JdbcAuthSessionIssuer`
- `JdbcAuthAuditRecorder`
- `JdbcSmsCodeAttemptRecorder`

`TRAILMATE_GEAR_PERSISTENCE_MODE=jdbc` 会启用：

- `JdbcGearCatalogRepository`
- `JdbcGearInventoryRepository`

`TRAILMATE_USER_PROFILE_PERSISTENCE_MODE=jdbc` 会启用：

- `JdbcUserProfileRepository`

### 4.3 数据卷

Compose 会创建两个命名卷：

```text
trailmate-postgres-data
trailmate-redis-data
```

PostgreSQL 数据保存在：

```text
trailmate-postgres-data:/var/lib/postgresql/data
```

Redis 开启 AOF，数据保存在：

```text
trailmate-redis-data:/data
```

### 4.4 数据库验证命令

查看 PostgreSQL 容器状态：

```bash
docker compose ps postgres
```

进入数据库：

```bash
docker compose exec postgres psql \
  -U "$TRAILMATE_DB_USER" \
  -d "$TRAILMATE_DB_NAME"
```

查看表：

```sql
\dt
```

查看 Flyway 迁移记录：

```sql
select installed_rank, version, description, success, installed_on
from flyway_schema_history
order by installed_rank;
```

查看最近认证审计：

```sql
select event_type, provider, outcome, reason_code, created_at
from auth_audit_event
order by created_at desc
limit 20;
```

查看短信发送尝试：

```sql
select phone_e164, scene, provider, delivery_status, failure_code, created_at, expires_at
from auth_sms_code_attempt
order by created_at desc
limit 20;
```

查看装备目录：

```sql
select catalog_item_id, category, brand, model, image_url
from gear_catalog_item
where active = true
order by category, brand, model;
```

查看基础档案：

```sql
select user_id, exercise_frequency, typical_duration, experience_level, updated_at
from user_onboarding_profile
order by updated_at desc;
```

查看用户装备关系：

```sql
select inventory_item_id, user_id, catalog_item_id, custom, available, updated_at
from user_gear_inventory
where deleted_at is null
order by updated_at desc;
```

### 4.5 备份与恢复

备份：

```bash
mkdir -p backups
docker compose exec -T postgres pg_dump \
  -U "$TRAILMATE_DB_USER" \
  "$TRAILMATE_DB_NAME" > backups/trailmate-$(date +%Y%m%d-%H%M%S).sql
```

恢复到空库：

```bash
cat backups/<backup-file>.sql | docker compose exec -T postgres psql \
  -U "$TRAILMATE_DB_USER" \
  -d "$TRAILMATE_DB_NAME"
```

上线前至少做一次恢复演练，不要只做备份文件生成。

## 5. Redis 验证码与限流状态

Redis 在 Compose 部署中默认启用。它只保存短期运行状态，不作为账号事实源。

当前 key 设计：

```text
auth:sms:code:{phone_hash}       300 秒 TTL
auth:sms:cooldown:{phone_hash}   60 秒 TTL
auth:rate:phone:{phone_hash}     1 小时 TTL
auth:rate:ip:{ip_hash}           1 小时 TTL
```

验证 Redis：

```bash
docker compose exec redis redis-cli \
  -a "$TRAILMATE_REDIS_PASSWORD" \
  ping
```

预期返回：

```text
PONG
```

注意：

- Redis key 中使用哈希后的手机号/IP。
- Redis 不保存验证码明文。
- 成功登录会一次性消费验证码 key。

## 6. 认证冒烟测试

仅在内网环境执行。若真实短信服务商还未接入，请先临时设置：

```dotenv
TRAILMATE_AUTH_SMS_CODE_FIXED_CODE=123456
```

重启服务：

```bash
docker compose up -d --build
```

执行脚本：

```bash
TRAILMATE_BASE_URL=http://127.0.0.1:8080 \
TRAILMATE_SMOKE_PHONE=+8613800138000 \
TRAILMATE_SMOKE_CODE=123456 \
bash scripts/deployment/trailmate-auth-smoke.sh
```

脚本会验证：

- `/actuator/health` 返回 `UP`。
- 手机号验证码请求返回 `expiresInSeconds=300` 和 `retryAfterSeconds=60`。
- 手机号验证码登录返回 `provider=PHONE`。
- refresh token 可以轮换。
- logout 返回 HTTP `204`。
- 微信 preview 登录返回 `provider=WECHAT` 和非空 `wechatOpenId`。

对外开放前必须清空测试验证码：

```bash
grep -q '^TRAILMATE_AUTH_SMS_CODE_FIXED_CODE=$' .env
```

## 7. Android 构建参数

手机和服务器在同一局域网时，Android 构建参数示例：

```properties
TRAILMATE_SERVER_BASE_URL=http://<server-ip>:8080
TRAILMATE_WECHAT_APP_ID=<wechat-open-platform-android-app-id>
```

微信开放平台 Android 应用配置必须匹配：

- Package name：`com.trailmate.app`
- SHA1：安装到手机上的 APK 签名证书 SHA1

debug 和 release 的 SHA1 不一样，真机调试时要确认使用的是当前 APK 的签名。

## 8. 日志与日常运维

查看服务端日志：

```bash
docker compose logs -f trailmate-server
```

重启服务端：

```bash
docker compose restart trailmate-server
```

重新构建并启动：

```bash
docker compose up -d --build
```

查看容器状态：

```bash
docker compose ps
```

## 9. 上线前安全清单

- 使用 HTTPS，例如 Nginx 反向代理或云负载均衡。
- 替换 `.env` 中所有默认密码和占位值。
- 真实微信登录使用 `TRAILMATE_WECHAT_MODE=http`。
- 公测前接入真实短信服务商，不允许使用固定验证码。
- 清空 `TRAILMATE_AUTH_SMS_CODE_FIXED_CODE`。
- 定期备份 PostgreSQL，并做恢复演练。
- 不在日志中输出短信验证码、refresh token、微信 auth code、微信 AppSecret。
- 限制 PostgreSQL 和 Redis 端口只在 Docker 网络或可信内网内访问。
