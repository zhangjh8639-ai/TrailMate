## Why

The current Android app shell keeps the five-tab information architecture, but the `路线` tab is still a placeholder. TrailMate needs this tab to become a focused route asset center so users can see saved/offline/imported routes and understand a GPX/KML import result before starting trail navigation.

## What Changes

- Replace the `路线` placeholder with a production-shaped route asset screen in Jetpack Compose.
- Add route tab UI state models and deterministic sample state backed by the existing GPX/KML import parser.
- Show a compact import result with file name, parse status, distance, elevation gain, waypoint count, track point count, elevation availability, data quality, and route-only/offline-map copy.
- Show route asset filters and route cards for offline, imported, favorite, and recent routes.
- Keep actions scoped to UI affordances only: `导入 GPX / KML`, `开始导航`, `保存到路线`, and `查看详情` do not yet perform file picking, persistence, or live navigation.
- Add tests that guard the route tab contract and prevent legacy `规划`, `装备`, `社区`, or `商城` surfaces from returning.

## Capabilities

### New Capabilities

- `routes-tab-import-ui`: Defines the Android route tab as a route asset center with GPX/KML import result presentation and route asset cards.

### Modified Capabilities

- `android-app-shell`: The shell routes the `路线` tab to a real Compose screen instead of placeholder copy while preserving the fixed five-tab IA.

## Impact

- Affected code: `app/src/main/java/com/trailmate/app/feature/routes/**`, `app/src/main/java/com/trailmate/app/ui/TrailMateApp.kt`, route UI tests, docs, and OpenSpec artifacts.
- No new Android permissions, file picker, Room/DataStore persistence, backend API, MapLibre map rendering, GPS, tracking service, or real navigation start behavior.
- No planning tab, equipment system, community feed, marketplace, or complex pre-trip check scope.
