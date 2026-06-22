# light-navigation Specification Delta

## ADDED Requirements

### Requirement: Route screen shall support foreground GPS positioning

The Android app SHALL request foreground location permission before using device GPS for an active route session.

#### Scenario: User grants foreground location permission

- GIVEN a user has imported a target GPX route
- AND the user opens the Route tab
- WHEN the user enables GPS positioning and grants permission
- THEN the app starts foreground location updates
- AND the app displays the latest location status and accuracy
- AND the app frames this as light route following, not full navigation

#### Scenario: User denies location permission

- GIVEN a user opens the Route tab
- WHEN the user denies location permission
- THEN the app keeps manual checkpoint progress available
- AND the app explains that GPS is unavailable without permission

#### Scenario: User grants approximate foreground location permission

- GIVEN a user opens the Route tab on an Android version that supports approximate location
- WHEN the user grants coarse foreground location but not precise foreground location
- THEN the app treats foreground location as available
- AND the app continues to display accuracy so the user can judge GPS quality

### Requirement: Accurate foreground fixes shall update light-navigation progress

The app SHALL project accurate foreground fixes onto the imported route geometry when route points are available.

#### Scenario: Accurate fix is near the planned route

- GIVEN an active hike session
- AND the imported route has geometry
- WHEN a foreground fix has usable accuracy and is near the planned route
- THEN the app updates route-relative progress and may advance checkpoints

#### Scenario: Fix is inaccurate or far from the planned route

- GIVEN an active hike session
- WHEN a fix has poor accuracy or is far from the planned route
- THEN the app does not advance checkpoint progress from that fix
- AND the app asks the user to check the map and trail signs

### Requirement: GPS light-navigation feedback shall be localized and actionable

The app SHALL present foreground GPS guidance in Chinese and SHALL explain the next safe action when location progress is not trusted.

#### Scenario: GPS fix is usable and aligned

- GIVEN an active hike session
- WHEN a usable foreground fix aligns with the planned route
- THEN the app describes the aligned checkpoint and route progress in Chinese
- AND the app surfaces a visible route-check status showing the user is on route

#### Scenario: GPS fix is low quality or off-route

- GIVEN an active hike session
- WHEN a fix is inaccurate, invalid, or far from the planned route
- THEN the app keeps checkpoint progress unchanged
- AND the app explains in Chinese whether the issue is location quality or route mismatch
- AND the app surfaces a visible route-check warning before suggesting any automatic progress
- AND the app prompts the user to rely on map, trail signs, or manual progress instead of auto-advancing

#### Scenario: User is likely off route

- GIVEN GPS has produced an accurate fix far from the planned route
- WHEN the Route tab renders the route-check warning
- THEN the app displays an off-route recovery panel
- AND the panel states that automatic checkpoint progress is stopped
- AND the panel recommends confirming direction, returning to the nearest clear path, and manually advancing only after rejoining the route
- AND the panel offers the current safety-sharing action when a shareable location is available

#### Scenario: User rejoins route after an off-route warning

- GIVEN the app has shown an off-route recovery panel
- WHEN a later accurate GPS fix returns near the planned route
- THEN the app displays a rejoined-route confirmation
- AND the app tells the user to confirm the next checkpoint before continuing
- AND the user can dismiss the confirmation and continue light navigation

#### Scenario: GPS route check has not started

- GIVEN a user opens light navigation before trusted GPS progress is available
- WHEN the GPS panel renders
- THEN the app shows that route checking is waiting for location progress
- AND the app keeps manual checkpoint controls available

#### Scenario: GPS reliability has not been established

- GIVEN a user opens light navigation before enabling GPS
- WHEN the GPS panel renders
- THEN the app explains that GPS must be enabled before positioning starts
- AND the app displays scan-friendly reliability details for location accuracy, route matching, and last update time

#### Scenario: GPS fix is available

- GIVEN GPS has produced a location fix
- WHEN the GPS panel renders
- THEN the app summarizes whether the fix is reliable, cautious, or blocked
- AND the app displays accuracy, route matching availability, and recency in Chinese
- AND low-accuracy fixes advise the user to wait in an open area before relying on route checking

### Requirement: Light navigation shall support local safety location sharing

The app SHALL let the user share their current route location through the Android system share sheet without requiring TrailMate cloud sync.

#### Scenario: Location is not available yet

- GIVEN a user opens the Route tab before GPS has produced a location fix
- WHEN the safety sharing area renders
- THEN the app explains that GPS is required before sharing
- AND the primary action enables GPS instead of producing an empty share message

#### Scenario: Current location is available

- GIVEN GPS has produced a current location fix
- WHEN the safety sharing area renders
- THEN it displays safety-plan details such as route distance, ascent, and overdue-check timing when available

#### Scenario: User shares current location

- GIVEN GPS has produced a current location fix
- WHEN the user shares their safety location
- THEN the visible safety-plan details match the generated share message
- AND the app opens the Android system share sheet with the route name, route distance, ascent, coordinates, accuracy when available, and a map link
- AND the map link declares the raw GPS coordinate system as WGS84
- AND the map link URL-encodes the marker name derived from the TrailMate route name
- AND the app does not upload that location to TrailMate by default

#### Scenario: Imported route has an estimated duration

- GIVEN the imported route has an estimated duration
- AND GPS has produced a current location fix
- WHEN the safety sharing area renders
- THEN the app displays an expected finish time and overdue-check timing

#### Scenario: User shares route with estimated duration

- GIVEN the imported route has an estimated duration
- AND GPS has produced a current location fix
- WHEN the user shares their safety location
- THEN the share message includes an expected finish time
- AND the share message explains what a safety contact should do if the user is overdue

#### Scenario: Track recording is active

- GIVEN the user is recording a local track
- AND GPS has produced a current location fix
- WHEN the user shares their safety location
- THEN the share message includes the active route name
- AND the share message includes the recorded distance so a safety contact can understand field progress

### Requirement: Light-navigation action area shall surface live next-checkpoint guidance

The app SHALL show the next checkpoint as an actionable field prompt using current hike progress, recorded distance, and route gear readiness.

#### Scenario: Active hike has a next checkpoint

- GIVEN a user has started a light-navigation hike
- AND the route has generated checkpoints
- WHEN the action area renders
- THEN it displays the next checkpoint title as a field prompt
- AND it displays a distance label derived from the current track recording when available
- AND it displays route-relevant readiness guidance such as replenishment, rest, risk, or equipment state

#### Scenario: Hike reaches the finish checkpoint

- GIVEN a user has completed all route checkpoints
- WHEN the action area renders
- THEN it explains that the route is complete
- AND it prompts the user to finish and save the local track recording for later review

### Requirement: App shall record a local foreground-service track

The app SHALL let the user start, pause, resume, and finish a local track recording during a hike.

#### Scenario: User records a hike track

- GIVEN a user has granted foreground location permission
- WHEN the user starts track recording
- THEN the app starts a location foreground service for the active route
- AND the app appends usable GPS fixes to a local track
- AND the Route tab displays recorded distance and point count
- AND the latest recording is restored after app restart

#### Scenario: Stale last-known location is ignored

- GIVEN a user starts a new local track recording
- WHEN the system provides a last-known location fix from before the recording start time
- THEN the app does not append that stale fix to the current track
- AND the recording waits for a current usable GPS fix

#### Scenario: Unreliable recording fixes are ignored

- GIVEN a local track recording already contains a current GPS point
- WHEN the system provides a later fix with a non-increasing timestamp or an implausible travel speed for a hiking track
- THEN the app does not append that fix to the current track
- AND the recorded distance remains based only on trusted points

#### Scenario: User locks the screen or switches apps while recording

- GIVEN a local track recording is active
- WHEN the user locks the screen or switches to another app
- THEN the foreground service keeps collecting usable GPS fixes
- AND the persistent notification exposes pause/resume and finish controls

#### Scenario: Foreground-service recording updates the app outside Route tab

- GIVEN a local track recording is active
- AND the user is not currently viewing the Route tab
- WHEN the foreground service publishes a newer recorded track
- THEN the app updates its local session state
- AND the Data tab can show the latest track review without waiting for app restart

#### Scenario: Notification permission is not granted on Android 13+

- GIVEN the device requires runtime notification permission
- AND the user opens the Route tab
- WHEN notification permission has not been granted
- THEN the app explains that track notifications show recording state while the screen is locked or the app is backgrounded
- AND the app offers a clear action to request notification permission before starting track recording

#### Scenario: User grants permissions after starting track recording

- GIVEN the user has imported a target GPX route
- AND track recording is not active
- WHEN the user taps start recording and a required foreground location or notification permission is missing
- THEN the app requests the missing permission
- AND if the user grants the requested permission, the app continues the original start-recording action without requiring another tap
- AND if another required recording permission is still missing, the app requests that next permission before starting

### Requirement: Track recording shall remain user-controlled and local-first

The app SHALL stop location collection when the user finishes recording and SHALL keep recorded tracks local by default.

#### Scenario: User finishes track recording

- GIVEN a local track recording is active
- WHEN the user finishes recording
- THEN the app stops foreground-service location updates
- AND the final track remains saved in the local snapshot
- AND the Light Navigation tab displays a saved-track review with distance, point count, and duration
- AND the review offers a path to the Data tab for GPX export

### Requirement: Recorded tracks shall support local review and GPX export

The app SHALL treat the latest GPS track recording as a local user asset after it contains recorded points.

#### Scenario: User reviews a recorded track from Data tab

- GIVEN the latest local track recording contains recorded GPS points
- WHEN the user opens the Data tab
- THEN the app displays a track review card with the route name, recorded distance, and point count
- AND the data export preview includes the recorded track summary
- AND the app exposes a GPX export entry for the recorded track

#### Scenario: App generates a GPX export for a recorded track

- GIVEN a recorded track has one or more recorded GPS points
- WHEN the app exports the track
- THEN the generated GPX 1.1 document contains each recorded point latitude, longitude, timestamp, and available elevation
- AND the app does not fabricate a GPX document when no recorded points exist
