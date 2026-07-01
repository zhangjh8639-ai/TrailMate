# Routes Tab Import UI

This slice turns the Android `路线` tab into a focused route asset center.

## What It Shows

- Route title and asset-center subtitle.
- `导入 GPX / KML` affordance.
- Search affordance.
- Filters: `全部`, `已离线`, `已导入`, `收藏`, `最近`.
- GPX import preview derived from `RouteImportParser`.
- Route asset cards for imported, platform, and favorite routes.
- Route-only import copy:

```text
导入文件只包含路线轨迹和航点，用于轨迹导航、偏航判断和进度计算，不包含商业地图底图。
```

## Current Boundaries

This PR is UI and deterministic state only. It does not implement:

- Android file picker.
- Room or DataStore persistence.
- Backend sync.
- Route detail navigation.
- Real navigation sessions.
- GPS, tracking service, or MapLibre.

## Design Notes

- `RoutesTabState` is pure Kotlin and covered by JVM tests.
- The sample import preview parses an embedded GPX file through the production import parser, so route metrics and warnings exercise the same code path as future real imports.
- If the embedded sample cannot be parsed on a platform XML implementation, the demo state falls back to the same static preview values so the route tab does not crash. Real rejected imports still render rejected preview state in `RoutesTabSampleState.previewFromImportResult`.
- Compose code in `RoutesScreen` only renders state and contains no file-system, database, network, or GPS access.
- The screen intentionally avoids planning, equipment, community, marketplace, and complex pre-trip-check surfaces.
- The always-visible import preview is deterministic sample data for this slice. After the file picker lands, the preview should appear only after the user selects and parses a GPX/KML file.

## Verification

Recommended commands:

```powershell
openspec validate add-routes-tab-import-ui
.\gradlew.bat :app:testDebugUnitTest --console=plain
.\gradlew.bat :app:assembleDebug --console=plain
git diff --check
```
