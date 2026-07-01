# AGENTS.md — Android-first 徒步 App 开发指南

> 本文件给 Codex / coding agents 使用，建议放在仓库根目录。  
> 项目暂定名：`TrailTrust`。目标是生产级 **Android 原生徒步 App**，核心能力是可信路线、路线级离线包、离线轨迹导航、轨迹记录、安全与隐私。  
> 这不是 MVP，不要做成低配高德地图、运动打卡、泛户外社区或一次性 demo。

---

## 1. 产品北极星

构建一个面向中文徒步用户的 Android App：用户选择平台路线或导入 GPX/KML，下载路线级离线包，在弱网/无网/锁屏场景下沿轨迹导航，获得偏航、岔路、风险点和撤退点提醒，记录自己的轨迹，并默认私密地保存和分享安全信息。

核心价值：

1. **路线可信**：路线来源、置信度、最近反馈、风险标签、版本清楚。
2. **离线可用**：无网也能看路线、当前位置、航点、撤退点并继续记录。
3. **轨迹导航**：沿既定轨迹走，不做高德式任意两点自动算路。
4. **安全隐私**：行踪轨迹默认私密，分享显式、限时、可撤销。

---

## 2. Android-first 总边界

- 首发客户端是 **Android 原生 App**。
- 默认技术栈：**Kotlin + Jetpack Compose + MapLibre Native Android**。
- 除非任务明确要求，Codex 不得引入 React Native、Flutter、WebView 主架构或 iOS 工程。
- 地图能力以路线级离线包为核心，不做全国完整离线地图。
- 轨迹导航以 GPS + 路线轨迹线 + 本地算法实现，不依赖商业地图离线底图。
- 后台定位、低功耗、轨迹落库、权限流程、前台服务通知、离线包校验都是 P0，不可后补。

绝对禁止：

- 抓取、缓存、离线打包高德/百度/腾讯/天地图/OSM 官方在线瓦片。
- 默认公开用户轨迹、收藏、计划路线、导航 session 或实时位置。
- 把“最近路线点方向”说成“安全穿越路线”。
- 承诺自动救援、无信号实时定位或绝对安全。
- 在导航、离线、低电量、偏航、SOS 场景插入广告或强会员弹窗。
- 只在页面打开时记录轨迹；轨迹必须支持锁屏、后台、崩溃恢复。

---

## 3. Codex 工作规则

每次开始任务前：

1. 阅读本文件相关章节。
2. 检查现有 repo 结构、README、Gradle、CI、已有命令。
3. 不盲目重建项目；已有结构优先适配。
4. 输出简短计划：改哪些模块、不改哪些模块、风险点是什么。
5. 小步修改，避免一次跨 Android、后端、GIS、Admin 多层。
6. 对几何算法、离线包、权限、隐私、轨迹记录补测试。
7. 完成后运行相关 lint/test/build，并说明结果。

完成回复格式：

```text
Summary
- 改了什么
- 为什么这么改

Tests
- 运行了哪些命令
- 哪些通过
- 哪些没跑，原因是什么

Risks / Notes
- 仍需人工验证的点
- 可能影响安全、隐私、耗电、地图合规的点
```

---

## 4. 1.0 产品范围

### 4.1 P0 必须有

Android App：

- 路线列表、路线搜索、路线详情、收藏。
- 路线可信信息：来源、置信度、最近验证、近期反馈、风险标签、路线版本。
- GPX/KML 导入、解析、预览、保存、导航；基础 GPX 导出。
- 路线级离线包下载、续传、校验、版本管理、删除。
- 无网查看路线详情、轨迹线、等高线/海拔、关键 POI、撤退点、风险点。
- MapLibre 渲染本地 PMTiles/MBTiles 或等价路线级矢量包。
- 沿轨迹导航：当前位置、路线线、已走轨迹、进度、剩余距离、剩余爬升、预计耗时、下一个航点。
- 偏航提醒：GPS 精度过滤、持续偏离判断、返回路线点提示。
- 轨迹记录：锁屏继续、后台继续、前台服务通知、断点恢复、崩溃恢复。
- 原路返回：基于本地已记录轨迹反向展示。
- 出发前检查：离线包、GPS 权限、精确定位、前台服务、通知、电量、紧急联系人、天气快照。
- 紧急卡片：路线名、坐标、海拔、电量、最后更新时间、联系人、求助文本。
- Live Share：显式开启、限时、可撤销、显示最后同步时间。
- 默认隐私：轨迹、收藏、计划、导航 session 默认 private。
- 结构化反馈：封路、危险、补给、水源、厕所、岔路、适合新手/雨后等。
- 离线同步队列：无网产生的数据恢复网络后同步。

后端 / Admin / GIS：

- 路线 API、路线详情 API、离线包 manifest API。
- 轨迹、收藏、反馈、Live Share、同步队列基础 API。
- PostgreSQL + PostGIS 路线与地理对象存储。
- 离线包生成、发布、回滚、checksum。
- Admin 路线审核、风险点审核、举报处理、路线下架。
- 事故/投诉回溯：用户当时看到的路线版本、离线包版本、风险提示版本。

### 4.2 P1 上线时最好有

- 路线置信度评分模型。
- 路线版本更新提醒。
- 反向导航。
- 多路线对比。
- 撤退点规划。
- 电量续航估算。
- 天气快照与过期提醒。
- 队伍位置共享基础版。
- 轨迹复盘：计划偏差、停留点、慢速路段、偏航次数。
- 公开轨迹自动隐藏起终点附近区域。

### 4.3 1.0 不做

- 全国完整离线地图。
- 卫星影像离线。
- 城市道路完整离线搜索。
- 公交、驾车、骑行离线路径规划。
- 任意两点自动徒步规划。
- 偏航后自动重算路线。
- 高德式连续语音转向导航。
- 商城、赛事、跟团交易闭环。
- 泛信息流社区。
- 攀岩、冰雪、高海拔、洞穴、自驾穿越等专业高风险场景。

---

## 5. 默认技术选型

若 repo 已有成熟约定，优先遵循；若为空或早期，按以下实现。

### 5.1 Android

- 语言：Kotlin only。禁止新增 Java 业务代码，除非第三方 SDK 必须。
- UI：Jetpack Compose + Material 3。
- 构建：Gradle Kotlin DSL。
- 架构：MVVM + Repository + UseCase；核心算法独立 module。
- 异步：Kotlin Coroutines + Flow。
- DI：Hilt。
- 本地数据库：Room / SQLite。
- 轻量配置：DataStore。
- 网络：Retrofit + OkHttp；序列化用 kotlinx.serialization 或 Moshi，跟随现有项目。
- 地图：MapLibre Native Android。
- 离线地图：PMTiles / MBTiles / 自定义路线级 package；App 自己管理下载、校验、版本。
- 定位：以 Android `LocationManager` / GNSS 为 baseline；若接 Google Play Services Fused Location，必须放在 provider abstraction 后，并有无 GMS 降级路径。
- 活跃导航/记录：Foreground Service + location foreground service type。
- 后台非实时任务：WorkManager，用于上传、同步、清理、离线包下载续传；不要用 WorkManager 承担连续 GPS 采样。
- 日志：位置日志必须脱敏；crash report 不上传完整轨迹，除非用户单独同意。

### 5.2 推荐 Android 目录

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
```

单 Android repo 可使用：

```text
app/
core/
feature/
services/
docs/
scripts/
```

### 5.3 Backend / Admin / GIS

- API：Node.js TypeScript，框架用 NestJS / Fastify / Hono，优先现有约定。
- 数据库：PostgreSQL + PostGIS。
- 队列：BullMQ / Temporal / cloud queue。
- 对象存储：S3 compatible，用于 PMTiles/MBTiles/离线包/GPX/图片。
- Admin：Next.js / React Admin，用于路线审核、风险反馈、离线包发布、举报处理。
- GIS：Python + CLI 工具，处理 OSM PBF、DEM、GPX/KML、等高线、路线 buffer、离线包 manifest。

---

## 6. Android 架构原则

推荐分层：

```text
Compose UI
  ↓
ViewModel
  ↓
UseCase
  ↓
Repository
  ↓
LocalDataSource / RemoteDataSource / LocationProvider / MapPackageReader
  ↓
Room / DataStore / File System / API / Android Location APIs
```

规则：

- Compose 页面只负责展示和事件，不承载轨迹算法。
- ViewModel 不直接访问 Location API、大文件系统或网络细节。
- Foreground Service 与 UI 解耦；退出导航页后 session 仍可继续。
- navigation session state 必须可序列化和恢复。
- 轨迹点先本地落库，再异步同步。
- 所有 domain model 必须能被 JVM unit test 覆盖。

推荐模块职责：

```text
:core:model       领域模型
:core:geo         距离、bearing、最近线段匹配、偏航、进度、海拔
:core:offline     离线包 manifest、checksum、版本、解析
:core:location    LocationProvider、精度过滤、采样策略
:core:database    Room entities、DAO、migrations
:core:network     API client、DTO、同步队列协议
:core:map         MapLibre 封装、图层管理、本地 source 加载
:services:tracking Foreground Service、通知 actions、session 生命周期
:feature:navigation 导航 UI、偏航 UI、紧急卡片入口
```

---

## 7. 地图与离线包策略

### 7.1 路线级离线包

1.0 不做全国离线地图，只做路线级离线包。每个包至少包含：

- 主路线轨迹线。
- 路线周边 3–5 公里缓冲区步道网络。
- 简化底图或地形层。
- 等高线/海拔数据。
- 起点、终点、停车点、公交点。
- 水源、厕所、补给、村庄、露营点等关键 POI。
- 撤退点和备选撤退线。
- 风险点：岔路、涉水、落石、陡坡、封路、信号弱区。
- 路线说明、紧急卡片模板、manifest、checksum、版本信息。

### 7.2 数据来源规则

推荐：

- OSM 原始数据，用于自建步道/地形相关图层。
- DEM 数据，用于等高线、海拔曲线、累计爬升。
- 自有路线库和人工审核路线。
- 用户显式授权贡献的匿名化路况反馈。
- 合作领队/机构提供的路线。

禁止：

- 批量缓存商业地图瓦片。
- 使用 OSM 官方在线瓦片做离线包。
- 未授权批量搬运第三方路线、图片、评论。
- 将用户私密轨迹未经同意转成公开路线。

### 7.3 MapLibre 使用规则

- MapLibre 只负责地图渲染；轨迹导航算法由 App 自己实现。
- 本地 PMTiles/MBTiles 放在 App 私有目录，通过本地 file path 加载。
- App 必须自己做下载、续传、checksum、版本、空间不足处理。
- 不要假设 MapLibre 的 offline pack 能替代本项目路线级离线包系统。

### 7.4 坐标系统

- 自有路线、GPX、离线包、轨迹计算统一用 WGS84 经纬度存储。
- 地图渲染内部可使用 Web Mercator。
- 若接中国商业在线底图，GCJ-02/BD-09 转换必须隔离在 map adapter 层。
- 不得让未转换 WGS84 轨迹直接叠加在 GCJ-02 商业底图上。
- 坐标转换必须有测试。

### 7.5 离线包目录

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

### 7.6 状态机

```text
not_downloaded → queued → downloading → downloaded → verifying → verified
failed 可从 queued/downloading/verifying 进入
verified → stale → updating → verified
deleted 只能由用户删除或存储清理进入
```

只有 `verified` 状态才允许显示“可离线导航”。

---

## 8. 轨迹导航算法

### 8.1 1.0 支持

- 平台路线轨迹导航。
- GPX/KML 导入导航。
- 离线轨迹线展示。
- 当前 GPS 定位。
- 轨迹进度计算。
- 剩余距离、剩余爬升。
- 航点、岔路、风险点提醒。
- 偏航提醒。
- 原路返回。
- 实际轨迹记录。

不支持任意两点自动规划、偏航后自动重算、无路区域自动推荐穿越、高德式连续转向语音导航。

### 8.2 导航循环

```text
raw location
  ↓
精度过滤 / 漂移过滤
  ↓
投影到路线最近线段
  ↓
计算 crossTrackDistance
  ↓
结合历史 progress 防止环线/交叉线跳变
  ↓
计算已完成距离 / 剩余距离 / 剩余爬升
  ↓
检查下一个航点 / 岔路 / 风险点 / 撤退点
  ↓
判断偏航等级
  ↓
更新 navigation state
  ↓
落库实际轨迹点
  ↓
必要时触发通知、震动、语音或 UI 提醒
```

### 8.3 最近线段匹配

匹配结果必须包含：

- nearest point on route。
- segment index。
- progress distance。
- cross-track distance。
- bearing difference。

路线较长时使用空间索引或分段 bounding box，不要每次定位全量重算大对象。

### 8.4 环线、往返线、交叉线

- 不得只用“全路线最近点”决定进度。
- 必须结合最近历史 progress、用户移动方向、候选线段窗口、距离变化趋势。
- 用户反向行走时识别并提示是否切换反向导航。
- 往返路线允许相同空间位置对应不同进度。

### 8.5 偏航判断

偏航不要单点触发：

```text
candidate_off_route:
  crossTrackDistance > threshold
  AND locationAccuracy acceptable

confirmed_off_route:
  candidate_off_route 持续 30–60 秒
  OR 连续 N 个有效定位点都偏离
```

默认阈值：

```text
景区/城市公园：30–50m
普通山路：50–80m
林区/峡谷/GPS 不稳：80–120m
高风险岔路：30–50m
```

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

### 8.6 航点提醒

航点类型：`junction`、`summit`、`water`、`toilet`、`supply`、`risk`、`exit`、`viewpoint`、`transport`、`signal_weak`。

规则：

- 只提醒未来方向上的航点。
- 同一航点不要频繁重复提醒。
- 岔路、风险点、撤退点优先级高于风景点。
- 语音/震动可关闭，但风险/偏航视觉提示保留。

### 8.7 剩余爬升与原路返回

- 剩余爬升从当前 progress 之后的 elevation array 计算。
- 过滤海拔小幅噪声，避免累计爬升异常。
- 缺少 DEM 时显示“海拔数据不可用”，不要伪造。
- 原路返回使用本次已记录 track points 反向展示，不承诺自动规划安全路线。

---

## 9. 轨迹记录与 Foreground Service

### 9.1 生产级要求

轨迹记录必须满足：

- 锁屏后继续记录。
- 切后台后继续记录。
- 导航页被系统回收后继续记录。
- 前台服务通知常驻，明确说明正在记录/导航。
- 通知提供“暂停”“继续”“结束”“回到导航页”。
- App 崩溃或进程被杀后，重新打开能恢复未完成 session。
- 每个有效轨迹点尽快本地落库。
- 网络失败不丢轨迹。
- 低电量可降采样，但不能静默停止。

### 9.2 前台服务要求

- 活跃导航和轨迹记录必须使用 Foreground Service。
- manifest 必须声明合适 foreground service type，定位场景使用 `location` 类型。
- 启动前台服务前必须确保已经获得对应运行时位置权限。
- 不得在无前台服务的情况下尝试长时间后台定位。
- WorkManager 只用于下载/上传/清理等后台任务，不用于连续 GPS 采样。

### 9.3 通知要求

通知必须：

- 显示当前状态：正在导航 / 正在记录 / 已暂停 / GPS 弱。
- 显示路线名或“未命名轨迹”。
- 提供回到导航页的 PendingIntent。
- 提供暂停/继续/结束 action。
- 低电量和 GPS 弱时给出清楚提示。
- 不承载广告或营销。

Android 13+ 通知权限被拒时，仍要保证前台服务逻辑合规，并在 App 内提示用户开启通知可获得更可靠的导航状态提醒。

### 9.4 轨迹点字段

每个轨迹点至少包含：sessionId、lat/lng、altitude、accuracy、verticalAccuracy、speed、bearing、provider、recordedAt、batteryPercent、isMocked、appState。

### 9.5 采样策略

- 普通直线路段低频采样。
- 接近岔路、风险点、偏航状态提高频率。
- GPS 精度差时不要立即偏航报警。
- 用户静止时降低采样，保留心跳点。
- 低电量时提示低功耗模式。
- 支持 Normal、PowerSave、HighAccuracy 三种模式，并有测试。

---

## 10. 权限、隐私与安全

### 10.1 权限流程

场景化申请，不要启动即申请全部权限：

```text
浏览路线：不申请定位
附近路线：申请前台定位，可接受 approximate location
开始轨迹导航：申请 precise location
锁屏继续导航：说明 Foreground Service 和通知用途
Live Share：单独确认实时位置分享、有效期、可撤销
后台位置：仅在确需无界面持续追踪时单独申请，并提供关闭入口
```

用户拒绝后提供降级路径，不因拒绝非必要权限阻断基础路线查看。

### 10.2 隐私默认

- 轨迹默认 `private`。
- 收藏默认 `private`。
- 计划路线默认 `private`。
- 导航 session 默认 `private`。
- Live Share 默认关闭。
- 发布前必须预览公开内容。
- 公开轨迹默认隐藏起终点附近区域。
- 用户可设置隐私区域：家、公司、常用集合点。
- 用户可导出、删除轨迹和注销账号。

### 10.3 Live Share

Live Share 必须显式开启、有过期时间、可提前关闭、显示最后同步时间。无信号时显示最后同步状态，不伪装实时。链接不可无限期有效。

### 10.4 敏感个人信息

行踪轨迹、精准定位、实时位置都按敏感个人信息处理：最小必要、单独同意、明确用途、加密传输、严格权限控制。客服和后台不得随意查看完整轨迹；日志和 crash report 不上传完整轨迹；删除账号时清理或匿名化关联轨迹。

### 10.5 安全文案

必须说明：App 不能替代专业 GPS、纸质地图、指南针、卫星通信设备；路线、天气、封路、地质风险会变化；无信号区域无法保证实时位置分享；偏航返回提示不是自动安全路线；高风险路线需结伴、领队或专业装备。

---

## 11. Android UI / UX

### 11.1 主导航

建议底部 Tab：

1. 发现：路线搜索、附近路线、主题路线、收藏。
2. 规划：GPX 导入、自定义路线、离线包准备。
3. 导航：当前路线、轨迹记录、偏航、撤退、SOS。
4. 记录：历史轨迹、复盘、私人游记、贡献路况。
5. 我的：离线包、隐私、设备、会员、客服、数据导出。

不把泛社区信息流放到底部主 Tab。

### 11.2 导航页

导航页核心信息：地图、当前定位点、计划路线、已走轨迹、下一个航点、最近撤退点、风险点、剩余距离、剩余爬升、预计耗时、离线包状态、电量、暂停/结束、原路返回、紧急卡片。

导航页禁止广告弹窗、强会员弹窗、遮挡地图的社区/营销内容、非必要 onboarding。

### 11.3 出发前检查

开始导航前必须显示：路线轨迹、离线地图、等高线、POI/撤退点、GPS 权限、精确定位、前台服务通知、电量、紧急联系人、天气快照、路线置信度、最近路况反馈。

只有关键项通过时，才显示“可离线导航”。

### 11.4 无网体验

无网时必须能打开已验证离线包、查看路线详情、查看地图和轨迹线、查看当前位置、记录轨迹、偏航提醒、原路返回、查看紧急卡片、保存反馈到本地队列。

---

## 12. API 与同步

### 12.1 API 原则

- API 必须版本化：`/api/v1/...`
- 写入必须幂等，尤其是轨迹点批量同步、反馈提交、离线包状态回传。
- Android 客户端必须离线排队，恢复网络后同步。
- 同步失败不得丢失本地轨迹。
- 服务端返回必须包含路线版本、离线包版本、风险提示版本。

### 12.2 关键接口

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
```

### 12.3 本地同步事件

```text
track_session_started
track_points_recorded
track_session_paused
track_session_resumed
track_session_finished
route_feedback_created
offline_package_downloaded
offline_package_verified
live_share_location_updated
privacy_visibility_changed
```

每个事件必须有 idempotency key，可重试，可查看失败原因，不阻塞本地导航。

---

## 13. 核心数据模型

模型必须稳定、可版本化，并与 API、Room、GIS manifest 对齐。

- `Route`：id、名称、区域、距离、爬升、难度、来源、置信度、可见性、路线版本、离线包版本。
- `Waypoint`：id、routeId、类型、坐标、距离起点、标题、指令、风险等级、来源。
- `NavigationSession`：id、routeId、userId、开始/结束时间、状态、方向、离线包版本、最后进度、隐私可见性。
- `TrackPoint`：sessionId、坐标、海拔、精度、速度、方向、provider、时间、电量、是否 mock、App 状态。
- `Feedback`：routeId、类型、位置、描述、图片、创建时间、可见性、审核状态。

涉及坐标、海拔、距离、可见性、版本号的字段不得随意改名；改动必须包含迁移、测试和 API 兼容说明。

---

## 14. 后台与路线运营

Admin 必须支持：

- 新路线审核。
- GPX 清洗和质量检测。
- 路线合并和去重。
- 路线下架。
- 风险点新增、确认、关闭。
- 用户反馈处理。
- 内容举报处理。
- 离线包生成状态查看。
- 离线包发布/回滚。
- 路线置信度调整。
- 事故/投诉回溯。

路线置信度至少考虑：来源、最近完成次数、最近有效反馈时间、反馈一致性、封路/危险反馈、轨迹漂移和异常速度比例、是否近期人工审核。

UGC 规则：用户可以贡献路况，不默认公开完整轨迹；公开贡献尽量结构化；私密轨迹不得被后台直接转为公开路线；危险诱导、敏感地点、违法穿越、误导性路线必须可审核和下架。

---

## 15. 测试要求

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

后端测试覆盖鉴权、私密轨迹不可被他人读取、Live Share 过期、轨迹点批量写入幂等、离线包版本回滚、风险反馈审核流程。

GIS 测试覆盖 GPX 清洗、路线简化、DEM 海拔采样、路线 buffer 裁剪、manifest/checksum、坐标转换。

---

## 16. 构建与测试命令

若 repo 没有这些命令，新增或适配等价命令。

Android：

```bash
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew connectedDebugAndroidTest
./gradlew lintDebug
./gradlew detekt
```

后端：

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

Codex 不应假设命令一定存在；先检查 repo，再运行最相关命令。

---

## 17. 性能、错误与 Done Definition

### 17.1 性能指标

关注：冷启动、地图首次可交互、离线包打开时间、导航页掉帧、GPS 采样耗电、低电量模式、Foreground Service 存活、轨迹点丢失率、崩溃恢复成功率、离线包下载失败率、checksum 失败率、ANR。

基本目标：20km 常规路线轨迹匹配稳定运行；导航状态更新不阻塞 UI；定位点落库失败可重试；离线包损坏不导致崩溃；地图加载失败时仍尽量显示路线线和当前位置 fallback。

### 17.2 错误码

必须有明确错误状态：

```text
GPS_PERMISSION_DENIED
PRECISE_LOCATION_DENIED
BACKGROUND_LOCATION_DENIED
NOTIFICATION_PERMISSION_DENIED
FOREGROUND_SERVICE_FAILED
GPS_ACCURACY_POOR
OFFLINE_PACKAGE_MISSING
OFFLINE_PACKAGE_CHECKSUM_FAILED
OFFLINE_PACKAGE_VERSION_STALE
MAP_RENDER_FAILED
ROUTE_GEOMETRY_INVALID
TRACK_SESSION_RECOVERY_FAILED
SYNC_FAILED
LIVE_SHARE_EXPIRED
LIVE_SHARE_REVOKED
```

错误文案应帮助用户行动，例如“离线包校验失败，当前不能保证无网导航。请重新下载。”不要只显示 `Checksum error`。

### 17.3 Done Definition

功能完成必须满足：

- 符合产品边界。
- 有降级和错误状态。
- 隐私默认正确。
- 离线场景不崩溃。
- 关键逻辑有测试。
- 相关命令已运行或说明无法运行原因。
- 文档或注释更新。

---

## 18. 合规与文档

必须维护：

```text
docs/
  architecture.md
  android-location.md
  offline-packages.md
  navigation-algorithms.md
  map-compliance.md
  privacy.md
  api.md
  gis-pipeline.md
```

地图合规：不要自行宣称提供全国互联网地图服务；不要自行生产或发布未经合规确认的中国大陆完整底图；自建 OSM/DEM 路线级离线包上线前必须经过地图合规和法务确认；用户上传路线、POI、图片、反馈需要审核和举报通道。

隐私合规：行踪轨迹、精准定位、实时位置按敏感个人信息处理；隐私政策必须覆盖定位、轨迹、离线包、Live Share、UGC、第三方 SDK、日志、删除、导出、注销；第三方 SDK 必须有清单，不得静默采集定位或设备信息。

---

## 19. 里程碑

1. 工程骨架：Android 原生工程、Compose、Room/DataStore/Network/DI、MapLibre 空地图、CI。
2. 路线与 GPX：路线列表/详情、GPX 导入、路线线展示、距离/爬升/bounds 计算。
3. 离线包：manifest、下载/续传/checksum/版本、本地 PMTiles/MBTiles、出发前检查。
4. 轨迹导航：Foreground Service、落库、最近线段匹配、进度、偏航、原路返回。
5. 安全隐私：紧急卡片、Live Share、默认私密、起终点脱敏、删除/导出。
6. 运营闭环：路线审核、风险反馈、离线包生成发布、事故/投诉回溯。

---

## 20. 官方参考资料

- OpenAI Codex AGENTS.md：`https://developers.openai.com/codex/guides/agents-md`
- Android Foreground Service types：`https://developer.android.com/develop/background-work/services/fgs/service-types`
- Android location permissions：`https://developer.android.com/develop/sensors-and-location/location/permissions`
- Android background location：`https://developer.android.com/develop/sensors-and-location/location/background`
- MapLibre Android PMTiles：`https://www.maplibre.org/maplibre-native/android/examples/data/PMTiles/`
- 个人信息保护法：`https://www.cac.gov.cn/2021-08/20/c_1631050028355286.htm`

---

## 21. 最后提醒

Codex 在任何任务中都应优先维护这些底线：

```text
Android 原生优先。
路线级离线包优先。
沿轨迹导航优先。
轨迹默认私密。
后台定位必须显式、可见、可停止。
离线导航不能依赖在线地图瓦片。
偏航提醒不能承诺安全穿越。
导航中不能出现广告或强付费弹窗。
```
