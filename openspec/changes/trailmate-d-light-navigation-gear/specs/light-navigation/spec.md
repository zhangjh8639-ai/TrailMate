# light-navigation Specification

## ADDED Requirements

### Requirement: Route details shall include a light navigation route tab

After target route import and assessment, the Android app shall expose a Route tab that provides route-following context without turn-by-turn navigation.

#### Scenario: User opens assessed route

- GIVEN a user has imported and assessed a target GPX route
- WHEN the user opens the Route tab
- THEN the app displays the simplified route line, elevation preview, highlighted risk segments, and next checkpoint context
- AND the app does not promise turn-by-turn navigation, rescue, or guaranteed deviation detection

### Requirement: Saved route and plan context shall be available offline

The app shall cache the assessed route summary, route visualization data, and plan checkpoints for offline viewing.

#### Scenario: User opens route with no network

- GIVEN the user has saved a route assessment and plan
- AND the device has no network connection
- WHEN the user opens the Route or Plan tab
- THEN the app displays the last cached route summary, risk highlights, and checkpoints
- AND the app shows the last updated time

### Requirement: Full navigation features shall remain out of MVP unless explicitly promoted

The MVP shall not implement turn-by-turn navigation, background GPS tracking, nationwide offline basemaps, voice guidance, or rescue dispatch.

#### Scenario: User expects navigation

- GIVEN the user is viewing the Route tab
- WHEN the app displays route-following context
- THEN the app frames it as route preview, plan context, and light route following
- AND the app avoids language that implies full navigation or emergency support

### Requirement: Location-backed hike sessions shall keep light-navigation safety boundaries

Foreground location updates may assist an active hike session by advancing checkpoints and surfacing route-check prompts, but shall not become turn-by-turn navigation or emergency support.

#### Scenario: Accurate foreground location reaches a checkpoint

- GIVEN an active hike session with planned checkpoints
- AND the foreground location fix has usable accuracy
- WHEN the fix places the user within the checkpoint radius by distance along route
- THEN the session advances to that checkpoint
- AND the app reports the update as route-following context, not turn-by-turn guidance

#### Scenario: Foreground location accuracy is low

- GIVEN an active hike session with planned checkpoints
- WHEN the foreground location fix has poor horizontal accuracy
- THEN the session does not advance checkpoints from that fix
- AND the app keeps manual checkpoint progress available

#### Scenario: Foreground location is far from the planned route

- GIVEN an active hike session with planned checkpoints
- WHEN the foreground location fix is beyond the route-check threshold from the planned route
- THEN the session does not advance checkpoints from that fix
- AND the app prompts the user to check the map and trail signs without claiming guaranteed deviation detection
