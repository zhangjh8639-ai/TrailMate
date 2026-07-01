# AGENTS.md — 徒步 App 生产级开发指南

> 本文件给 Codex / coding agents 使用。目标是把需求、边界、架构、工程规范、验收标准一次性写清楚，避免后续反复返工。
>
> 项目代号：`TrailTrust`（可改名）。产品定位：面向中文徒步用户的“可信离线轨迹导航与路线规划 App”。

---

## 1. Agent 工作方式

### 1.1 总原则

- 先理解产品边界，再写代码。不要把本项目做成“低配高德地图”“泛户外社区”或“运动打卡 App”。
- 所有实现必须服务于四个核心价值：可信路线、离线可用、轨迹导航、安全隐私。
- 默认生产级，不做一次性 demo。涉及定位、离线地图、轨迹、隐私、安全提示、后台审核的能力必须按可上线标准设计。
- 遇到需求不明确时，先按本文档边界做最小合理决策，不要扩大范围。
- 不要引入违反地图服务条款、隐私合规或用户安全预期的实现。
- 不要为了快速完成而抓取、缓存或离线打包商业地图瓦片。

### 1.2 Codex 执行每个任务前必须做

1. 读本文件相关章节。
2. 检查现有 repo 结构、技术栈和命令，不要盲目重建。
3. 输出简短实现计划，说明会改哪些模块、哪些不改。
4. 小步提交式修改，避免一次改动跨越太多层。
5. 为业务逻辑、几何算法、离线包、权限/隐私相关逻辑补测试。
6. 完成后运行相关 lint/typecheck/test，并在最终说明中列出执行结果。

### 1.3 不允许的行为

- 不得使用高德/百度/腾讯/天地图/OSM 官方在线瓦片的批量缓存或离线打包作为生产方案。
- 不得默认公开用户轨迹、收藏、行程、实时位置。
- 不得把“最近路线点方向”描述成“安全穿越路径”。偏航返回必须提示用户结合实际地形判断。
- 不得承诺自动救援、无信号实时定位或绝对安全。
- 不得把广告弹窗、强制登录、强付费弹窗插入导航中、离线中、低电量中或紧急模式中。
- 不得在没有测试的情况下修改轨迹匹配、偏航判断、离线包校验、后台定位、隐私可见性逻辑。

---

## 2. 产品定位与范围

### 2.1 一句话定义

做一个面向中文徒步用户的可信离线轨迹导航 App：用户可以发现/规划/导入一条徒步路线，出发前下载路线级离线包，山里无网时沿轨迹导航、查看当前位置、偏航提醒、撤退点、剩余距离/爬升，并记录实际轨迹；个人轨迹默认私密。

### 2.2 首发用户

- 入门到中级徒步用户：周末徒步、城市周边、轻登山、5–20 公里路线。
- 高频徒步用户：需要 GPX、离线、海拔、轨迹复盘、路线可信度。
- 小队/领队用户：3–20 人队伍，需要行程分享、队员确认、位置共享、撤退方案。

### 2.3 1.0 地理边界

- 首发聚焦中国大陆热门徒步城市圈和路线级离线包。
- 不做全国完整离线地图。
- 海外路线先支持 GPX 导入、轨迹记录、基础离线包能力，不承诺完整路线库。

### 2.4 P0 功能范围

必须实现或预留清晰架构：

- 路线库：路线详情、难度、距离、累计爬升、预计耗时、路线类型。
- 路线可信度：来源、更新时间、近期反馈、风险标签、路线版本。
- GPX/KML 导入导出。
- 路线级离线包：轨迹、周边步道、等高线/海拔、POI、撤退点、风险点、路线说明。
- 离线包下载、完整性校验、版本检查、损坏恢复。
- 轨迹导航：当前位置、沿轨迹进度、剩余距离、剩余爬升、偏航提醒、航点提醒。
- 轨迹记录：后台记录、低功耗模式、断点恢复、GPS 漂移过滤。
- 安全能力：行程分享、紧急联系人、离线求助卡、原路返回、最近撤退点。
- 隐私能力：轨迹默认私密、分享显式授权、起终点脱敏、Live Share 限时可撤销。
- 结构化路况反馈：封路、危险、补给、岔路、适合人群、路面状态。
- 管理后台：路线审核、风险反馈处理、路线下架、用户举报、离线包发布。

### 2.5 1.0 明确不做

- 不做全国离线底图。
- 不做任意两点离线自动规划。
- 不做偏航后自动重算新路线。
- 不做装备商城、赛事系统、跟团交易闭环、泛信息流社区。
- 不做攀岩、冰雪、高海拔、洞穴、越野车、自驾穿越等高风险专业场景。
- 不做“官方救援调度”或“安全保证”。

---

## 3. 默认技术选型

若 repo 已有成熟技术栈，优先遵循现有栈；若 repo 为空或处于早期，按以下默认栈实现。

### 3.1 Monorepo

- 包管理：`pnpm`
- 语言：TypeScript 为主，GIS 数据管线可用 Python。
- 结构：mobile app、backend api、admin web、gis pipeline、shared packages。

建议目录：

```text
apps/
  mobile/              # React Native App，含必要原生模块
  api/                 # 后端 API
  admin/               # 路线/风控/内容审核后台
packages/
  domain/              # 共享领域模型、类型、校验 schema
  geo/                 # 轨迹匹配、距离、海拔、偏航算法
  offline-package/     # 离线包 manifest、校验、版本、解析
  ui/                  # 跨端 UI 组件（如适用）
pipelines/
  gis/                 # OSM/DEM/GPX 处理、PMTiles/MBTiles 生成
docs/
  architecture.md
  privacy.md
  offline-packages.md
  navigation-algorithms.md
  map-compliance.md
  api.md
```

### 3.2 Mobile

默认：React Native + TypeScript + 必要原生模块。

- 地图渲染：MapLibre Native / React Native MapLibre 封装。
- 离线地图：优先支持 PMTiles 或 MBTiles；路线级包优先，不做全国包。
- 本地存储：SQLite，用于路线包、轨迹点、导航 session、同步队列。
- 后台定位：iOS CoreLocation + Android Foreground Service；必须有显式权限说明和状态提示。
- 状态管理：轻量、可测试。不要把轨迹导航状态塞进不可测试的全局 UI 状态。
- 关键算法放在 `packages/geo`，不要散落在页面组件里。

### 3.3 Backend

默认：Node.js TypeScript API + PostgreSQL/PostGIS。

- API 框架：NestJS / Fastify / Hono 任选其一，优先现有项目约定。
- 数据库：PostgreSQL + PostGIS。
- 队列：BullMQ / Temporal / cloud queue，处理离线包生成、风险反馈、轨迹分析。
- 对象存储：S3 compatible，用于 PMTiles/MBTiles/离线包/GPX/图片。
- 缓存：Redis。
- 鉴权：JWT/session 均可，但轨迹、位置分享、后台权限必须严格隔离。

### 3.4 Admin

默认：Next.js / React Admin。

必须支持：

- 路线审核、编辑、合并、下架。
- 风险点审核、标注、关闭。
- 用户反馈处理。
- 离线包版本发布和回滚。
- 内容举报处理。
- 事故/投诉回溯：用户当时看到的路线版本、离线包版本、风险提示版本。

### 3.5 GIS Pipeline

默认：Python + CLI 工具组合。

- OSM PBF 处理：`osmium` / `pyosmium` / `ogr2ogr`。
- 矢量瓦片：`tippecanoe` / `tilemaker` / OpenMapTiles schema。
- 地形：Copernicus DEM / SRTM → 等高线、山影、海拔采样。
- 输出：路线级 `offline_package`，包含 manifest、tiles、route geometry、waypoints、POI、risk points。

---

## 4. 地图与离线包策略

### 4.1 核心原则

- 在线地图和离线导航解耦。
- 在线可使用合规商业地图或自建 OSM/MapLibre 底图。
- 离线不依赖高德离线地图，采用自建路线级离线包。
- 离线包不是单纯瓦片包，而是一次徒步所需的完整导航资料包。

### 4.2 离线包内容

每条路线的离线包至少包含：

```text
manifest.json
route.geometry     # 高精度路线折线
route.display      # 简化显示折线
route.metrics      # 累计距离、海拔数组、坡度、剩余爬升辅助数据
waypoints          # 岔路、山顶、补给、水源、厕所、停车、公交、撤退点
risk_points        # 涉水、落石、封路、陡坡、易迷路、信号弱区
pois               # 路线周边必要 POI
map_tiles          # 路线周边 3-5km 缓冲区矢量底图
contours           # 等高线/地形简图
safety_info        # 求助模板、紧急电话、路线注意事项
version.json       # 路线版本、地图版本、风险点版本、生成时间、hash
```

### 4.3 离线包状态

App 必须能展示：

- 可离线导航。
- 部分可用：缺少等高线/POI，但路线导航可用。
- 不可用：缺少主轨迹或 manifest 校验失败。
- 已过期：路线/风险点/地图数据有新版本。
- 已损坏：hash 不一致，需要重下。

### 4.4 离线包校验

下载完成不等于可用。必须校验：

- manifest schema 合法。
- 所有声明文件存在。
- hash/size 匹配。
- route geometry 非空、坐标合法、距离合理。
- navigation metrics 与 geometry 点数/区间匹配。
- map tiles 可被渲染引擎打开。

### 4.5 坐标系统

- 内部轨迹、GPX、离线包、算法统一使用 WGS84，经渲染时进入 Web Mercator。
- 若在线叠加中国商业地图底图，必须在地图适配层处理坐标转换，不得污染领域模型。
- 不要让同一页面混用未转换的 WGS84/GCJ-02/BD-09 数据。
- 坐标转换逻辑必须集中在 `packages/geo/coordinates` 并有测试。

### 4.6 授权与署名

- 使用 OSM 数据时必须保留 ODbL 署名、来源说明和数据贡献说明。
- 使用 DEM 数据时必须保留来源与许可说明。
- App 地图页、关于页、法律页必须展示地图/地形数据 attribution。
- 不要把商业地图的服务、图标、POI、瓦片、样式复制进自有离线包。

---

## 5. 领域模型

### 5.1 Route

路线不是一条线，而是一份出行决策资料。

字段建议：

```ts
type Route = {
  id: string;
  name: string;
  regionId: string;
  routeType: 'loop' | 'out_and_back' | 'point_to_point';
  distanceMeters: number;
  elevationGainMeters: number;
  elevationLossMeters: number;
  minElevationMeters?: number;
  maxElevationMeters?: number;
  estimatedDurationMin: number;
  difficulty: 'easy' | 'moderate' | 'hard' | 'expert';
  confidenceLevel: 'A' | 'B' | 'C' | 'D';
  sourceType: 'official' | 'partner_leader' | 'platform_verified' | 'user_uploaded' | 'aggregated';
  status: 'draft' | 'published' | 'archived' | 'temporarily_closed';
  visibility: 'public' | 'private' | 'unlisted';
  version: number;
  lastVerifiedAt?: string;
  lastCompletedAt?: string;
  createdAt: string;
  updatedAt: string;
};
```

### 5.2 Waypoint

```ts
type WaypointType =
  | 'trailhead'
  | 'junction'
  | 'summit'
  | 'viewpoint'
  | 'water'
  | 'toilet'
  | 'supply'
  | 'parking'
  | 'transit'
  | 'exit'
  | 'camp'
  | 'risk'
  | 'custom';
```

每个 waypoint 必须有 `distanceFromStartMeters`，便于离线导航和提醒。

### 5.3 RiskPoint

```ts
type RiskPoint = {
  id: string;
  routeId: string;
  type: 'wrong_turn' | 'river_crossing' | 'rockfall' | 'steep_slope' | 'muddy' | 'closed' | 'low_signal' | 'exposure' | 'wildlife' | 'other';
  severity: 'low' | 'medium' | 'high';
  lat: number;
  lng: number;
  distanceFromStartMeters: number;
  title: string;
  description?: string;
  source: 'admin' | 'user_report' | 'algorithm' | 'partner';
  status: 'active' | 'resolved' | 'needs_review';
  updatedAt: string;
};
```

### 5.4 NavigationSession

```ts
type NavigationSession = {
  id: string;
  userId: string;
  routeId: string;
  routeVersion: number;
  offlinePackageId?: string;
  startedAt: string;
  endedAt?: string;
  direction: 'forward' | 'reverse' | 'auto';
  mode: 'normal' | 'battery_saver' | 'return_track' | 'emergency';
  privacy: 'private';
  syncStatus: 'local_only' | 'queued' | 'synced' | 'failed';
};
```

### 5.5 TrackPoint

```ts
type TrackPoint = {
  sessionId: string;
  timestamp: string;
  lat: number;
  lng: number;
  altitudeMeters?: number;
  horizontalAccuracyMeters?: number;
  speedMps?: number;
  headingDegrees?: number;
  batteryPercent?: number;
  source: 'gps' | 'network' | 'fused';
};
```

---

## 6. 轨迹导航算法

关键算法必须可测试，放在 `packages/geo`。

### 6.1 导航循环

```text
定位更新
  → GPS 点过滤
  → 投影到路线最近线段
  → 计算离轨距离
  → 判断方向与进度
  → 计算已完成/剩余距离
  → 计算剩余爬升
  → 检查下一个航点/风险点/撤退点
  → 必要时触发震动/语音/弹窗
  → 本地保存轨迹点
```

### 6.2 最近线段匹配

- 路线 geometry 是折线，用户位置投影到最近线段。
- 返回：最近点坐标、线段 index、垂直距离、累计距离 progressMeters。
- 性能要求：长路线不得每次全量扫描。使用分段索引/R-tree/grid index 或窗口搜索。
- 必须支持环线、往返线、反向导航。

### 6.3 偏航判断

单点偏离不等于偏航。默认规则：

```text
普通山路：距离路线 > 60m 且持续 30s 以上 → 偏航提醒
GPS 不稳定：阈值自动提高到 80-120m
高风险岔路：可降到 30-50m
```

偏航分级：

- `soft`: 疑似偏离，静默或轻提示。
- `warning`: 明显偏离，提示返回最近路线点方向和距离。
- `critical`: 持续偏离且远离路线，建议停止前进、查看撤退点、联系队友。

文案必须避免“直行穿越”。使用：

> 最近路线点在西北方向 110m，请结合实际地形返回。

不要使用：

> 请直行 110m 返回路线。

### 6.4 进度与剩余距离

- 离线包预生成累计距离数组。
- 用户投影点落在线段内时，进度 = 线段起点累计距离 + 线段内距离。
- 环线和交叉路线要避免进度跳变：结合最近历史 progress、移动方向和距离窗口。

### 6.5 剩余爬升

- 使用路线采样点海拔数组。
- 小于阈值的海拔抖动不计入爬升，默认阈值 3–5m，可配置。
- 支持“前方 1km 爬升”和“剩余总爬升”。

### 6.6 航点提醒

触发条件：

- 距离 waypoint 小于阈值；默认 100–200m。
- 岔路/高风险点可提前 150–300m。
- 同一 waypoint 单次 session 只提醒一次，除非用户反向返回。

### 6.7 低功耗模式

- 直路、低风险路段降低 GPS 采样频率。
- 接近岔路、风险点、偏航状态、高速移动异常时提高采样频率。
- 低功耗模式必须保留偏航提醒和轨迹记录，不得静默关闭安全能力。

---

## 7. 移动端体验要求

### 7.1 核心页面

底部主导航建议：

1. 发现：路线搜索、附近路线、主题路线、收藏。
2. 规划：GPX 导入、自定义路线、离线包准备。
3. 导航：当前路线、轨迹记录、偏航、撤退、SOS。
4. 记录：历史轨迹、复盘、私人游记、贡献路况。
5. 我的：离线包、隐私、设备、会员、客服、数据导出。

### 7.2 导航页必须克制

导航页核心信息：

- 地图、当前位置、计划路线、已走轨迹、下一个航点、最近撤退点。
- 剩余距离、剩余爬升、预计耗时、离线包状态、电量。
- 一键：暂停/结束、偏航详情、原路返回、紧急卡片、标记问题。

导航中禁止：

- 广告弹窗。
- 强会员弹窗。
- 非必要引导。
- 遮挡地图的社区/营销内容。

### 7.3 出发前检查

开始导航前必须显示离线与安全检查：

- 路线轨迹已下载。
- 离线地图可打开。
- 等高线/POI/撤退点状态。
- GPS 权限、后台定位权限。
- 电量建议。
- 紧急联系人状态。
- 天气快照是否过期。
- 路线最近反馈和置信度。

### 7.4 隐私默认

- 轨迹默认 `private`。
- 收藏、计划、导航 session 默认私密。
- Live Share 必须显式开启、限时、可撤销。
- 发布轨迹前必须预览公开内容，并默认隐藏起终点附近区域。

---

## 8. API 与同步原则

### 8.1 API 风格

- API 必须版本化：`/api/v1/...`
- 数据写入必须幂等，尤其是轨迹点批量同步、反馈提交、离线包状态回传。
- 客户端必须能离线排队，恢复网络后同步。
- 不要因为同步失败丢失本地轨迹。

### 8.2 关键接口建议

```text
GET    /api/v1/routes
GET    /api/v1/routes/:id
GET    /api/v1/routes/:id/offline-package
POST   /api/v1/routes/import-gpx
POST   /api/v1/navigation-sessions
PATCH  /api/v1/navigation-sessions/:id
POST   /api/v1/navigation-sessions/:id/track-points:batch
POST   /api/v1/route-reports
POST   /api/v1/live-shares
DELETE /api/v1/live-shares/:id
GET    /api/v1/me/privacy-settings
PATCH  /api/v1/me/privacy-settings
```

### 8.3 同步队列

本地队列任务类型：

- track point batch upload。
- navigation session start/end。
- route report。
- offline package download status。
- privacy setting changes。

同步失败策略：

- 指数退避。
- 保留原始本地数据。
- 明确区分网络失败、鉴权失败、服务端拒绝。
- 轨迹点上传失败不得影响本地导航。

---

## 9. 隐私、安全与合规

### 9.1 个人位置数据

位置、轨迹、实时分享属于高敏感数据。实现要求：

- 最小必要采集。
- 分场景申请权限。
- 后台定位必须有清晰说明和系统状态提示。
- 用户能导出、删除、撤回分享、注销账号。
- 不因拒绝非必要权限而阻断基础浏览功能。
- 不将精确轨迹用于广告定向。

### 9.2 轨迹发布

发布前必须：

- 展示可见范围。
- 默认隐藏起终点附近 200–500m，可配置。
- 支持隐私区域：家、公司、常用集合点。
- 支持只贡献匿名路况，不公开完整轨迹。

### 9.3 Live Share

- 默认关闭。
- 用户主动开启。
- 默认限时过期。
- 可一键关闭。
- 链接权限应最小化，禁止永久公开实时位置。
- 展示最后更新时间，避免接收方误以为位置实时。

### 9.4 安全文案

所有路线详情、导航和紧急页面必须避免绝对承诺。推荐文案：

- “路线信息可能随天气、封路、施工、地质情况变化。”
- “无信号区域无法保证实时位置分享。”
- “App 不能替代专业装备、纸质地图、指南针和现场判断。”
- “请根据实际地形、天气和体力决定是否继续。”

---

## 10. 后台与运营系统

### 10.1 路线审核

后台必须支持：

- 创建/编辑路线。
- 上传 GPX，自动计算距离、爬升、海拔曲线。
- 标注 waypoint/risk point/exit point。
- 设置路线置信度。
- 发布/下架/临时封闭。
- 查看路线版本历史。

### 10.2 风险反馈处理

用户反馈类型：

- 封路。
- 危险点。
- 补给不存在。
- 岔路容易走错。
- 路线与实际不符。
- 难度不准。
- 垃圾内容/违规内容。

后台流程：

```text
用户提交 → 自动去重/聚合 → 待审核 → 审核通过 → 更新路线风险点/版本 → 触发离线包更新
```

### 10.3 离线包发布

离线包发布必须有：

- 生成任务状态。
- hash/size/manifest 校验。
- 灰度发布。
- 回滚。
- 客户端最低版本要求。
- 旧包失效策略。

---

## 11. 质量、测试与验收

### 11.1 必须测试的模块

- 坐标转换。
- 距离计算、bearing、polyline 投影。
- 路线进度计算。
- 偏航判断。
- 反向导航与环线进度。
- 海拔爬升去噪。
- 离线包 manifest 校验。
- GPX/KML 导入。
- 轨迹本地保存和断点恢复。
- 隐私可见性默认值。
- Live Share 过期和撤销。

### 11.2 测试数据

建立 `fixtures/geo`：

- 简单直线。
- 环线。
- 往返线。
- 交叉路线。
- GPS 漂移样本。
- 山谷低精度样本。
- GPX 点过密/过稀样本。
- 缺失海拔样本。
- 损坏离线包样本。

### 11.3 性能目标

- 导航页定位更新计算不阻塞 UI。
- 常规路线 20km 内轨迹匹配在移动端稳定运行。
- 离线包打开失败必须可恢复，不得崩溃。
- 轨迹记录持续 6 小时不应出现大量丢点或无法结束 session。
- 低电量模式要减少采样/渲染开销，但不能关闭关键安全提醒。

### 11.4 Done Definition

一个任务完成必须满足：

- 符合本文件范围边界。
- 有类型检查。
- 有必要单元/集成测试。
- 相关命令通过，或明确说明为何无法运行。
- 隐私、安全、离线场景没有退化。
- 文档/注释更新到位。
- 最终说明包含：改了什么、怎么验证、已知限制、后续建议。

---

## 12. 开发命令约定

如果 repo 没有这些命令，新增或适配等价命令。

```bash
pnpm install
pnpm lint
pnpm typecheck
pnpm test
pnpm test:geo
pnpm test:api
pnpm test:mobile
pnpm build
```

移动端常用：

```bash
pnpm --filter mobile ios
pnpm --filter mobile android
pnpm --filter mobile test
pnpm --filter mobile typecheck
```

后端常用：

```bash
pnpm --filter api dev
pnpm --filter api test
pnpm --filter api migrate
pnpm --filter api typecheck
```

GIS 管线常用：

```bash
pnpm --filter gis test
python -m pytest pipelines/gis
```

Codex 不应假设这些命令一定存在；先检查 `package.json`、README、CI 配置。

---

## 13. PR / Diff 规范

### 13.1 代码风格

- TypeScript 严格模式。
- 领域类型放共享包，不要每个页面重复定义。
- 几何算法纯函数优先，避免依赖 UI 或网络。
- API 输入输出使用 schema 校验。
- 错误必须可观测、可恢复、用户可理解。

### 13.2 提交说明

每次完成任务输出：

```text
Summary
- ...

Validation
- pnpm test:geo ✅
- pnpm typecheck ✅

Notes / Risks
- ...
```

### 13.3 需要特别谨慎 review 的改动

- 后台定位。
- 轨迹隐私。
- Live Share。
- 坐标转换。
- 偏航算法。
- 离线包下载/校验。
- 路线下架/封闭逻辑。
- 用户数据删除/导出。
- 订阅/付费弹窗与导航流程交叉处。

---

## 14. 未来拓展边界

P1 可做：

- 路线置信度自动评分。
- 撤退路线规划。
- 队伍位置共享。
- 电量续航估算。
- 天气快照。
- 轨迹复盘。
- 结构化路况贡献。

P2 可做：

- Apple Watch / Wear OS。
- Garmin/COROS/Strava/Apple Health 同步。
- 离线路由引擎：GraphHopper/Valhalla。
- 偏航后自动算路回主路线。
- AI 路线推荐。
- 领队 SaaS。
- 景区/国家公园合作。

这些不得提前污染 P0 架构，但要保留可扩展接口。

---

## 15. 首批里程碑建议

### Milestone 1 — 基础骨架

- Monorepo。
- Mobile 空壳 + 地图页。
- API 空壳 + PostGIS。
- domain/geo/offline-package 共享包。
- 基础 CI：lint/typecheck/test。

### Milestone 2 — 路线与 GPX

- GPX 导入解析。
- 路线详情。
- 距离/爬升/海拔曲线计算。
- 路线 geometry 存储。
- 管理后台基础路线编辑。

### Milestone 3 — 离线包

- offline package manifest。
- 本地下载、校验、打开。
- 路线级矢量底图/等高线占位或真实 pipeline。
- 离线状态页。

### Milestone 4 — 轨迹导航

- 当前定位。
- 最近线段匹配。
- 进度/剩余距离/剩余爬升。
- 偏航提醒。
- 航点提醒。
- 轨迹记录与断点恢复。

### Milestone 5 — 安全隐私

- 紧急联系人。
- 原路返回。
- Live Share 基础版。
- 轨迹默认私密。
- 起终点脱敏。
- 数据删除/导出接口。

### Milestone 6 — 运营闭环

- 结构化路况反馈。
- 后台审核。
- 风险点发布。
- 路线版本更新。
- 离线包更新提醒。

---

## 16. 任务模板

Codex 处理较大任务时按此模板回应和执行：

```text
Goal
- 本次要实现/修改什么。

Context checked
- 读了哪些文件/模块。

Plan
- 1. ...
- 2. ...

Out of scope
- 明确不做什么，避免范围扩大。

Validation
- 会运行哪些测试/命令。
```

实现完成后：

```text
Summary
- ...

Files changed
- ...

Validation
- ...

Known limitations
- ...
```

---

## 17. 最重要的产品判断

本项目不是要复刻高德、两步路或 AllTrails。它的核心是：

- 不追求地图最全，追求徒步时最可靠。
- 不追求社区最热闹，追求路线反馈最有用。
- 不追求功能最多，追求山里无网时还能工作。
- 不默认公开轨迹，保护用户行踪隐私。
- 不用商业地图灰色缓存，使用合规数据和自建路线级离线包。

任何实现如果偏离这五点，都应先停下来重新评估。
