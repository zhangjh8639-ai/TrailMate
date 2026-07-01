## Why

TrailMate 的第一责任是让用户在低信号、岔路多和体力下降时知道「我在哪里、是否偏离路线、还有多远」。已有 `core.model` 只定义了路线和导航状态，还没有可测试的本地几何算法。如果后续直接把 GPS、地图 UI 和偏航判断写在页面里，会很难保证离线可用、偏航克制和进度稳定。

本变更新增纯 Kotlin `core.geo` 能力，先把路线投影、路线进度、剩余爬升和偏航证据做成 JVM 可测试的算法层。它不依赖 Android Location、MapLibre、网络或后台服务，后续真实 GPS、MapLibre 渲染、导航 UI 和轨迹记录服务都可以复用。

## What Changes

- 新增 `core.geo` Kotlin package。
- 新增 WGS84 距离、方位角、路线点投影、路线进度计算。
- 新增路线剩余距离、剩余爬升、下一个航点和最近撤退点选择。
- 新增偏航判断：GPS 精度过滤、疑似偏航、持续偏航后确认偏航。
- 新增往返线/重叠线的进度防跳变策略：可传入上一次路线进度，让投影优先选择相邻候选。
- 新增 JVM unit tests 覆盖最近线段投影、剩余距离、剩余爬升、低精度 GPS、疑似偏航、确认偏航和重叠路线进度稳定。
- 新增文档说明算法边界、参数含义和后续接入点。

## Capabilities

### New Capabilities

- `core-geo-route-matching`: Calculates nearest route point, along-route progress, remaining route metrics, next navigation anchors, and off-route evidence from local route geometry and location samples.

### Modified Capabilities

None.

## Impact

- Affected code: `app/src/main/java/com/trailmate/app/core/geo/**`, `app/src/test/java/com/trailmate/app/core/geo/**`, documentation, and OpenSpec artifacts.
- No UI changes, runtime permissions, map SDK changes, foreground service changes, GPX/KML parser changes, backend API changes, or live GPS code.
- No unsafe rerouting claim: confirmed off-route output identifies the nearest route point and evidence only; it does not generate a "safe route" or direct-return instruction.
