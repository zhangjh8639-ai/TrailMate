# light-navigation Specification Delta

## ADDED Requirements

### Requirement: GPX-backed route preview

The light-navigation route map SHALL render the imported route geometry when `ImportedRoute.routePoints` contains at least two points.

#### Scenario: Imported route has geometry

- **GIVEN** a target route contains multiple `RoutePoint` values
- **WHEN** the user opens the light-navigation or assessment route map
- **THEN** the displayed route line is projected from those route points rather than a fixed demonstration path

#### Scenario: Imported route has no geometry

- **GIVEN** a target route has no route points
- **WHEN** the route map is displayed
- **THEN** the app may show a local fallback sketch
- **AND** the readiness state SHALL indicate missing route geometry

### Requirement: Recorded track map layer

The light-navigation map SHALL visually distinguish the planned GPX route from the user's recorded track when recording points are available.

#### Scenario: Local map preview has recorded track points

- **GIVEN** the latest track recording contains at least two usable points
- **WHEN** the local route map is displayed
- **THEN** the app draws the planned GPX route and the recorded track in the same map viewport
- **AND** the recorded track uses a visually distinct style from the planned route
- **AND** the map exposes a visible `实走轨迹` layer cue

#### Scenario: AMap provider has recorded track points

- **GIVEN** the AMap provider is production-ready
- **AND** the latest track recording contains at least two usable points
- **WHEN** the AMap map is displayed
- **THEN** the app draws the planned GPX route as one polyline
- **AND** it draws the recorded track as a second visually distinct polyline

### Requirement: Hike-plan checkpoint map layer

The light-navigation map SHALL project hike-plan checkpoints onto imported route geometry so supplement, rest, and risk guidance appears in map context.

#### Scenario: Route has geometry and plan checkpoints

- **GIVEN** a target route contains multiple `RoutePoint` values
- **AND** the hike plan contains supplement, rest, or risk checkpoints
- **WHEN** the route map is displayed
- **THEN** the app projects each checkpoint by `distanceKm` onto the route geometry
- **AND** the map shows a `路线提示点` layer with labels such as `补给`, `休息`, and `风险`
- **AND** AMap mode uses map markers for the same checkpoints

#### Scenario: User opens a checkpoint detail from the map

- **GIVEN** the route map shows a `路线提示点` layer
- **WHEN** the user selects a supplement, rest, or risk hint
- **THEN** the app shows a `提示点详情` panel
- **AND** the panel includes the checkpoint distance, expected arrival time, and suggested action
- **AND** the panel reflects current recorded progress when available
- **AND** the panel summarizes gear or water readiness for the selected checkpoint type
- **AND** the user can set that checkpoint as the current focus

#### Scenario: Route has no usable geometry

- **GIVEN** a target route has fewer than two usable route points
- **WHEN** checkpoint markers are requested
- **THEN** the app SHALL NOT fabricate marker coordinates

### Requirement: Map readiness disclosure

The light-navigation screen SHALL disclose whether the current map is a local GPX preview, field-ready light navigation, or a configured production map provider.

#### Scenario: Map readiness exposes field checks

- **GIVEN** the route map is displayed
- **WHEN** the readiness state is computed
- **THEN** it includes checks for route geometry, offline route pack, GPS, and map base layer
- **AND** the map surface highlights the next check that needs user action

#### Scenario: Map layer legend explains visible route layers

- **GIVEN** the route map is displayed
- **WHEN** the map contains a planned route, checkpoint plan, optional recorded track, optional current location, and a base map state
- **THEN** the app shows a `地图图层` legend
- **AND** the legend distinguishes `计划路线`, `路线提示点`, `实走轨迹`, `当前位置`, and `底图`
- **AND** unavailable route geometry blocks route-dependent layer status instead of implying the map is ready

#### Scenario: Location and recording summary appears after the route map

- **GIVEN** the user opens `路线辅助`
- **WHEN** the route map is displayed
- **THEN** the app shows a `定位与记录` summary directly after the map surface
- **AND** the summary combines GPS reliability, track recording point count, base map readiness, and lock-screen notification support
- **AND** the summary uses short Chinese labels such as `准备定位记录`, `正在记录实走轨迹`, `定位`, `轨迹`, `底图`, and `通知`

#### Scenario: AMap launch diagnostics are visible without secrets

- **GIVEN** the user opens `路线辅助`
- **WHEN** the route map and readiness panels are displayed
- **THEN** the app shows a `高德上线检查` summary
- **AND** the summary includes non-secret checks for `Android Key`, `Package/SHA1`, SDK linkage, privacy authorization, online base map readiness, offline base map entry readiness, and GPS
- **AND** the summary displays the package name that must be checked in the AMap console
- **AND** the summary SHALL NOT reveal the actual AMap API key value

#### Scenario: AMap offline map entry follows provider gates

- **GIVEN** the user opens `路线辅助`
- **WHEN** the AMap key, SDK linkage, privacy authorization, and official offline map activity registration gates are ready
- **THEN** the `高德上线检查` summary SHALL mark `离线底图` as `可打开`
- **AND** the app SHALL expose an action to open the AMap offline map manager
- **WHEN** any of those gates is missing
- **THEN** the `离线底图` check SHALL display the missing action such as `待配置`, `待接入`, `待授权`, or `待注册`
- **AND** the app SHALL NOT expose an offline map manager action

#### Scenario: Route geometry is missing

- **GIVEN** a target route has fewer than two usable route points
- **WHEN** the readiness state is computed
- **THEN** it prioritizes `路线几何缺失`
- **AND** it asks the user to re-import a GPX that contains track or route points
- **AND** the app remains in local preview mode without marking the map as production-ready

#### Scenario: AMap is not configured

- **GIVEN** no AMap key/provider is configured
- **WHEN** the route map is displayed
- **THEN** the readiness state shows `本地路线预览`
- **AND** it indicates `高德底图待配置`
- **AND** it exposes a map setup hint that mentions Package/SHA1 binding and `TRAILMATE_AMAP_API_KEY`
- **AND** it lists local layers such as `GPX 折线` and `检查点`

#### Scenario: AMap key exists but compliance gate is incomplete

- **GIVEN** an AMap API key exists
- **AND** the AMap SDK provider is not linked or AMap privacy consent has not been accepted
- **WHEN** the route map is displayed
- **THEN** the app remains in local GPX preview mode
- **AND** the readiness state explains whether `高德 SDK 待接入` or `高德隐私授权待确认`
- **AND** the map setup hint explains that AMap `MapView` is initialized only after consent

#### Scenario: AMap privacy consent can be accepted from route assistance

- **GIVEN** an AMap API key exists
- **AND** AMap privacy consent has not been accepted
- **WHEN** the user opens `路线辅助`
- **THEN** the app shows a `高德地图授权` action
- **WHEN** the user accepts the authorization
- **THEN** the accepted consent is persisted locally with an acceptance timestamp and policy version

#### Scenario: AMap key, SDK, and privacy consent are ready

- **GIVEN** an AMap API key exists
- **AND** the AMap SDK provider is linked
- **AND** AMap privacy consent has been accepted before SDK usage
- **WHEN** the route map is displayed
- **THEN** the readiness state may show `高德地图`
- **AND** production map readiness is true
- **AND** the map surface uses AMap `MapView`
- **AND** the imported GPX geometry is drawn as an AMap polyline
- **AND** the map setup hint marks the online base map as production-available

#### Scenario: AMap camera preserves user map browsing during same-route refreshes

- **GIVEN** the AMap map surface has fitted the imported route geometry
- **WHEN** GPS status, recorded track points, or checkpoint overlays refresh for the same imported route
- **THEN** the app keeps the current map camera instead of re-fitting route bounds
- **AND** the user can continue manually panning or zooming without the Compose update pulling the view back
- **WHEN** the user imports a route whose usable geometry endpoints differ
- **THEN** the app fits the new route bounds once so the new route is visible

#### Scenario: AMap gates are incomplete

- **GIVEN** any of the AMap key, linked SDK, or privacy consent gates are missing
- **WHEN** the route map is displayed
- **THEN** the map surface remains a local Canvas GPX preview
- **AND** the app SHALL NOT construct AMap `MapView`

#### Scenario: GPS and offline pack are ready

- **GIVEN** the user has saved the route pack
- **AND** foreground GPS is enabled
- **WHEN** the route map is displayed
- **THEN** the readiness state shows `定位与记录`
- **AND** the caption says the offline route pack and foreground GPS are active

#### Scenario: Saved route pack survives app restart

- **GIVEN** the user has saved the route pack for an imported route
- **WHEN** the app reloads the local session snapshot for the same imported route
- **THEN** the route detail screen starts with the offline route pack marked as saved
- **AND** map readiness and departure checks use the restored saved state

### Requirement: Map-first light navigation hierarchy

The light-navigation tab SHALL prioritize the map surface and map mode over secondary session-management panels.

#### Scenario: User opens route assistance

- **GIVEN** the user has opened a target route
- **WHEN** the user selects `路线辅助`
- **THEN** the visible viewport includes the map readiness title such as `本地路线预览`
- **AND** session controls such as `开始徒步` remain available without replacing the map as the first content surface

#### Scenario: Map location tool enables GPS

- **GIVEN** the route map is visible in `路线辅助`
- **WHEN** the user focuses the floating `定位` map tool
- **THEN** the tool is exposed as an actionable control rather than a decorative icon
- **WHEN** the user activates the tool
- **THEN** the app follows the same foreground GPS enablement flow used by the GPS readiness card

#### Scenario: Map tools are hidden until field behavior exists

- **GIVEN** the route map is visible in `路线辅助`
- **WHEN** heading or bearing calibration is not available
- **THEN** the app SHALL NOT expose a floating `校准方向` map tool
- **AND** every visible floating map tool SHALL either perform a real action or be absent

### Requirement: Route assistance shall surface pre-departure checks

The route-assistance tab SHALL summarize route, offline route pack, GPS, and route-critical gear readiness before the user starts a hike.

#### Scenario: Departure checks have missing items

- **GIVEN** the target route has usable geometry
- **AND** the offline route pack, GPS, or critical gear checks are not ready
- **WHEN** the user opens `路线辅助`
- **THEN** the app shows `出发检查`
- **AND** it summarizes how many readiness areas still need action
- **AND** it exposes the next recommended action such as saving the route pack, enabling GPS, or completing gear

#### Scenario: Departure checks are ready

- **GIVEN** the target route has usable geometry
- **AND** the offline route pack, GPS, and critical gear checks are ready
- **WHEN** the user opens `路线辅助`
- **THEN** the app shows `出发检查完成`
- **AND** it marks the route as ready to start

#### Scenario: Departure route geometry is missing

- **GIVEN** the target route has fewer than two usable route points
- **WHEN** departure readiness is computed
- **THEN** the app prioritizes re-importing a GPX file before starting the hike
