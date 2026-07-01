## Why

TrailMate 的后续导航算法、GPX/KML 导入、离线路线包、紧急卡片和记录复盘都需要一套稳定的 Kotlin 领域语言。现在只有 UI shell，如果继续直接写页面或算法，会很容易把 UI 文案、路线数据、隐私默认和导航状态混在一起。

本变更先建立核心路线与导航模型，让产品闭环中的「发现可信路线 → 管理可导航路线 → 执行轨迹导航 → 偏航/原路返回/紧急卡片 → 记录复盘」有可测试、可复用、可审查的基础。

## What Changes

- 新增 `core.model` Kotlin package，定义路线、路线几何、航点、风险点、撤退点、轨迹点、导航 session、导航状态、导航快照、紧急卡片、记录复盘、路况反馈和隐私设置。
- 新增可测试的领域默认值，尤其是导入路线、个人轨迹、收藏和导航 session 默认 `Private`。
- 新增导航状态枚举，覆盖 `Idle`、`Navigating`、`SuspectedOffRoute`、`ConfirmedOffRoute`、`ReturningOnTrack`、`Paused`、`Ended`。
- 新增轻量 reducer，用于验证允许的导航状态转换，不实现 GPS、几何投影或偏航算法。
- 新增 JVM unit tests 覆盖隐私默认、路线模型、导航状态转换、紧急卡片安全文案和旧范围禁止项。
- 更新文档说明模型边界和后续 PR 分工。

## Capabilities

### New Capabilities

- `core-navigation-domain`: Defines reusable Android/Kotlin domain models and navigation state semantics for routes, navigation sessions, emergency cards, records, feedback, and privacy defaults.

### Modified Capabilities

None.

## Impact

- Affected code: `app/src/main/java/com/trailmate/app/core/model/**`, `app/src/test/java/com/trailmate/app/core/model/**`, documentation, and OpenSpec artifacts.
- No new runtime permissions, network calls, map SDKs, GPS code, Room schema, backend API, or Compose navigation changes.
- No UI entry for planning, equipment, community, marketplace, or complex pre-trip checks.
- No external service dependency; all tests run as JVM unit tests.
