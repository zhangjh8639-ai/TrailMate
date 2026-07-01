## Why

TrailMate 的「路线」Tab 需要把用户导入的 GPX/KML 文件转成可导航路线，而不是只显示文件名。已有 `core.model` 和 `core.geo` 已经能表达路线几何和导航进度，但还缺少安全、可测试的导入解析层。

本变更先实现纯 Kotlin 的导入核心：解析 GPX/KML、生成 `RouteGeometry`、计算累计距离/爬升、统计轨迹点/航点、输出质量提示，并能转成默认私密的导入路线。这样后续路线页可以直接展示紧凑解析结果并进入「开始轨迹导航 / 保存到路线 / 查看详情」动线。

## What Changes

- 新增 `core.routeimport` Kotlin package。
- 新增 GPX 解析：读取 `trkpt` 或 `rtept`，解析 `wpt` 航点、名称、海拔和累计距离。
- 新增 KML 解析：读取 `LineString coordinates`，解析 `Point` 航点、名称、海拔和累计距离。
- 新增导入结果模型，覆盖文件名、格式、解析状态、路线名、距离、累计爬升、航点数、轨迹点数、是否含海拔、数据质量提示。
- 新增质量提示：缺少海拔、轨迹点过少、点间距过大、文件格式不支持、缺少可导航轨迹。
- 新增安全 XML 解析设置，禁用外部实体和 DTD，避免导入文件触发外部资源读取。
- 新增 JVM unit tests 覆盖 GPX、KML、缺少海拔、稀疏轨迹、无轨迹文件和不支持格式。

## Capabilities

### New Capabilities

- `route-import-core`: Parses GPX/KML text into route import summaries and navigation-ready route geometry without depending on Android file pickers, Compose UI, MapLibre, database, network, or live GPS.

### Modified Capabilities

- `core-geo-route-matching`: Exposes a small public distance helper so import parsing can build route cumulative distances without duplicating geodesic math.

## Impact

- Affected code: `app/src/main/java/com/trailmate/app/core/routeimport/**`, a small public `core.geo` distance facade, corresponding JVM tests, documentation, and OpenSpec artifacts.
- No UI screens, file picker, storage, Room, DataStore, MapLibre, permissions, network calls, backend API, or live GPS code.
- No planning tab, equipment, community, marketplace, or complex pre-trip-check scope.
