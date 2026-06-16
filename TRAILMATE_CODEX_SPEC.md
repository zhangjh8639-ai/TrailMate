# TrailMate（山行教练）MVP 产品与技术开发说明书

**Codex 执行版｜历史轨迹驱动的个性化徒步路线评估与计划 App**

- 版本：V1.0
- 日期：2026-06-15

---

## 0. 文档定位与使用方式

本说明书用于指导 Codex 在一个全新代码仓库中完成 TrailMate MVP。它同时承担产品需求文档（PRD）、技术设计说明书、接口契约、算法规则、测试计划和开发任务清单的作用。

> **一句话目标：** 用户导入至少 3 条历史徒步 GPX 和 1 条目标路线 GPX，系统根据个人距离、爬升、坡度速度与体能衰减，生成路线匹配度、预计用时、风险路段、休息检查点和补给检查点。

Codex 执行时必须以本说明书为最高优先级需求。出现实现细节歧义时，优先选择：可测试、可解释、离线友好、隐私最小化、不过度承诺安全或医疗结论的方案，并将决定记录在 docs/adr/。

### 0.1 已确定的产品与技术假设

| 编号 | 决定 | 说明 |
| --- | --- | --- |
| D-01 | Android 优先 | 第一版只交付 Android 客户端；iOS 不在 MVP 范围。 |
| D-02 | 核心闭环优先 | 不复制两步路的社区、活动、商城和海量路线库。 |
| D-03 | GPX 优先 | MVP 只支持 GPX；KML/KMZ 放入后续版本。 |
| D-04 | Java 单体后端 | 算法先在 Spring Boot 内实现，不额外拆 Python 服务。 |
| D-05 | 无大模型依赖 | MVP 的路线判断必须由确定性算法与规则完成；LLM 只作为未来解释层。 |
| D-06 | 不输出虚假概率 | 数据不足时输出匹配等级与可信度，不宣称“完成概率 87%”。 |
| D-07 | 非导航产品 | MVP 提供路线预览和海拔剖面，不提供转向导航、救援承诺或全国离线地图。 |
| D-08 | 版本锁定 | 初始化时选择当前稳定且兼容的依赖版本，写入版本目录并锁定，开发中不得自动升级。 |

### 0.2 Codex 执行原则

- 按第 16 章的里程碑顺序开发；一次只完成一个任务卡。
- 任何业务算法必须先写测试样例，再写实现。
- 每次提交必须可编译、测试通过，并更新 CHANGELOG.md。
- 不得擅自增加社交、付费、聊天 Agent、自动救援、实时导航等功能。
- 涉及位置和用户轨迹的数据，默认私有；任何共享能力都不在 MVP 中。
- 算法结论必须保存“输入、版本、依据和解释”，保证可复现。

## 1. 产品概述

### 1.1 产品背景

现有户外 App 往往提供统一的路线难度、公共轨迹和导航信息，但徒步新手真正缺少的是“这条路线对我而言是否合适”。同一条 15 公里路线，对于有稳定爬升经验的人和只走过城市绿道的人，完成难度完全不同。TrailMate 通过用户自己的历史轨迹建立能力画像，让路线评估从“路线是几星”变成“你是否适合”。

### 1.2 目标用户

| 用户类型 | 特征 | 核心问题 |
| --- | --- | --- |
| 徒步新手 | 完成过少量短途徒步，不会判断距离与爬升 | 我能不能走完？什么时候应该休息？ |
| 进阶徒步者 | 有多条轨迹，希望挑战更难路线 | 我的能力提升了吗？哪一段会掉速？ |
| 谨慎型用户 | 重视天黑前结束和下撤边界 | 最晚几点必须到达检查点？ |

### 1.3 产品价值主张

- 个人化：使用用户自己的速度、爬升和衰减规律，而不是群体平均值。
- 可解释：每条结论都说明依据，例如“后半程仍有 300 米爬升，超过你近期稳定范围”。
- 保守安全：数据不足时降低可信度并给出保守建议，不输出确定性安全承诺。
- 持续学习：用户完成路线并反馈后，能力画像重新计算。

### 1.4 MVP 成功指标

| 指标 | 定义 | MVP 目标 |
| --- | --- | --- |
| 预计用时误差 | 实际移动/总耗时与预测区间的偏差 | 多数测试路线实际用时落入预测区间或偏差不超过 20% |
| 风险路段命中 | 用户标记困难路段与系统预测风险路段重合 | 内测阶段人工复核可解释 |
| 建议有用度 | 用户对计划的 1-5 分评分 | 平均不低于 3.5 |
| 二次使用意愿 | 用户完成一次评估后再次导入目标路线 | 内测用户中可观测 |

## 2. 范围定义

### 2.1 MVP 必须实现

| ID | 功能 | 优先级 |
| --- | --- | --- |
| FR-001 | 用户注册、登录、退出和删除账户 | P0 |
| FR-002 | 导入历史 GPX，显示质量检测结果 | P0 |
| FR-003 | 历史活动列表与活动分析详情 | P0 |
| FR-004 | 基于历史活动生成个人能力画像 | P0 |
| FR-005 | 导入目标路线 GPX | P0 |
| FR-006 | 生成路线匹配等级、可信度和解释 | P0 |
| FR-007 | 生成分段预计时间、风险路段、休息与补给检查点 | P0 |
| FR-008 | 保存评估与计划，支持离线查看最近数据 | P0 |
| FR-009 | 徒步后录入是否完成、实际时长、疲劳评分和困难路段 | P1 |
| FR-010 | 根据反馈重新计算能力画像 | P1 |
| FR-011 | 导出/删除用户数据 | P1 |

### 2.2 明确不做

- 公共路线社区、动态、评论、点赞、关注和约伴。
- 商城、广告、付费会员、赛事报名。
- 实时转向导航、全国离线底图、卫星通信和救援调度。
- 医疗诊断、脱水诊断、热射病诊断或强制补给剂量。
- 聊天机器人、多 Agent、自动写游记。
- iOS、Web 管理端和穿戴设备接入。
- 基于群体大数据的完成概率模型；MVP 只做个人历史规则模型。

### 2.3 第二阶段预留

- 后台 GPS 轨迹记录、偏航提醒和本地语音提醒。
- 补水、能量补给、短休、身体不适等快捷事件。
- 天气、日落时间、路线封闭和地形标签服务。
- Health Connect、HealthKit 和厂商运动数据。
- 经过真实数据训练的预计用时与路线完成模型。

## 3. 核心用户流程

### 3.1 首次使用

1. 用户注册并阅读位置数据与轨迹隐私说明。
2. 用户选择初始经验等级：新手、规律徒步、经验丰富；该值只用于数据不足时的保守默认。
3. 用户导入 3 条以上历史 GPX。
4. 系统逐条完成质量检测与特征提取，并提示哪些记录可用于能力画像。
5. 系统生成第一版能力画像，展示样本数和可信度。

### 3.2 目标路线评估

1. 用户导入目标路线 GPX。
2. 系统解析路线距离、爬升、坡度、连续爬升和后半程负荷。
3. 用户补充可选信息：预计出发时间、背包重量、是否独行、地形标签。
4. 系统基于当前画像模拟分段用时，生成匹配等级和可信度。
5. 用户查看主要依据、主要风险、预计时间区间和计划检查点。
6. 用户将计划保存到本地，离线可查看。

### 3.3 徒步后反馈

1. 用户选择对应目标路线并填写：是否完成、实际总时长、主观疲劳 1-5、最困难路段。
2. 若有实际 GPX，可再次导入并关联到原计划。
3. 系统比较计划与实际差异，更新画像版本。
4. 旧评估保留原画像版本，避免历史结果随画像变化而不可复现。

## 4. 页面与交互需求

### 4.1 页面清单

| 页面 | 关键内容 | 主要操作 |
| --- | --- | --- |
| 登录/注册 | 邮箱、密码、隐私协议 | 注册、登录、找回入口（MVP 可仅预留） |
| 首页 | 画像摘要、历史记录数、最近评估 | 导入历史、评估新路线 |
| GPX 导入 | 文件选择、用途、解析进度 | 选择历史活动/目标路线 |
| 导入结果 | 质量分、异常项、核心指标 | 确认保存或放弃 |
| 历史活动列表 | 名称、日期、距离、爬升、是否可用于画像 | 查看详情、删除 |
| 活动详情 | 轨迹预览、海拔剖面、速度、停留、分段表 | 标记完成情况、编辑疲劳评分 |
| 能力画像 | 稳定距离/爬升/时长、坡度速度、衰减曲线、可信度 | 重新计算、查看依据 |
| 目标路线详情 | 距离、爬升、连续爬升、后半程负荷 | 填写附加条件、发起评估 |
| 评估结果 | 匹配等级、可信度、预计时间、依据、风险 | 生成/查看计划 |
| 行程计划 | 分段到达时间、休息检查点、补给检查点、风险段 | 离线保存、填写完成反馈 |
| 设置与隐私 | 账户、数据导出、删除账户、算法说明 | 导出、删除、退出 |

### 4.2 首页状态

| 状态 | 展示 |
| --- | --- |
| 无历史数据 | 主按钮“导入历史轨迹”，解释至少 3 条记录的原因。 |
| 1-2 条有效记录 | 展示临时画像，可信度 LOW，继续引导导入。 |
| 3 条以上 | 展示稳定能力摘要和“评估新路线”。 |
| 存在失败导入 | 显示可恢复的错误卡片，不阻塞其他功能。 |

### 4.3 评估结果展示规范

> **禁止文案：** 不得显示“保证完成”“绝对安全”“你已脱水”“必须摄入精确剂量”等结论。

| 字段 | 示例 | 规则 |
| --- | --- | --- |
| 匹配等级 | 较适合 / 谨慎尝试 / 暂不建议 | 由确定性风险分数映射。 |
| 可信度 | 低 / 中 / 高 | 与样本数量、数据质量、坡度覆盖相关。 |
| 预计用时 | 6 小时 40 分 - 7 小时 50 分 | 给区间，不给单一精确值。 |
| 主要依据 | 距离在稳定范围内，但爬升略高 | 至少 2 条，最多 5 条。 |
| 主要风险 | 最后 4 公里仍有 310 米爬升 | 按影响降序。 |
| 建议 | 在长爬升前进行补给检查 | 使用“建议/检查”，避免医疗命令。 |

## 5. 总体技术架构

```text
┌──────────────── Android App ────────────────┐
│ Jetpack Compose                             │
│ ViewModel + UseCase + Repository            │
│ Room：离线缓存画像、路线、评估和计划          │
│ 文件选择器：读取 GPX                         │
└───────────────────┬─────────────────────────┘
                    │ HTTPS / JSON / Multipart
┌───────────────────▼─────────────────────────┐
│ Spring Boot 3.x / Java 21                   │
│ Auth / GPX Import / Analysis / Profile      │
│ Route Assessment / Plan / Feedback          │
│ OpenAPI / Flyway / Validation               │
└───────────────┬─────────────────────────────┘
                │
┌───────────────▼─────────────────────────────┐
│ PostgreSQL + PostGIS                        │
│ 用户、轨迹点、分段特征、画像版本、评估与计划  │
└─────────────────────────────────────────────┘
```

### 5.1 技术选型

| 层 | 技术 | 要求 |
| --- | --- | --- |
| Android | Kotlin、Jetpack Compose、Coroutines、Room、Retrofit/OkHttp | minSdk 26；依赖版本集中管理并锁定。 |
| 架构 | MVVM + Clean-ish 分层 | UI 不直接访问网络或数据库。 |
| 后端 | Java 21、Spring Boot 3.x、Spring Security、Bean Validation | 单体模块化，不提前微服务化。 |
| 数据访问 | Spring Data JDBC/JPA 二选一，或 MyBatis | 全项目统一，禁止混用多套 ORM。 |
| 数据库 | PostgreSQL 16+、PostGIS 3.x、Flyway | 轨迹点用 geometry(Point,4326)。 |
| API | REST / OpenAPI 3 | 错误响应统一；生成客户端或 DTO。 |
| 测试 | JUnit 5、Testcontainers、MockWebServer、Compose UI Test | 核心算法覆盖率优先。 |
| 构建 | Gradle Kotlin DSL、Docker Compose | 一条命令启动本地依赖。 |

### 5.2 仓库目录

```text
trailmate/
├─ README.md
├─ CHANGELOG.md
├─ docker-compose.yml
├─ docs/
│  ├─ product-spec.md
│  ├─ api/
│  ├─ adr/
│  └─ test-data/
├─ server/
│  ├─ build.gradle.kts
│  └─ src/
│     ├─ main/java/com/trailmate/
│     │  ├─ auth/
│     │  ├─ user/
│     │  ├─ gpx/
│     │  ├─ activity/
│     │  ├─ profile/
│     │  ├─ route/
│     │  ├─ assessment/
│     │  ├─ plan/
│     │  ├─ feedback/
│     │  └─ common/
│     └─ test/
├─ android-app/
│  ├─ build.gradle.kts
│  └─ app/src/main/java/com/trailmate/app/
│     ├─ core/
│     ├─ data/
│     ├─ domain/
│     ├─ feature/auth/
│     ├─ feature/home/
│     ├─ feature/importgpx/
│     ├─ feature/activity/
│     ├─ feature/profile/
│     ├─ feature/assessment/
│     ├─ feature/plan/
│     └─ feature/settings/
└─ scripts/
   ├─ dev-up.sh
   ├─ dev-down.sh
   └─ seed-demo-data.sh
```

## 6. 后端模块设计

| 模块 | 职责 | 禁止职责 |
| --- | --- | --- |
| auth | 注册、登录、JWT、密码哈希、当前用户 | 不承载业务数据查询。 |
| gpx | 流式解析、质量检测、清洗、标准化 | 不直接计算用户画像。 |
| activity | 历史活动生命周期、轨迹点和分段保存 | 不输出目标路线建议。 |
| profile | 从可用历史活动生成画像版本 | 不修改原始轨迹。 |
| route | 目标路线导入与路线特征 | 不读取用户隐私之外的数据。 |
| assessment | 画像与路线匹配、风险解释 | 不生成医疗建议。 |
| plan | 分段预计到达、休息和补给检查点 | 不承诺导航与救援。 |
| feedback | 完成结果和主观反馈，触发画像重算 | 不覆盖历史画像版本。 |

### 6.1 分层约束

```text
Controller -> Application Service -> Domain Service -> Repository

Controller：协议转换、鉴权、校验，不写算法。
Application Service：编排事务和用例。
Domain Service：纯业务规则，尽量无框架依赖，可单元测试。
Repository：持久化接口与实现。
```

## 7. 数据模型

### 7.1 关系概览

```text
app_user 1---N hike_activity 1---N track_point
                         └---N track_segment
app_user 1---N capability_profile
app_user 1---N target_route 1---N route_segment
capability_profile + target_route -> route_assessment
route_assessment 1---1 hike_plan 1---N plan_checkpoint
hike_plan 1---N completion_feedback
```

### 7.2 核心表

以下为逻辑模型。Codex 应使用 Flyway 生成可执行 SQL，并为 user_id、activity_id、route_id、recorded_at 和空间字段建立必要索引。

```text
CREATE TABLE app_user (
  id                UUID PRIMARY KEY,
  email             VARCHAR(320) NOT NULL UNIQUE,
  password_hash     VARCHAR(255) NOT NULL,
  experience_level  VARCHAR(20) NOT NULL,
  created_at        TIMESTAMPTZ NOT NULL,
  updated_at        TIMESTAMPTZ NOT NULL,
  deleted_at        TIMESTAMPTZ
);

CREATE TABLE file_asset (
  id            UUID PRIMARY KEY,
  user_id       UUID NOT NULL REFERENCES app_user(id),
  original_name VARCHAR(255) NOT NULL,
  content_type  VARCHAR(100) NOT NULL,
  size_bytes    BIGINT NOT NULL,
  sha256        CHAR(64) NOT NULL,
  storage_path  VARCHAR(500) NOT NULL,
  created_at    TIMESTAMPTZ NOT NULL
);

CREATE TABLE hike_activity (
  id                 UUID PRIMARY KEY,
  user_id            UUID NOT NULL REFERENCES app_user(id),
  source_file_id     UUID REFERENCES file_asset(id),
  name               VARCHAR(200) NOT NULL,
  started_at         TIMESTAMPTZ,
  ended_at           TIMESTAMPTZ,
  distance_m         DOUBLE PRECISION NOT NULL,
  ascent_m           DOUBLE PRECISION NOT NULL,
  descent_m          DOUBLE PRECISION NOT NULL,
  elapsed_seconds    BIGINT,
  moving_seconds     BIGINT,
  point_count        INTEGER NOT NULL,
  quality_status     VARCHAR(20) NOT NULL,
  usable_for_profile BOOLEAN NOT NULL,
  completion_status  VARCHAR(20) NOT NULL DEFAULT 'COMPLETED',
  fatigue_rating     SMALLINT,
  created_at         TIMESTAMPTZ NOT NULL
);

CREATE TABLE track_point (
  id            BIGSERIAL PRIMARY KEY,
  activity_id   UUID NOT NULL REFERENCES hike_activity(id) ON DELETE CASCADE,
  sequence_no   INTEGER NOT NULL,
  recorded_at   TIMESTAMPTZ,
  location      geometry(Point, 4326) NOT NULL,
  elevation_m   DOUBLE PRECISION,
  speed_mps     DOUBLE PRECISION,
  is_outlier    BOOLEAN NOT NULL DEFAULT FALSE,
  UNIQUE(activity_id, sequence_no)
);

CREATE TABLE track_segment (
  id               BIGSERIAL PRIMARY KEY,
  activity_id      UUID NOT NULL REFERENCES hike_activity(id) ON DELETE CASCADE,
  segment_no       INTEGER NOT NULL,
  start_sequence   INTEGER NOT NULL,
  end_sequence     INTEGER NOT NULL,
  distance_m       DOUBLE PRECISION NOT NULL,
  duration_seconds INTEGER,
  elevation_gain_m DOUBLE PRECISION NOT NULL,
  elevation_loss_m DOUBLE PRECISION NOT NULL,
  average_grade    DOUBLE PRECISION,
  average_speed_mps DOUBLE PRECISION,
  elapsed_ratio    DOUBLE PRECISION,
  grade_bucket     VARCHAR(30),
  is_stop          BOOLEAN NOT NULL DEFAULT FALSE,
  UNIQUE(activity_id, segment_no)
);
```

```text
CREATE TABLE capability_profile (
  id                    UUID PRIMARY KEY,
  user_id               UUID NOT NULL REFERENCES app_user(id),
  version_no            INTEGER NOT NULL,
  sample_count          INTEGER NOT NULL,
  confidence_level      VARCHAR(10) NOT NULL,
  stable_distance_m     DOUBLE PRECISION,
  stable_ascent_m       DOUBLE PRECISION,
  stable_duration_sec   BIGINT,
  grade_speed_json      JSONB NOT NULL,
  fatigue_curve_json    JSONB NOT NULL,
  evidence_json         JSONB NOT NULL,
  algorithm_version     VARCHAR(30) NOT NULL,
  created_at            TIMESTAMPTZ NOT NULL,
  UNIQUE(user_id, version_no)
);

CREATE TABLE target_route (
  id                     UUID PRIMARY KEY,
  user_id                UUID NOT NULL REFERENCES app_user(id),
  source_file_id         UUID REFERENCES file_asset(id),
  name                   VARCHAR(200) NOT NULL,
  distance_m             DOUBLE PRECISION NOT NULL,
  ascent_m               DOUBLE PRECISION NOT NULL,
  descent_m              DOUBLE PRECISION NOT NULL,
  max_continuous_ascent_m DOUBLE PRECISION NOT NULL,
  final_third_ascent_m    DOUBLE PRECISION NOT NULL,
  highest_elevation_m     DOUBLE PRECISION,
  quality_status          VARCHAR(20) NOT NULL,
  terrain_tags            JSONB NOT NULL DEFAULT '[]',
  created_at              TIMESTAMPTZ NOT NULL
);

CREATE TABLE route_assessment (
  id                    UUID PRIMARY KEY,
  user_id               UUID NOT NULL REFERENCES app_user(id),
  route_id              UUID NOT NULL REFERENCES target_route(id),
  profile_id            UUID NOT NULL REFERENCES capability_profile(id),
  match_score           INTEGER NOT NULL,
  match_level           VARCHAR(30) NOT NULL,
  confidence_level      VARCHAR(10) NOT NULL,
  estimated_min_sec     BIGINT,
  estimated_max_sec     BIGINT,
  risk_factors_json     JSONB NOT NULL,
  evidence_json         JSONB NOT NULL,
  algorithm_version     VARCHAR(30) NOT NULL,
  created_at            TIMESTAMPTZ NOT NULL
);

CREATE TABLE hike_plan (
  id                  UUID PRIMARY KEY,
  assessment_id       UUID NOT NULL UNIQUE REFERENCES route_assessment(id),
  planned_start_time  TIMESTAMPTZ,
  estimated_finish_min TIMESTAMPTZ,
  estimated_finish_max TIMESTAMPTZ,
  summary_json        JSONB NOT NULL,
  created_at          TIMESTAMPTZ NOT NULL
);

CREATE TABLE plan_checkpoint (
  id                    UUID PRIMARY KEY,
  plan_id               UUID NOT NULL REFERENCES hike_plan(id) ON DELETE CASCADE,
  sequence_no           INTEGER NOT NULL,
  checkpoint_type       VARCHAR(30) NOT NULL,
  route_distance_m      DOUBLE PRECISION NOT NULL,
  estimated_arrival_min TIMESTAMPTZ,
  estimated_arrival_max TIMESTAMPTZ,
  message               VARCHAR(500) NOT NULL,
  reason_json           JSONB NOT NULL,
  UNIQUE(plan_id, sequence_no)
);
```

### 7.3 枚举约定

| 枚举 | 值 |
| --- | --- |
| quality_status | GOOD / PARTIAL / POOR |
| completion_status | COMPLETED / PARTIAL / ABORTED / UNKNOWN |
| confidence_level | LOW / MEDIUM / HIGH |
| match_level | RECOMMENDED / CAUTION / NOT_RECOMMENDED |
| checkpoint_type | REST_CHECK / HYDRATION_CHECK / ENERGY_CHECK / RISK_START / TURNAROUND_CHECK |
| experience_level | BEGINNER / REGULAR / EXPERIENCED |

## 8. GPX 解析与轨迹分析

### 8.1 输入限制

- 只接受 .gpx；服务端同时校验扩展名、MIME 和 XML 根节点。
- 单文件默认不超过 50 MB，轨迹点不超过 500,000；限制必须可配置。
- 使用 StAX/SAX 流式解析，禁止用 DOM 将大文件一次性载入内存。
- 坐标按 WGS84 处理；缺少时间的目标路线仍可评估，但缺少时间的历史记录不能用于速度画像。
- 原文件保存前计算 SHA-256；相同用户重复导入相同文件时提示重复。

### 8.2 清洗流程

1. 解析 trk/trkseg/trkpt，保留原始段边界。
2. 删除完全重复点：坐标、时间均相同。
3. 标记异常跳点：相邻点推导速度超过 12 m/s，或单步距离超过 1 km 且时间不足 2 分钟。异常点不参与统计，但保留审计。
4. 海拔使用 5 点中值滤波；首尾使用可用窗口。
5. 累计爬升/下降只累计平滑后绝对变化不小于 3 米的增量，阈值配置化。
6. 移动时间：速度大于 0.5 km/h 的区间累计；若连续至少 120 秒且活动范围小于 25 米，判定为停留。
7. 历史轨迹按累计水平距离约 200 米切分；目标路线按约 100-200 米切分，段边界必须保留明显坡度变化。

### 8.3 基础公式

```text
水平距离：Haversine(lat1, lon1, lat2, lon2)
坡度 grade = elevationDelta / horizontalDistance
速度 speed = horizontalDistance / duration
垂直速度 verticalSpeed = elevationGain / duration
阶段比例 elapsedRatio = segmentEndElapsed / totalMovingTime
```

当水平距离小于 5 米时不计算坡度，避免微小定位抖动造成极端坡度。坡度仅用于分桶和解释，不应作为精确地形测量。

### 8.4 坡度分桶

| 桶 | 坡度范围 | 标签 |
| --- | --- | --- |
| STEEP_DOWN | < -15% | 陡下坡 |
| DOWN | -15% 至 -5% | 普通下坡 |
| FLAT | -5% 至 3% | 平缓路段 |
| GENTLE_UP | 3% 至 8% | 缓上坡 |
| UP | 8% 至 15% | 普通上坡 |
| STEEP_UP | > 15% | 陡上坡 |

### 8.5 质量评估

| 质量 | 条件示例 | 是否用于画像 |
| --- | --- | --- |
| GOOD | 有时间与海拔；有效点充分；异常点比例低于 5% | 是 |
| PARTIAL | 缺少少量时间/海拔，或异常点 5%-15% | 视指标可用性决定 |
| POOR | 无时间、轨迹严重跳变、有效点过少 | 否 |

> **实现要求：** 质量评估必须返回 machine-readable 的 issueCodes，以及面向用户的中文说明。不要只返回一个 GOOD/POOR。

## 9. 用户能力画像算法

### 9.1 有效样本选择

- 默认使用最近 12 个月最多 20 条记录；配置可调整。
- 只使用 usable_for_profile=true 且 completion_status=COMPLETED 的记录计算稳定能力。
- PARTIAL/ABORTED 记录可作为风险证据，但不能提高稳定能力上限。
- 同一文件重复导入不重复计入。

### 9.2 稳定能力

MVP 不采用历史最大值，而采用加权分位数，避免一次勉强完成导致能力虚高。

```text
stableDistance = weightedPercentile(activity.distance, 0.70)
stableAscent   = weightedPercentile(activity.ascent, 0.70)
stableDuration = weightedPercentile(activity.movingTime, 0.70)

recencyWeight = 0.95 ^ monthsAgo
qualityWeight = GOOD ? 1.0 : 0.7
finalWeight   = recencyWeight * qualityWeight
```

### 9.3 个人坡度速度

- 对每个坡度桶收集非停留、非异常分段的速度。
- 每桶至少 5 个有效分段才使用个人中位速度。
- 不足 5 段时使用相邻桶插值；仍不足时使用经验等级的保守默认值。
- 保存中位数、P25、P75 和样本数，评估解释必须可引用。

### 9.4 体能衰减曲线

将每次有效活动按移动时间比例分成四个阶段，比较同坡度桶内后续阶段速度与第一阶段速度。跨活动聚合时使用中位数。

```text
Q1: 0% - 25%     baseline = 1.00
Q2: 25% - 50%    factor = median(speedQ2 / speedQ1Comparable)
Q3: 50% - 75%    factor = median(speedQ3 / speedQ1Comparable)
Q4: 75% - 100%   factor = median(speedQ4 / speedQ1Comparable)

所有 factor 限制在 [0.55, 1.10]，避免异常数据放大。
```

### 9.5 可信度

| 等级 | 规则 |
| --- | --- |
| LOW | 有效活动少于 3 条，或大多数活动缺时间/海拔。 |
| MEDIUM | 有效活动 3-7 条，且至少覆盖 FLAT 与一个上坡桶。 |
| HIGH | 有效活动不少于 8 条，至少覆盖 3 个坡度桶，GOOD 质量占比不低于 80%。 |

可信度只描述“系统对画像的把握”，不描述用户能力高低。

### 9.6 冷启动默认

| 经验等级 | 默认假设 |
| --- | --- |
| BEGINNER | 平路 3.0 km/h；上坡保守；稳定距离 8 km；稳定爬升 300 m；稳定时长 3 h。 |
| REGULAR | 平路 3.8 km/h；稳定距离 14 km；稳定爬升 700 m；稳定时长 5 h。 |
| EXPERIENCED | 平路 4.3 km/h；稳定距离 20 km；稳定爬升 1200 m；稳定时长 7 h。 |

这些默认值必须放在配置中并标记 algorithmSource=DEFAULT_BASELINE。只要用户拥有足够个人数据，就以个人画像替代。

## 10. 目标路线分析与评估

### 10.1 路线特征

- 总距离、累计爬升、累计下降、最高海拔。
- 坡度分布及各坡度桶距离占比。
- 最大连续爬升：连续 grade>3% 的分段累计爬升；允许夹杂一个不超过 100 米的平缓段。
- 最后三分之一路程的累计爬升。
- 最长连续陡上坡距离。
- 用户补充的 terrainTags：泥泞、涉水、碎石、简单攀爬、暴露、路线不清晰。

### 10.2 分段预计时间

1. 对每个路线分段确定坡度桶。
2. 从画像获取该桶个人中位速度；缺失时使用插值或保守默认。
3. 根据路线累计进度应用 Q1-Q4 体能衰减因子。
4. 根据可选背包重量和地形标签应用保守惩罚系数。
5. 累加得到基础预计时间。
6. 输出区间：min=基础时间×0.90，max=基础时间×1.20；可信度 LOW 时扩大为 0.85-1.35。

```text
segmentTime = segmentDistance / personalSpeed(gradeBucket)
              / fatigueFactor(progressQuartile)
              * packPenalty
              * terrainPenalty
```

MVP 不引入天气惩罚；接口和数据结构预留 environmentFactor，默认 1.0。

### 10.3 风险比率

```text
distanceRatio        = route.distance / profile.stableDistance
ascentRatio          = route.ascent / profile.stableAscent
durationRatio        = predictedDuration / profile.stableDuration
continuousAscentRatio= route.maxContinuousAscent / profile.p75ContinuousAscent
finalLoadRatio       = route.finalThirdAscent / profile.p75FinalThirdAscent
```

当画像缺少某项分母时，该项不直接判零，而使用经验等级默认值，并降低可信度。

### 10.4 比率转风险分

| 比率 | 风险分 |
| --- | --- |
| ≤ 0.80 | 0 |
| 0.80 - 1.00 | 20，线性插值 |
| 1.00 - 1.20 | 20 - 50，线性插值 |
| 1.20 - 1.50 | 50 - 80，线性插值 |
| > 1.50 | 80 - 100，封顶 100 |

```text
overallRisk =
    distanceRisk         * 0.25 +
    ascentRisk           * 0.25 +
    durationRisk         * 0.20 +
    continuousAscentRisk * 0.20 +
    finalLoadRisk        * 0.10 +
    terrainPenaltyPoints

matchScore = clamp(round(100 - overallRisk), 0, 100)
```

### 10.5 匹配等级

| 分数 | 等级 | 含义 |
| --- | --- | --- |
| 75-100 | RECOMMENDED | 核心负荷在近期稳定范围内，仍需正常户外准备。 |
| 50-74 | CAUTION | 存在一到两个超出稳定范围的因素，建议调整节奏或路线。 |
| 0-49 | NOT_RECOMMENDED | 多个关键指标明显超出当前画像，暂不建议作为独立挑战。 |

terrainTags 中存在“暴露、攀爬、路线不清晰”等标签时可增加 5-20 风险点，但必须在解释中明确这是用户输入的地形风险，而非 GPX 自动识别。

### 10.6 解释生成

解释由模板引擎生成，不依赖 LLM。每个风险项提供 fact、comparison、impact 和 suggestion 四个字段。

```json
{
  "code": "LATE_STAGE_ASCENT",
  "fact": "最后三分之一路程仍有 310 米爬升",
  "comparison": "高于你近期同阶段稳定值约 28%",
  "impact": "你在运动后半程的上坡速度通常下降",
  "suggestion": "保留体力，并在进入该路段前完成补给检查"
}
```

## 11. 行程计划生成

### 11.1 计划内容

- 每个路线分段的预计通过时间区间。
- REST_CHECK：建议检查是否需要短休。
- HYDRATION_CHECK：提醒检查饮水情况，不规定医疗剂量。
- ENERGY_CHECK：在较长路线或长爬升前提醒检查能量补给。
- RISK_START：进入预测风险路段前说明原因。
- TURNAROUND_CHECK：当用户设置最晚结束时间时，提供保守回撤检查点。

### 11.2 检查点规则

| 类型 | 默认规则 |
| --- | --- |
| REST_CHECK | 预计连续行走 75-90 分钟；优先移动到长爬升前或风险段前。 |
| HYDRATION_CHECK | 首次 45-60 分钟，之后每 45-60 分钟；只提示检查。 |
| ENERGY_CHECK | 预计活动超过 3 小时时启用；首次约 90 分钟，之后每 60-90 分钟；长爬升前优先。 |
| RISK_START | 每个高风险连续路段开始前，最多 3 个。 |
| TURNAROUND_CHECK | 仅用户输入 plannedEndTime 时生成；保留 20% 时间安全缓冲。 |

### 11.3 检查点去重

多个检查点落在 300 米或 10 分钟范围内时合并为一个复合检查点，消息按“风险提醒 → 休息 → 补给检查”的顺序组织。单条计划默认不超过 12 个检查点。

## 12. REST API 契约

### 12.1 通用规范

- 基础路径：/api/v1。
- JSON 字段使用 lowerCamelCase；时间使用 ISO-8601 UTC。
- 分页参数 page、size，默认 size=20，最大 100。
- 鉴权：Authorization: Bearer <token>。
- 异步导入返回 jobId；客户端轮询或使用简单状态查询。MVP 不强制 WebSocket。

### 12.2 错误格式

```json
{
  "timestamp": "2026-06-15T10:00:00Z",
  "status": 422,
  "code": "GPX_MISSING_TIME",
  "message": "该轨迹缺少时间信息，不能用于个人速度画像",
  "traceId": "...",
  "details": {"missingTimeRatio": 1.0}
}
```

### 12.3 接口清单

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | /auth/register | 注册 |
| POST | /auth/login | 登录 |
| GET | /users/me | 当前用户 |
| DELETE | /users/me | 删除账户及数据 |
| POST | /imports/gpx | multipart 导入 GPX，purpose=HISTORY或TARGET |
| GET | /imports/{jobId} | 查询解析状态与质量结果 |
| POST | /imports/{jobId}/confirm | 确认保存解析结果 |
| GET | /activities | 历史活动列表 |
| GET | /activities/{id} | 活动详情与分段指标 |
| DELETE | /activities/{id} | 删除活动并触发画像重算 |
| POST | /profiles/recalculate | 重新计算画像 |
| GET | /profiles/current | 当前画像 |
| GET | /profiles/{id} | 指定历史画像 |
| GET | /routes | 目标路线列表 |
| GET | /routes/{id} | 目标路线详情 |
| POST | /routes/{id}/assessments | 生成评估 |
| GET | /assessments/{id} | 查看评估 |
| POST | /assessments/{id}/plans | 生成计划 |
| GET | /plans/{id} | 查看计划 |
| POST | /plans/{id}/feedback | 提交完成反馈 |
| GET | /exports/me | 导出用户数据任务 |

### 12.4 GPX 导入示例

```text
POST /api/v1/imports/gpx
Content-Type: multipart/form-data

file: route.gpx
purpose: HISTORY
name: 九溪十里琅珰
completionStatus: COMPLETED
fatigueRating: 3
```

```json
{
  "jobId": "f5a2...",
  "status": "PROCESSING"
}
```

### 12.5 评估请求与响应示例

```json
POST /api/v1/routes/{routeId}/assessments
{
  "profileId": null,
  "plannedStartTime": "2026-06-20T23:00:00Z",
  "packWeightKg": 6.0,
  "isSolo": false,
  "terrainTags": ["MUDDY"]
}
```

```json
{
  "assessmentId": "9e91...",
  "matchScore": 68,
  "matchLevel": "CAUTION",
  "confidenceLevel": "MEDIUM",
  "estimatedDuration": {
    "minSeconds": 24000,
    "maxSeconds": 28200
  },
  "evidence": [
    "路线距离处于你的近期稳定范围内",
    "累计爬升比你的稳定值高约 18%"
  ],
  "risks": [
    {
      "code": "LATE_STAGE_ASCENT",
      "severity": "HIGH",
      "routeStartMeters": 11200,
      "routeEndMeters": 14600,
      "message": "最后阶段仍有连续爬升，可能放大后程速度下降"
    }
  ],
  "algorithmVersion": "assessment-v1"
}
```

## 13. Android 客户端设计

### 13.1 分层

```text
Compose Screen
   ↓ UI State / Event
ViewModel
   ↓
UseCase
   ↓
Repository Interface
   ├─ RemoteDataSource (Retrofit)
   └─ LocalDataSource  (Room)
```

### 13.2 离线缓存

- 缓存当前画像、最近 20 条活动摘要、最近 10 条路线、评估详情和计划。
- 导入文件先复制到 App 私有目录，再上传，避免临时 URI 失效。
- 网络失败时保留待上传任务；MVP 可由用户手动重试。
- 评估和计划页面优先显示本地最后成功数据，并标注更新时间。

### 13.3 UI 状态规范

每个页面 ViewModel 至少支持 Loading、Content、Empty、RecoverableError 和 FatalError。错误必须提供用户可理解的操作，例如“重新上传”或“查看质量问题”，而不是直接展示堆栈。

### 13.4 路线可视化

- MVP 必须展示简化轨迹折线和海拔剖面。
- 底图 SDK 不是验收阻塞项；先通过 Compose Canvas 绘制归一化轨迹和海拔曲线。
- 风险路段在轨迹和海拔剖面上高亮，但不得写死具体颜色语义，需兼容深色模式。
- 后续通过 RouteMapRenderer 接口接入地图厂商。

## 14. 安全、隐私与合规要求

### 14.1 数据最小化

- 只采集实现当前功能所需的账户、GPX 和反馈数据。
- MVP 不采集通讯录、相册全量权限、后台位置和精确健康数据。
- 文件选择使用系统文件选择器，不申请存储全盘权限。

### 14.2 安全要求

- 密码使用 Argon2id 或 BCrypt，禁止明文和可逆加密。
- JWT 短期访问令牌；刷新令牌如实现必须可撤销。
- 所有资源查询必须同时校验 user_id，防止水平越权。
- 上传 XML 禁止外部实体（XXE），限制深度、大小和点数。
- 日志不得记录完整 GPX、密码、Token 或精确轨迹点。
- 生产环境只允许 HTTPS；本地开发例外。

### 14.3 用户权利

- 用户可删除单条活动、目标路线和全部账户数据。
- 删除历史活动后必须重新计算画像；历史评估可标记其依据已删除，但不得静默改变。
- 提供 JSON/GPX 数据导出任务，MVP 至少支持导出账户、活动摘要、画像、评估和反馈。

### 14.4 风险声明

> **产品文案：** TrailMate 提供基于历史数据的辅助评估，不能替代专业领队、现场判断、天气预警或应急救援。户外环境变化可能导致评估失效。

## 15. 测试与验收策略

### 15.1 GPX 固定测试集

| 文件 | 场景 | 预期 |
| --- | --- | --- |
| flat_5km.gpx | 平路、完整时间海拔 | 距离误差可控，坡度主要为 FLAT。 |
| uphill_800m.gpx | 连续爬升 | 累计爬升和连续爬升正确。 |
| with_stop.gpx | 中途停留 10 分钟 | elapsed 与 movingTime 有明显差异。 |
| gps_jump.gpx | 包含异常跳点 | 跳点被标记且不显著放大距离。 |
| missing_time.gpx | 无时间 | 可作为目标路线，不可用于速度画像。 |
| noisy_elevation.gpx | 海拔抖动 | 平滑后爬升不过度累积。 |
| multi_segment.gpx | 多个 trkseg | 保留段边界且总指标正确。 |

### 15.2 核心单元测试

- Haversine 距离与边界坐标。
- 海拔中值滤波和 3 米累计阈值。
- 停留检测、异常跳点和分段。
- 加权分位数和画像可信度。
- 风险比率到分数的分段线性映射。
- 预计时间在不同坡度和衰减因子下的结果。
- 检查点合并和最多 12 个限制。
- 权限隔离：用户 A 不可读取用户 B 的资源。

### 15.3 集成测试

- 使用 Testcontainers 启动 PostgreSQL/PostGIS。
- 从上传 GPX 到活动保存、画像生成、路线评估、计划生成走完整链路。
- Flyway 从空库可一次迁移成功。
- OpenAPI 与实际响应字段一致。

### 15.4 性能目标

| 场景 | 目标 |
| --- | --- |
| 普通 API | 本地测试环境 p95 小于 800 ms（不含文件解析）。 |
| 10 万轨迹点 GPX | 解析、清洗、分段和保存不超过 15 秒；内存无明显峰值失控。 |
| 画像重算 | 20 条活动、总 50 万分段以内不超过 5 秒。 |
| 路线评估 | 2000 个路线分段以内不超过 1 秒。 |

### 15.5 Definition of Done

- 功能符合对应任务卡验收标准。
- 新增代码有必要的单元/集成测试，所有测试通过。
- 后端格式化、静态检查和构建通过；Android lint 和构建通过。
- API 或数据模型变化已更新文档和迁移脚本。
- 未提交密钥、真实用户 GPX、Token 或本地配置。
- CHANGELOG.md 和任务状态已更新。

## 16. Codex 开发里程碑与任务卡

### M0. 仓库初始化

| 任务 | 交付物 | 验收 |
| --- | --- | --- |
| M0-T1 | 创建 monorepo、Gradle、README、代码规范 | server 与 android-app 均能执行空构建。 |
| M0-T2 | docker-compose：PostgreSQL/PostGIS | 一条命令启动，健康检查通过。 |
| M0-T3 | Spring Boot 基础、Flyway、统一错误 | /actuator/health 和 OpenAPI 可访问。 |
| M0-T4 | Android Compose 壳、导航、主题 | 模拟器可启动，页面骨架可跳转。 |

### M1. 账户与 GPX 导入

| 任务 | 交付物 | 验收 |
| --- | --- | --- |
| M1-T1 | 注册、登录、JWT、当前用户 | 越权和错误密码测试通过。 |
| M1-T2 | 文件上传与安全校验 | 拒绝超限、非 GPX、XXE 文件。 |
| M1-T3 | StAX GPX 解析器 | 固定测试集解析通过。 |
| M1-T4 | 轨迹清洗、质量评估、分段 | 异常跳点、停留、海拔抖动测试通过。 |
| M1-T5 | 导入确认、活动列表与详情 API | 完整导入链路可演示。 |
| M1-T6 | Android 文件选择、上传、进度与结果页 | 真实 GPX 可从手机导入。 |

### M2. 个人能力画像

| 任务 | 交付物 | 验收 |
| --- | --- | --- |
| M2-T1 | 加权分位数、坡度速度、衰减算法 | 纯单元测试覆盖边界。 |
| M2-T2 | 画像版本化与重算 API | 删除活动后生成新版本。 |
| M2-T3 | 画像解释 DTO | 可展示样本数、可信度和证据。 |
| M2-T4 | Android 画像页面 | 展示稳定能力、速度桶和衰减曲线。 |

### M3. 目标路线评估与计划

| 任务 | 交付物 | 验收 |
| --- | --- | --- |
| M3-T1 | 目标路线导入和路线特征 | 无时间 GPX 也可完成路线分析。 |
| M3-T2 | 分段用时模拟 | 预测结果可复现且保存算法版本。 |
| M3-T3 | 风险评分与模板解释 | 所有风险分数均有解释。 |
| M3-T4 | 计划检查点生成和合并 | 规则测试通过，最多 12 个。 |
| M3-T5 | Android 评估与计划页面 | 完成导入到查看计划的主链路。 |

### M4. 反馈、离线与隐私

| 任务 | 交付物 | 验收 |
| --- | --- | --- |
| M4-T1 | 完成反馈和画像更新 | 反馈保留，旧评估不被改写。 |
| M4-T2 | Room 缓存 | 断网可查看已缓存画像、评估和计划。 |
| M4-T3 | 数据删除与导出 | 用户可删除账户；导出结构可读。 |
| M4-T4 | 隐私说明、风险声明和设置页 | 主流程可访问，文案完整。 |

### M5. 发布前质量门禁

| 任务 | 交付物 | 验收 |
| --- | --- | --- |
| M5-T1 | 完整端到端自动测试 | 上传历史→画像→评估→计划→反馈通过。 |
| M5-T2 | 性能与大文件测试 | 达到第 15.4 节目标或记录差异。 |
| M5-T3 | Demo 数据与演示脚本 | 无真实隐私数据，5 分钟可演示。 |
| M5-T4 | 安装包和部署文档 | 开发者可按 README 独立启动。 |

## 17. Codex 主提示词

将下面内容作为 Codex 在仓库根目录开始工作时的首条指令。每次只指定一个里程碑任务，不要一次要求完成整个项目。

```text
你是 TrailMate 项目的主开发者。请先完整阅读：
1. docs/product-spec.md
2. README.md
3. docs/adr/ 下已有决策

目标：按说明书实现 Android 优先的 TrailMate MVP。核心闭环是：
导入历史 GPX -> 轨迹分析 -> 个人能力画像 -> 导入目标路线 ->
路线评估 -> 行程计划 -> 完成反馈。

强制规则：
- 当前只执行我指定的一个任务卡，不提前开发后续功能。
- 先给出本任务的文件改动计划和测试计划，再开始修改代码。
- 核心算法先写失败测试，再实现。
- 不得加入社区、聊天 Agent、实时导航、付费、iOS 或自动救援。
- 不得用 LLM 生成路线分数；所有数值必须来自可测试的确定性算法。
- 任何位置数据查询都必须校验资源所属用户。
- 不提交密钥、真实轨迹和本地配置。
- 实现完成后运行相关构建、单元测试、集成测试与静态检查。
- 更新 CHANGELOG.md，并总结：修改内容、测试结果、遗留风险。
- 如需求存在歧义，选择最保守且可测试的实现，在 docs/adr/ 新增记录，
  不要擅自扩大范围。

当前任务卡：<在这里填写，例如 M1-T3：StAX GPX 解析器>
```

### 17.1 单任务输出格式

```text
开始编码前输出：
1. 对任务的理解
2. 将修改/新增的文件
3. 测试用例清单
4. 可能风险

完成编码后输出：
1. 实现摘要
2. 关键设计决定
3. 已运行命令及结果
4. 未完成项或已知限制
5. 下一任务建议（只建议，不实施）
```

### 17.2 首个任务建议

> **推荐起点：** 先执行 M0-T1 至 M0-T3，确保后端、数据库、迁移和测试框架稳定；随后优先实现 M1-T3/M1-T4 的 GPX 分析核心，再接 Android UI。

## 18. MVP 最终验收场景

1. 新用户注册登录。
2. 连续导入 3 条有效历史 GPX；系统对每条显示距离、爬升、总时长、移动时长和质量问题。
3. 系统生成 MEDIUM 或 LOW 可信度画像，并展示稳定距离、稳定爬升、稳定时长、坡度速度和衰减曲线。
4. 导入一条无时间但有坐标和海拔的目标路线 GPX。
5. 系统生成路线特征、匹配等级、可信度、预计时间区间、至少 2 条依据和至少 1 条风险说明。
6. 系统生成不超过 12 个计划检查点，可在断网后继续查看。
7. 用户提交完成反馈；系统生成新画像版本，旧评估仍引用旧画像。
8. 用户删除一条历史活动，相关轨迹点被级联删除并触发画像重算。
9. 用户 A 无法通过修改 ID 获取用户 B 的活动、路线、评估或计划。
10. 用户可以删除账户，服务端数据按策略清理。

## 19. 后续路线图（不属于 MVP）

| 阶段 | 能力 |
| --- | --- |
| V1.1 实时教练 | 前台定位、实时进度、偏航、补给事件、本地 TTS。 |
| V1.2 环境融合 | 天气、日落、温度、海拔和路线封闭信息。 |
| V1.3 设备接入 | Health Connect、手表心率和运动记录。 |
| V1.4 数据模型 | 基于真实完成记录训练预计用时和失败风险模型。 |
| V1.5 AI 解释层 | 大模型只读取结构化评估结果，负责问答与自然语言解释。 |

## 附录 A：算法版本与可复现性

- 所有 capability_profile、route_assessment 和 hike_plan 必须保存 algorithmVersion。
- 算法配置如海拔阈值、分段长度、风险权重存入版本化配置文件。
- 同一输入、同一画像版本、同一算法版本必须得到相同结果。
- 升级算法时不覆盖旧结果；创建新评估或由用户主动重算。

## 附录 B：建议配置文件

```yaml
trailmate:
  gpx:
    max-file-size-mb: 50
    max-track-points: 500000
    outlier-speed-mps: 12.0
    elevation-median-window: 5
    elevation-gain-threshold-m: 3.0
    stop-radius-m: 25.0
    stop-min-seconds: 120
    history-segment-distance-m: 200
    route-segment-distance-m: 150
  profile:
    lookback-months: 12
    max-activities: 20
    stable-percentile: 0.70
    min-segments-per-grade-bucket: 5
  assessment:
    version: assessment-v1
    weight-distance: 0.25
    weight-ascent: 0.25
    weight-duration: 0.20
    weight-continuous-ascent: 0.20
    weight-final-load: 0.10
  plan:
    rest-minutes: 80
    hydration-check-minutes: 50
    first-energy-check-minutes: 90
    max-checkpoints: 12
    merge-distance-m: 300
```

## 附录 C：提交规范

```text
feat(gpx): add streaming GPX parser
fix(profile): exclude aborted activities from stable capacity
refactor(assessment): extract risk interpolation policy
test(plan): cover nearby checkpoint merging
chore(build): lock dependency versions
```
