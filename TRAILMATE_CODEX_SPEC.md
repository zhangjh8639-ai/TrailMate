# TrailMate Android 生产级开发规格

版本：2026-07-01
状态：重构基线
适用范围：Android 原生 App、后端 API、GIS/离线包、Admin 运营后台

> 本文档直接替代旧版 MVP 规格。旧版中“非导航产品”“Java 单体后端”“只做历史 GPX 评估”“不做实时导航/离线地图”等假设全部作废。
> 新方向是生产级 Android 原生徒步 App：以路线级离线包、GPS 轨迹导航、偏航提醒、轨迹记录、安全与隐私为核心。

---

## 1. 产品北极星

TrailMate 要解决的不是“记录一次运动”，而是在无信号、低电量、岔路多、天气变化、体力下降的户外场景中，让用户始终知道：

- 我在哪里。
- 我应该沿哪条既定轨迹走。
- 我离路线还有多远，是否偏航。
- 还剩多少距离、爬升和关键检查点。
- 出现异常时如何查看撤退点、原路返回和紧急信息。

一句话定位：

```text
一个让普通中文用户更安全完成徒步路线的离线导航与风险决策工具。
```

核心价值顺序：

1. 离线可信导航。
2. 偏航与风险预警。
3. 路线可信度、撤退点和安全准备。
4. 轨迹记录、复盘和结构化反馈。
5. 路线发现、装备建议和社区化能力。

---

## 2. 关键产品原则

### 2.1 必须坚持

- Android 原生优先，使用 Kotlin + Jetpack Compose。
- 以路线级离线包为核心，不依赖在线地图才能导航。
- 以“沿既定轨迹导航”为核心，不做任意两点自动规划。
- GPS 轨迹记录必须支持锁屏、后台、崩溃恢复。
- 偏航提示只提供“最近路线点方向和距离”，不承诺安全穿越。
- 用户轨迹、收藏、计划、实时位置默认私密。
- Live Share 必须显式开启、限时、可撤销。
- 任何路线、安全、装备、天气结论都要有置信度和依据。

### 2.2 明确不做

- 不做全国完整离线地图。
- 不做卫星影像离线。
- 不缓存高德、百度、腾讯、天地图、OSM 官方在线瓦片。
- 不做高德式驾车/公交/骑行/连续语音转向导航。
- 不做偏航后自动重算“安全路线”。
- 不做泛信息流社区、商城、赛事、跟团交易闭环。
- 不把装备做成“我的装备管理器”作为主线。
- 不承诺救援、无信号实时同步或绝对安全。

---

## 3. 目标用户与关键场景

### 3.1 目标用户

| 用户 | 特征 | 关键需求 |
| --- | --- | --- |
| 徒步新手 | 路线经验少，不会判断爬升、岔路、撤退点 | 出发前判断是否适合，行进中不迷路 |
| 周末徒步者 | 有一定经验，经常走郊野路线 | 离线地图、偏航提醒、轨迹记录 |
| 独行用户 | 安全焦虑高，担心无信号和家人联系不上 | 行程共享、紧急卡片、预计返回 |
| 带娃/结伴用户 | 节奏慢，风险容忍度低 | 路线难度、补给点、撤退点和低风险路线 |
| 进阶用户 | 想挑战长线或高爬升路线 | 离线包、轨迹导航、复盘和路线版本 |

### 3.2 关键场景

1. 出发前：选路线、看可信度、下载离线包、完成安全检查。
2. 行进中：看当前位置、方向、偏航、剩余距离、下个检查点。
3. 异常时：查看最近撤退点、原路返回、紧急卡片、共享最后位置。
4. 结束后：保存轨迹、复盘、反馈封路/风险/补给点。

---

## 4. 1.0 范围

### 4.1 P0 必须完成

Android App：

- 路线列表、搜索、详情、收藏。
- 路线可信信息：来源、置信度、版本、最近验证、近期反馈、风险标签。
- GPX/KML 导入、解析、预览、保存、导航。
- 路线级离线包下载、续传、校验、版本管理、删除。
- 无网查看路线、轨迹线、航点、POI、风险点、撤退点。
- MapLibre 渲染本地路线级 PMTiles/MBTiles 或等价矢量包。
- 沿轨迹导航：当前位置、计划路线、已走轨迹、进度、剩余距离、剩余爬升、预计耗时、下一个航点。
- 偏航提醒：GPS 精度过滤、持续偏离判断、最近路线点提示。
- 轨迹记录：前台服务、锁屏继续、后台继续、断点恢复、崩溃恢复。
- 原路返回：基于本次已记录轨迹反向展示。
- 出发前检查：离线包、GPS、精确定位、通知、电量、天气快照、紧急联系人。
- 紧急卡片：路线名、坐标、海拔、电量、最后更新时间、联系人、求助文本。
- Live Share：显式开启、限时、可撤销、显示最后同步时间。
- 离线同步队列：无网产生的数据恢复网络后同步。
- 默认隐私：轨迹、收藏、计划、导航 session 默认 private。

后端/API：

- 路线列表、路线详情、路线版本 API。
- 离线包 manifest API。
- GPX/KML 导入 API。
- 轨迹 session 与轨迹点批量同步 API。
- 收藏、反馈、Live Share、同步队列 API。
- 用户账户、登录、注销、数据导出、删除。
- 对象存储用于离线包、GPX、图片和缩略图。

GIS/运营：

- PostgreSQL + PostGIS 存储路线和地理对象。
- 离线包生成、checksum、发布、回滚。
- Admin 路线审核、风险点审核、反馈处理、路线下架。
- 事故/投诉回溯：用户当时看到的路线版本、离线包版本、风险提示版本。

### 4.2 P1 上线最好完成

- 路线置信度评分模型。
- 路线版本更新提醒。
- 反向导航。
- 多路线对比。
- 撤退点推荐。
- 电量续航估算。
- 天气快照与过期提醒。
- 队伍位置共享基础版。
- 轨迹复盘：计划偏差、停留点、慢速路段、偏航次数。
- 公开轨迹隐藏起终点附近区域。

### 4.3 P2 预留

- 品牌装备目录与路线装备匹配。
- 装备缩略图、品牌、型号、重量、适用温度/天气/路线类型。
- 用户不需要维护“我的装备”主概念，只在路线装备建议中从服务端品牌装备库选择匹配项。
- AI 只读取结构化路线、天气、用户能力、装备目录后生成解释和建议，不直接决定安全结论。
- 社区评价、图文游记、达人路线。

---

## 5. 信息架构与主流程

### 5.1 底部主导航

1. 发现：路线搜索、附近路线、主题路线、收藏。
2. 规划：GPX/KML 导入、离线包准备、出发前检查。
3. 导航：当前路线、轨迹记录、偏航、撤退、紧急卡片。
4. 记录：历史轨迹、复盘、结构化反馈。
5. 我的：离线包、隐私、安全联系人、设备、账户、数据导出。

不设置独立“装备”主 Tab。装备作为路线详情和出发前检查的一部分出现。

### 5.2 核心用户动线

```text
首次进入
  -> 注册/登录或游客浏览
  -> 选择路线/导入 GPX
  -> 查看路线详情与可信度
  -> 下载并校验路线级离线包
  -> 出发前检查
  -> 开始导航与轨迹记录
  -> 行进中偏航/风险/航点提醒
  -> 异常时查看撤退/原路返回/紧急卡片
  -> 结束轨迹
  -> 复盘与结构化反馈
  -> 同步到服务端
```

### 5.3 页面职责

| 页面 | 主要职责 | 禁止堆叠内容 |
| --- | --- | --- |
| 发现首页 | 快速找到可信路线 | 不放复杂导航状态 |
| 路线详情 | 路线风险、版本、离线包、检查点 | 不展示注册采集证据链 |
| 规划/导入 | 文件导入、路线预览、离线包准备 | 不做运动记录看板 |
| 出发前检查 | 判断能不能离线导航 | 不营销、不广告 |
| 导航页 | 地图、定位、轨迹、偏航、撤退 | 不混入数据分析和装备管理 |
| 记录页 | 历史轨迹、复盘、反馈 | 不承担实时导航 |
| 我的 | 隐私、离线包、账户、安全联系人 | 不成为功能垃圾桶 |

---

## 6. Android 技术架构

### 6.1 基础选择

- 语言：Kotlin only。
- UI：Jetpack Compose + Material 3。
- 构建：Gradle Kotlin DSL + Version Catalog。
- 架构：MVVM + Repository + UseCase。
- 异步：Kotlin Coroutines + Flow。
- DI：Hilt。
- 本地数据库：Room / SQLite。
- 本地配置：DataStore。
- 网络：Retrofit + OkHttp。
- 序列化：kotlinx.serialization。
- 地图：MapLibre Native Android。
- 后台任务：WorkManager。
- 连续定位：Foreground Service，不使用 WorkManager 承担 GPS 采样。
- 初始 applicationId：`com.trailmate.app`，发布前如需改名再统一迁移。

### 6.2 推荐仓库结构

```text
apps/android/
  app/
  core/
    model/
    geo/
    offline/
    database/
    datastore/
    network/
    location/
    map/
    permissions/
    testing/
  feature/
    discover/
    route_detail/
    planner/
    gpx_import/
    offline_packages/
    navigation/
    recording/
    safety/
    records/
    profile/
  services/
    tracking/
    sync/
docs/
  architecture.md
  android-location.md
  offline-packages.md
  navigation-algorithms.md
  map-compliance.md
  privacy.md
  api.md
  gis-pipeline.md
openspec/
```

如果后续确认做单 Android repo，也可以把 `apps/android/` 提升为根级 `app/ core/ feature/ services/`，但必须保持模块边界。

### 6.3 分层规则

```text
Compose UI
  -> ViewModel
  -> UseCase
  -> Repository
  -> LocalDataSource / RemoteDataSource / LocationProvider / MapPackageReader
  -> Room / DataStore / File System / API / Android Location APIs
```

规则：

- Compose 页面只负责展示和用户事件。
- ViewModel 不直接访问 Location API、网络和大文件系统。
- Foreground Service 与 UI 解耦。
- Navigation session state 必须可序列化、可恢复。
- 轨迹点先落本地库，再异步同步。
- 核心几何算法放在独立 module，可用 JVM unit test 覆盖。

### 6.4 推荐模块职责

| 模块 | 职责 |
| --- | --- |
| `:core:model` | Route、Waypoint、TrackPoint、OfflinePackage 等领域模型 |
| `:core:geo` | 距离、bearing、最近线段匹配、偏航、进度、海拔 |
| `:core:offline` | manifest、checksum、版本、离线包状态机 |
| `:core:location` | LocationProvider、精度过滤、采样策略 |
| `:core:database` | Room entity、DAO、migration |
| `:core:network` | API client、DTO、同步队列协议 |
| `:core:map` | MapLibre 封装、图层、本地 source 加载 |
| `:feature:navigation` | 导航 UI、偏航 UI、撤退、紧急卡片入口 |
| `:services:tracking` | Foreground Service、通知 action、session 生命周期 |
| `:services:sync` | 离线事件同步、重试、幂等 |

---

## 7. 地图与离线包

### 7.1 路线级离线包内容

每个路线级离线包至少包含：

- 主路线轨迹线。
- 路线周边 3-5 公里缓冲区步道网络。
- 简化底图或地形层。
- 等高线/海拔数据。
- 起点、终点、停车点、公交点。
- 水源、厕所、补给、村庄、露营点等 POI。
- 撤退点和备选撤退线。
- 风险点：岔路、涉水、落石、陡坡、封路、信号弱区。
- 路线说明、紧急卡片模板、manifest、checksum、版本信息。

### 7.2 本地目录

```text
/files/offline_packages/
  route_{routeId}_{packageVersion}/
    manifest.json
    map.pmtiles
    route_geometry.json
    route_simplified.json
    waypoints.json
    elevation.bin
    pois.json
    safety.json
    style.json
    checksum.txt
```

Room 只存索引和状态，不存大文件。

### 7.3 状态机

```text
not_downloaded -> queued -> downloading -> downloaded -> verifying -> verified
failed 可从 queued/downloading/verifying 进入
verified -> stale -> updating -> verified
deleted 只能由用户删除或存储清理进入
```

只有 `verified` 状态允许显示“可离线导航”。

### 7.4 数据与合规

允许：

- OSM 原始数据、自建地形层、DEM 数据。
- 自有路线库和人工审核路线。
- 用户显式授权贡献的匿名化路况反馈。
- 合作领队/机构提供的路线。

禁止：

- 批量缓存商业地图瓦片。
- 使用 OSM 官方在线瓦片做离线包。
- 未授权搬运第三方路线、图片、评论。
- 将用户私密轨迹未经同意转成公开路线。

坐标规则：

- 自有路线、GPX、离线包、轨迹计算统一 WGS84。
- 地图渲染内部可使用 Web Mercator。
- 如果接中国商业在线底图，GCJ-02/BD-09 转换隔离在 map adapter 层。
- 坐标转换必须有测试。

---

## 8. 轨迹导航算法

### 8.1 导航循环

```text
raw location
  -> 精度过滤 / 漂移过滤
  -> 投影到路线最近线段
  -> 计算 crossTrackDistance
  -> 结合历史 progress 防止环线/交叉线跳变
  -> 计算已完成距离 / 剩余距离 / 剩余爬升
  -> 检查下一个航点 / 岔路 / 风险点 / 撤退点
  -> 判断偏航等级
  -> 更新 navigation state
  -> 落库实际轨迹点
  -> 必要时触发通知、震动、语音或 UI 提醒
```

### 8.2 最近线段匹配输出

匹配结果必须包含：

- nearest point on route。
- segment index。
- progress distance。
- cross-track distance。
- bearing difference。

长路线需要使用空间索引或分段 bounding box，不得每次定位全量重算大对象。

### 8.3 环线、往返线、交叉线

- 不得只用“全路线最近点”决定进度。
- 必须结合最近历史 progress、用户移动方向、候选线段窗口、距离变化趋势。
- 用户反向行走时识别并提示是否切换反向导航。
- 往返路线允许相同空间位置对应不同进度。

### 8.4 偏航判断

偏航不要单点触发：

```text
candidate_off_route:
  crossTrackDistance > threshold
  AND locationAccuracy acceptable

confirmed_off_route:
  candidate_off_route 持续 30-60 秒
  OR 连续 N 个有效定位点都偏离
```

默认阈值：

| 场景 | 阈值 |
| --- | --- |
| 景区/城市公园 | 30-50m |
| 普通山路 | 50-80m |
| 林区/峡谷/GPS 不稳 | 80-120m |
| 高风险岔路 | 30-50m |

允许文案：

```text
你可能偏离路线约 70m。
最近路线点在西北方向约 110m。
请结合实际地形判断返回方式，不要直接穿越未知地形。
```

禁止文案：

```text
请直行 110m 返回路线。
这是一条安全返回路线。
```

---

## 9. 轨迹记录与前台服务

轨迹记录必须满足：

- 锁屏后继续记录。
- 切后台后继续记录。
- 导航页被系统回收后继续记录。
- 前台服务通知常驻，说明正在记录/导航。
- 通知提供“暂停”“继续”“结束”“回到导航页”。
- App 崩溃或进程被杀后，重新打开能恢复未完成 session。
- 每个有效轨迹点尽快本地落库。
- 网络失败不丢轨迹。
- 低电量可降采样，但不能静默停止。

轨迹点至少包含：

```text
sessionId
lat/lng
altitude
accuracy
verticalAccuracy
speed
bearing
provider
recordedAt
batteryPercent
isMocked
appState
```

采样模式：

- Normal：普通徒步。
- PowerSave：低电量和长线。
- HighAccuracy：岔路、风险点、偏航、复杂路线。

---

## 10. 权限、隐私与安全

### 10.1 权限申请

场景化申请，不启动即索要全部权限：

| 场景 | 权限策略 |
| --- | --- |
| 浏览路线 | 不申请定位 |
| 附近路线 | 前台定位，可接受 approximate location |
| 开始轨迹导航 | precise location |
| 锁屏继续导航 | 说明 Foreground Service 和通知用途 |
| Live Share | 单独确认实时位置分享、有效期、可撤销 |
| 后台位置 | 仅确有需要时单独申请，并提供关闭入口 |

### 10.2 隐私默认值

- 轨迹默认 private。
- 收藏默认 private。
- 计划路线默认 private。
- 导航 session 默认 private。
- Live Share 默认关闭。
- 发布前必须预览公开内容。
- 公开轨迹默认隐藏起终点附近区域。
- 用户可导出、删除轨迹和注销账号。

### 10.3 敏感信息处理

行踪轨迹、精准定位、实时位置按敏感个人信息处理：

- 最小必要。
- 单独同意。
- 明确用途。
- 加密传输。
- 严格权限控制。
- 日志和 crash report 不上传完整轨迹。
- 客服和后台不得随意查看完整轨迹。

---

## 11. 后端、数据与 API

### 11.1 默认服务端方向

- API：Node.js TypeScript，优先 NestJS 或 Fastify。
- 数据库：PostgreSQL + PostGIS。
- 队列：BullMQ / Temporal / 云队列，按部署复杂度选择。
- 对象存储：S3 compatible，用于离线包、GPX、图片、装备缩略图。
- Admin：Next.js / React Admin。
- GIS：Python + CLI 工具，处理 OSM PBF、DEM、GPX/KML、路线 buffer、manifest、checksum。

如果已有服务端成熟约定，优先遵循，但不得回到旧 Java MVP 规格。

### 11.2 API 原则

- API 版本化：`/api/v1/...`
- 写入幂等，尤其轨迹点批量同步、反馈、离线包状态回传。
- Android 客户端离线排队，恢复网络后同步。
- 同步失败不得丢失本地轨迹。
- 服务端返回路线版本、离线包版本、风险提示版本。

关键接口：

```text
GET    /api/v1/routes
GET    /api/v1/routes/:id
GET    /api/v1/routes/:id/offline-package
POST   /api/v1/routes/import-gpx
POST   /api/v1/tracks
POST   /api/v1/tracks/:id/points/batch
POST   /api/v1/feedback/route
POST   /api/v1/live-share
PATCH  /api/v1/live-share/:id/stop
POST   /api/v1/sync/events
GET    /api/v1/equipment/catalog
GET    /api/v1/equipment/recommendations
```

### 11.3 核心数据模型

- `Route`：id、名称、区域、距离、爬升、难度、来源、置信度、路线版本、离线包版本。
- `Waypoint`：id、routeId、类型、坐标、距离起点、标题、指令、风险等级、来源。
- `OfflinePackage`：id、routeId、version、status、checksum、size、manifestUrl、localPath。
- `NavigationSession`：id、routeId、userId、开始/结束时间、状态、方向、离线包版本、最后进度、隐私可见性。
- `TrackPoint`：sessionId、坐标、海拔、精度、速度、方向、provider、时间、电量、是否 mock、App 状态。
- `Feedback`：routeId、类型、位置、描述、图片、创建时间、可见性、审核状态。
- `EquipmentItem`：id、品牌、型号、分类、缩略图、重量、温度/天气/路线标签、来源、审核状态。
- `EquipmentRecommendation`：routeId、category、requiredLevel、reason、candidateItems。

---

## 12. 装备建议

装备不是主导航，也不是用户手动维护的“我的装备”系统。1.0 之后可做路线装备建议：

1. 服务端维护品牌装备目录。
2. 每个装备有缩略图、品牌、型号、分类、适用条件和审核状态。
3. 系统根据路线、天气、季节、夜间、海拔、风险点给出装备类别建议。
4. 用户在建议项里选择服务端装备库中的具体品牌型号。
5. 用户可以标记“本次携带/不携带”，但不建立复杂私有装备库。

装备建议必须低调，不干扰导航主链路。

---

## 13. 测试要求

Android unit test 必须覆盖：

- GPX/KML 解析。
- 路线距离计算。
- 累计距离表。
- 最近线段匹配。
- 环线进度防跳变。
- 偏航判断阈值。
- 剩余爬升计算。
- 航点提醒去重。
- 离线包 manifest 解析。
- checksum 校验。
- 隐私默认值。
- Live Share 过期逻辑。
- 同步队列幂等。

Android instrumentation / UI test 至少覆盖：

- 首次打开不强制定位。
- 开始导航时权限流程正确。
- 离线包校验失败时不能开始离线导航。
- 导航页无网可打开。
- 前台服务通知 action 正常。
- 结束导航后 session 正确保存。
- 轨迹默认私密。

后端测试覆盖：

- 鉴权。
- 私密轨迹不可被他人读取。
- Live Share 过期。
- 轨迹点批量写入幂等。
- 离线包版本回滚。
- 风险反馈审核流程。

GIS 测试覆盖：

- GPX 清洗。
- 路线简化。
- DEM 海拔采样。
- 路线 buffer 裁剪。
- manifest/checksum。
- 坐标转换。

---

## 14. 构建与验证命令

Android：

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest
./gradlew lintDebug
./gradlew detekt
```

服务端：

```bash
pnpm install
pnpm --filter api lint
pnpm --filter api typecheck
pnpm --filter api test
pnpm --filter api build
```

Admin：

```bash
pnpm --filter admin lint
pnpm --filter admin typecheck
pnpm --filter admin test
pnpm --filter admin build
```

GIS：

```bash
python -m pytest pipelines/gis
```

Codex 不应假设命令存在。每次任务先检查 repo，再运行最相关命令。

---

## 15. 里程碑

### M0. 环境与工程骨架

- Android SDK、JDK、Gradle Wrapper 配置。
- Android 原生工程。
- Compose + Material 3 主题。
- 基础模块结构。
- CI 构建。
- docs 与 OpenSpec 目录。

验收：干净机器按 README 可完成 `assembleDebug` 和基础 unit test。

### M1. 路线与 GPX/KML

- 路线列表和详情。
- GPX/KML 导入。
- 路线轨迹线展示。
- 距离、爬升、bounds、航点解析。
- 基础路线可信信息。

验收：真机可导入本地 GPX，并在无网状态查看路线线。

### M2. 离线包

- manifest。
- 下载、续传、checksum、版本。
- 本地 PMTiles/MBTiles 或等价路线级包。
- 出发前检查。

验收：飞行模式下打开已验证路线包，地图和路线可见。

### M3. 轨迹导航

- Foreground Service。
- GPS 精度过滤。
- 最近线段匹配。
- 进度、剩余距离、剩余爬升。
- 偏航提醒。
- 原路返回。

验收：真机锁屏/后台持续记录，偏航逻辑可用固定测试和现场测试验证。

### M4. 安全隐私

- 紧急卡片。
- Live Share。
- 默认私密。
- 起终点脱敏。
- 数据导出和删除。

验收：隐私默认值和 Live Share 过期逻辑有自动测试。

### M5. 运营闭环

- 路线审核。
- 风险反馈。
- 离线包生成发布。
- 事故/投诉回溯。
- 装备品牌目录与推荐接口。

验收：Admin 可审核路线和风险点，Android 可拉取装备缩略图与推荐。

### M6. 发布质量

- 性能、耗电、ANR、崩溃恢复。
- 真机矩阵测试。
- 地图合规检查。
- 隐私政策和第三方 SDK 清单。
- 灰度发布准备。

---

## 16. Done Definition

任一功能完成必须满足：

- 符合本规格和 AGENTS.md。
- 有明确降级和错误状态。
- 隐私默认正确。
- 离线场景不崩溃。
- 关键逻辑有测试。
- 已运行相关构建、测试或说明不能运行的原因。
- 文档或 OpenSpec 已更新。
- 没有提交密钥、真实轨迹、Token、本地配置。

---

## 17. 当前执行起点

本轮重构的第一阶段从 M0 开始：

1. 配置本机 Android 开发环境。
2. 建立 Android 原生工程骨架。
3. 固定 Gradle Wrapper 与依赖版本。
4. 建立基础模块、主题、空页面和测试命令。
5. 将后续功能变更用 OpenSpec 记录。

首个可验收目标：

```text
在 D:\workSpace\TrailMate 中，从干净 checkout 执行：
./gradlew assembleDebug
./gradlew testDebugUnitTest

两者可以通过，且 README 说明本机环境配置方式。
```
