## ADDED Requirements

### Requirement: Production Flow Has Clear Page Responsibilities

TrailMate SHALL keep user-facing pages aligned to one primary job.

#### Scenario: User completes first-use setup

- **GIVEN** a new user opens TrailMate
- **WHEN** they complete account/profile setup and map/location preparation
- **THEN** the app lands in the main tab shell
- **AND** Home asks the user to choose or import the route they are preparing for
- **AND** internal evidence/profile details do not dominate Home or Route first viewports

#### Scenario: User imports a target GPX

- **GIVEN** no target route is active
- **WHEN** the user opens the Route tab
- **THEN** TrailMate shows route preparation and target GPX import
- **AND** after a valid GPX import it shows route distance, ascent, point count, and a single path into route detail

#### Scenario: Production route import does not expose demo data

- **GIVEN** no target route is active
- **WHEN** the user opens the Route tab in the default app flow
- **THEN** TrailMate shows the real GPX import action
- **AND** it does not show the sample GPX action unless a demo or test caller explicitly enables it

### Requirement: AMap Surface Has Loading And Fallback Feedback

TrailMate SHALL avoid unexplained blank map surfaces.

#### Scenario: Online map is still loading

- **GIVEN** AMap SDK mode is selected for a route
- **WHEN** tiles or map-loaded callback are not ready yet
- **THEN** TrailMate shows an explicit loading or slow-network state
- **AND** it keeps route actions available where safe
- **AND** it does not show a plain gray map with no explanation

#### Scenario: Online map cannot become ready

- **GIVEN** route geometry is valid
- **WHEN** AMap online surface fails or remains unavailable
- **THEN** TrailMate keeps a local GPX route preview available
- **AND** it does not claim online base maps are ready

#### Scenario: Diagnostics explain why GPX does not replace offline base maps

- **GIVEN** expanded route diagnostics are available
- **WHEN** the user copies the physical-device diagnostics report
- **THEN** the report includes an `offlineBaseMapReason` explaining that GPX preserves route geometry while target-region offline base maps preserve roads, place names, water features, junctions, and retreat context when network is weak or unavailable
- **AND** the report keeps `revealsApiKey=false`

#### Scenario: Diagnostics name online base-map verification when route map has not rendered

- **GIVEN** AMap key, SDK linkage, privacy consent, and route geometry are ready
- **AND** the active route has not rendered the AMap online base map in the current app session
- **WHEN** TrailMate presents or copies AMap launch diagnostics
- **THEN** the online base-map diagnostic item MUST say that opening the route map for verification is still required
- **AND** the copied report MUST include an `onlineBaseMapNextStep` telling the tester to open the route map and confirm the AMap base map, roads, and route are visible together
- **AND** it MUST NOT present the online base map as verified from local GPX preview alone

#### Scenario: Diagnostics name the target offline base-map region

- **GIVEN** TrailMate has reverse-geocoded the active route to a target city, province, or adcode
- **AND** target-region offline base-map evidence is still missing
- **WHEN** the user copies the physical-device diagnostics report
- **THEN** the report MUST include `targetOfflineBaseMapRegion`
- **AND** it MUST include an `offlineBaseMapNextStep` telling the tester to download the target region and verify visible base-map tiles in airplane mode
- **AND** it MUST NOT treat the target-region label alone as downloaded offline base-map evidence

#### Scenario: Diagnostics provide a fallback hint when target offline region is unknown

- **GIVEN** TrailMate has valid route geometry
- **AND** reverse geocoding cannot resolve the target city, province, or adcode
- **WHEN** TrailMate presents AMap launch diagnostics or the user copies the physical-device diagnostics report
- **THEN** the route diagnostics caption and copied report MUST include a `targetOfflineBaseMapHint` based on a representative route coordinate
- **AND** they MUST tell the tester to confirm the target city from that route point before downloading offline base maps
- **AND** it MUST NOT treat the coordinate hint as downloaded offline base-map coverage or network-disabled tile proof

#### Scenario: Map labels low-accuracy user position as approximate

- **GIVEN** AMap route surface is visible
- **AND** the current location has low accuracy, missing accuracy, or is stale for more than 60 seconds
- **WHEN** TrailMate renders the user location marker
- **THEN** it MUST label the marker as approximate location
- **AND** it MUST show accuracy uncertainty instead of presenting the marker as precise current location

### Requirement: Full-Screen Navigation Is The Field Mode

TrailMate SHALL use full-screen navigation as the dedicated in-field route mode.

#### Scenario: User enters full-screen navigation

- **GIVEN** the user has an active route
- **WHEN** they choose full-screen navigation
- **THEN** bottom navigation and normal page chrome are hidden
- **AND** map, current checkpoint, next checkpoint, progress, GPS state, recording state, safety share, mark checkpoint, and finish controls remain visible

### Requirement: Real Track Recording Uses Android Foreground Service

TrailMate SHALL use a foreground location service for route track recording.

#### Scenario: User starts track recording

- **GIVEN** foreground location permission is granted
- **AND** notification permission is granted when required by Android
- **AND** a reliable location fix is available
- **WHEN** the user starts recording from route navigation
- **THEN** TrailMate starts `TrackRecordingForegroundService`
- **AND** the service runs in the foreground with location type
- **AND** the notification exposes pause/resume or finish controls
- **AND** the UI reflects recording state and point count

#### Scenario: Track recording waits for reliable location

- **GIVEN** foreground location permission and notification permission are ready
- **AND** the current location is searching, missing accuracy, stale for more than 60 seconds, or has accuracy worse than 50 meters
- **WHEN** TrailMate presents the track recording action
- **THEN** it MUST keep the user in the location calibration action
- **AND** it MUST NOT present "start recording" or start the foreground recording service until a reliable location fix is available

#### Scenario: Approximate-only location permission is not accepted for outdoor GPS

- **GIVEN** Android grants approximate location but not precise location
- **WHEN** TrailMate evaluates route location readiness or track recording readiness
- **THEN** it MUST continue to request precise location permission
- **AND** it MUST NOT use approximate-only permission as a GPS provider for outdoor navigation or foreground track recording

#### Scenario: Network provider does not replace outdoor GPS

- **GIVEN** precise location permission is granted
- **AND** Android GPS provider is disabled
- **AND** Android network location provider is enabled
- **WHEN** TrailMate evaluates route location readiness or foreground track recording readiness
- **THEN** it MUST treat outdoor GPS as unavailable
- **AND** it MUST NOT use the network provider as proof that GPS track recording is ready

#### Scenario: Location request does not equal reliable field location

- **GIVEN** the user has started location calibration
- **AND** TrailMate has not received a fresh, accurate GPS fix
- **WHEN** TrailMate presents map or route readiness
- **THEN** it MUST show the location state as calibration or waiting
- **AND** it MUST NOT mark the route as field-walkable or show GPS/location as ready from the request flag alone
- **AND** AMap launch diagnostics MUST NOT mark the location calibration item as ready until a reliable field fix exists

#### Scenario: First GPS fix takes too long

- **GIVEN** precise location permission is granted
- **AND** Android GPS provider is enabled
- **AND** TrailMate has started outdoor location calibration
- **WHEN** no first GPS fix has arrived after the slow-first-fix threshold
- **THEN** TrailMate MUST keep the GPS subscription active
- **AND** it MUST show calibration copy that asks the user to move to open sky and keep waiting
- **AND** it MUST NOT mark route readiness, recording readiness, or map readiness as reliable field GPS evidence

#### Scenario: System location settings cannot be opened directly

- **GIVEN** precise location permission is granted
- **AND** Android GPS provider is disabled
- **WHEN** TrailMate asks Android to open system location settings
- **THEN** it MUST fall back to general Android settings if the direct location settings screen cannot be opened
- **AND** it MUST show an unavailable-location repair state if neither settings screen can be opened
- **AND** it MUST NOT wait for a settings return when no settings screen was actually opened

#### Scenario: Precise location permission was permanently denied

- **GIVEN** the user has already been asked for location permission
- **AND** Android no longer offers the precise-location permission dialog
- **WHEN** TrailMate needs precise location for route navigation or track recording
- **THEN** it MUST open Android app-specific settings instead of retrying the permission dialog
- **AND** when the user returns without granting precise location, TrailMate MUST show the permission-required repair state
- **AND** it MUST NOT loop into another automatic permission or settings launch

#### Scenario: Foreground recording service refuses unsafe start

- **GIVEN** the foreground track-recording service is started or resumed directly
- **AND** precise location permission is missing or Android location providers are disabled
- **WHEN** the service evaluates recording startup
- **THEN** it MUST NOT transition an idle recording into active recording
- **AND** it MUST publish a clear non-active recording state before stopping itself
- **AND** it MUST NOT request Android's location foreground-service type when precise location permission is missing
- **AND** an already-active recording MUST pause when precise location permission is revoked

#### Scenario: Foreground recording service only publishes active state after location subscription succeeds

- **GIVEN** precise location permission and Android GPS provider appear ready
- **WHEN** Android rejects or fails the location update subscription
- **THEN** TrailMate MUST keep an idle recording idle or pause an already-active recording
- **AND** it MUST NOT persist or broadcast `RECORDING` as the final service state
- **AND** it MUST show clear unavailable-location copy

#### Scenario: Active recording shows conservative copy when GPS weakens

- **GIVEN** track recording is active
- **AND** the current location reliability is not good
- **WHEN** TrailMate presents field status
- **THEN** it MUST explain that TrailMate is waiting for stable location
- **AND** it MUST NOT imply that every active recording moment is being saved as a reliable track point

#### Scenario: Resumed recording ignores paused-window last-known locations

- **GIVEN** track recording has been paused
- **AND** the user resumes recording
- **WHEN** Android provides a last-known location whose timestamp is before the resume time
- **THEN** TrailMate MUST NOT append that point to the recorded track
- **AND** it MUST NOT count paused-window movement into recorded distance

#### Scenario: Finished track review requires usable movement evidence

- **GIVEN** track recording has finished
- **AND** the saved recording has fewer than two points or no movement distance
- **WHEN** TrailMate presents the post-recording review entry
- **THEN** it MUST NOT show the recording as a saved reviewable track
- **AND** it MUST avoid claiming that the route performance can be reviewed from insufficient track evidence

### Requirement: Production Claim Requires Physical-Device Field QA

TrailMate SHALL NOT claim outdoor production readiness from emulator evidence alone.

#### Scenario: Release candidate is evaluated

- **GIVEN** emulator tests and screenshots pass
- **WHEN** the app is considered for real outdoor use
- **THEN** TrailMate still requires physical-device QA covering GPS accuracy, background recording, screen lock, notification controls, weak signal, offline readiness, safety share, and battery behavior

#### Scenario: Release gate receives emulator-only evidence

- **GIVEN** emulator map, navigation, and recording regressions pass
- **AND** physical-device field QA has not passed
- **WHEN** TrailMate evaluates outdoor production release readiness
- **THEN** it MUST keep the release gate blocked
- **AND** it MUST list release identity, target offline base-map verification, and physical-device field QA as required evidence before any production claim

#### Scenario: Release gate receives complete field evidence

- **GIVEN** release Package/SHA1 and production AMap Key are verified
- **AND** at least one target-region offline base-map region is downloaded
- **AND** the offline base-map region covers the active route region
- **AND** offline base-map tiles are verified with network disabled
- **AND** physical-device field QA covers background recording, weak signal, battery behavior, and safety sharing
- **AND** emulator map and recording regressions pass
- **WHEN** TrailMate evaluates outdoor production release readiness
- **THEN** it MAY mark the build as a production candidate for final release review

#### Scenario: Release evidence derives field QA from structured physical-device proof

- **GIVEN** a physical-device field QA evidence record includes device identity, route distance, route point count, duration, screen-lock recording, background recording, notification controls, weak-signal behavior, battery drain, safety-share behavior, crash state, and recording-loss state
- **WHEN** TrailMate builds outdoor production release evidence
- **THEN** physical-device field QA, background recording, weak-signal, battery, and safety-share release evidence MUST be derived from that structured record
- **AND** emulator evidence, missing records, short walks, insufficient screen-lock/background recording, high battery drain, crashes, or recording loss MUST NOT satisfy those release evidence flags

#### Scenario: Physical-device map and GPS evidence is captured as a bundle

- **GIVEN** a tester executes the physical-device map/GPS runbook on an installed Android phone
- **WHEN** the tester captures the pre-walk and post-walk evidence bundle
- **THEN** the bundle MUST include device identity, package state, app ops, Android location provider state, connectivity state, battery state, foreground-service state, and recent TrailMate-related logcat evidence
- **AND** it MUST include copied TrailMate diagnostics with package/SHA1, location recovery actions, offline-download recovery actions, and the GPX-vs-offline-base-map rationale
- **AND** it MUST NOT reveal the configured AMap API key
- **AND** the evidence bundle MUST NOT satisfy outdoor production readiness without target-route offline base-map coverage, network-disabled visible tile proof, and the structured field walk record

#### Scenario: Release gate receives count-only offline map evidence

- **GIVEN** release Package/SHA1 and production AMap Key are verified
- **AND** downloaded offline base-map regions exist
- **AND** route-region coverage or network-disabled tile proof is missing
- **WHEN** TrailMate evaluates outdoor production release readiness
- **THEN** it MUST keep the release gate blocked
- **AND** it MUST list the missing offline base-map coverage or network-disabled tile evidence

#### Scenario: Online base map is ready before field QA

- **GIVEN** AMap key, SDK linkage, and privacy consent are ready
- **AND** the local route pack has been saved
- **WHEN** TrailMate presents route map readiness
- **THEN** it may state that the online base map is available
- **AND** it MUST NOT label the route map as outdoor production ready
- **AND** its map-production readiness flag MUST remain false
- **AND** it MUST distinguish the local route pack from target-region offline base-map download and physical-device field QA

#### Scenario: High-risk departure readiness checks offline route pack and offline base map separately

- **GIVEN** the route geometry, local route pack, GPS permission, and route-critical gear are ready
- **AND** the route assessment is caution or not-recommended
- **AND** no target-region offline base map with network-disabled tile proof has been verified
- **WHEN** TrailMate presents departure readiness
- **THEN** it MUST keep departure readiness pending
- **AND** it MUST show a repair action for target-region offline base-map download
- **AND** it MUST NOT treat the saved local route pack as proof that map tiles are available offline
- **AND** it MUST NOT treat downloaded offline base-map regions as ready unless they have been matched to the active route region
- **AND** it MUST NOT treat a matched offline base-map region as departure-ready until network-disabled tile proof exists

#### Scenario: Required offline base-map repair explains the safety reason

- **GIVEN** departure readiness requires a target-region offline base map
- **AND** the local route pack has already been saved
- **WHEN** TrailMate keeps departure readiness pending for missing offline base-map evidence
- **THEN** the user-facing repair copy MUST explain that the route pack only saves the route track
- **AND** it MUST explain that offline base maps keep roads, place names, water features, junctions, and retreat references available when network is weak
- **AND** it MUST NOT expose internal evidence collection or AI prompt details in the primary route cockpit

#### Scenario: Departure readiness names the target offline base-map region

- **GIVEN** the active route has been reverse-geocoded to a target city, province, or adcode
- **AND** the route assessment requires target-region offline base-map evidence
- **AND** the target offline base map has not been downloaded
- **WHEN** TrailMate presents departure readiness or the route cockpit primary action
- **THEN** the repair action MUST name the target region, for example "下载杭州市离线底图"
- **AND** it MUST still open the offline base-map repair flow
- **AND** it MUST NOT treat the target-region label alone as downloaded offline base-map evidence

#### Scenario: Recommended-route departure treats offline base maps as guidance

- **GIVEN** the route geometry, local route pack, GPS permission, and route-critical gear are ready
- **AND** the route assessment is recommended
- **AND** no target-region offline base map with network-disabled tile proof has been verified
- **WHEN** TrailMate presents departure readiness
- **THEN** it MAY allow the user to start the hike
- **AND** it MUST still show offline base maps as recommended preparation
- **AND** it MUST NOT claim outdoor production map readiness or release readiness from this recommended-only state

#### Scenario: Departure readiness waits for network-disabled tile proof

- **GIVEN** the route geometry, local route pack, GPS permission, and route-critical gear are ready
- **AND** the route assessment is caution or not-recommended
- **AND** a downloaded offline base-map region covers the active route
- **AND** offline base-map tiles have not been verified with network disabled
- **WHEN** TrailMate presents departure readiness
- **THEN** it MUST keep departure readiness pending
- **AND** its primary repair action MUST ask the user to verify offline base maps in airplane mode

#### Scenario: Network-disabled tile proof is route and region scoped

- **GIVEN** the user verifies offline base-map tiles with network disabled
- **WHEN** TrailMate stores the verification evidence
- **THEN** it MUST bind the evidence to the active route key
- **AND** it MUST bind the evidence to the target route adcode when available
- **AND** it MUST bind the evidence to the target city or province name when adcode is unavailable
- **AND** it MUST NOT unlock a different route or target region with the saved evidence

#### Scenario: Network-disabled tile proof requires the device to be offline

- **GIVEN** a downloaded offline base-map region covers the active route
- **AND** the device still has an active network connection
- **WHEN** the user tries to record offline tile verification
- **THEN** TrailMate MUST NOT store network-disabled tile proof
- **AND** it MUST ask the user to disable network and visually confirm tiles still render

#### Scenario: Network-disabled tile proof requires visible AMap base-map tiles

- **GIVEN** a downloaded offline base-map region covers the active route
- **AND** the device network is disabled
- **AND** the AMap base map has not loaded in the current route session
- **WHEN** the user tries to record offline tile verification
- **THEN** TrailMate MUST NOT store network-disabled tile proof
- **AND** it MUST ask the user to confirm the AMap base map has loaded and is visible while offline

#### Scenario: Offline tile proof action follows the current repair step

- **GIVEN** the route diagnostics panel is shown before offline tile proof is recordable
- **WHEN** target-region detection, target-route offline base-map coverage, network-disabled state, or visible AMap base-map tiles are still missing
- **THEN** the tile-proof action MUST be disabled or non-recording
- **AND** its label MUST name the current required repair step instead of saying the user has already verified offline tiles
- **AND** TrailMate MUST NOT present "I saw offline tiles" as the primary available proof action until every proof condition is satisfied

#### Scenario: Offline base-map download QA reports environment blockers

- **GIVEN** the opt-in AMap offline base-map download QA is executed
- **WHEN** target-region download does not complete
- **THEN** the QA output MUST include catalog load state, target city resolution, download request method/value, AMap offline storage directory, downloaded-region count delta, SDK download status, callback completion, system network validation, and AMap offline storage-directory writeability
- **AND** it MUST distinguish environment blockers from completed target-region offline base-map evidence
- **AND** when system network validation is false it MUST fail with diagnostics without waiting for the download timeout

#### Scenario: Offline base-map download QA captures before and after device state

- **GIVEN** a tester runs the target-region offline base-map download QA wrapper on a connected Android phone
- **WHEN** the wrapper invokes the opt-in AMap offline download QA
- **THEN** it MUST save before-download device evidence, the Gradle/instrumentation output, after-download device evidence, and a summary file in one timestamped evidence folder
- **AND** the captured text MUST redact AMap API keys and bare 32-character hex tokens
- **AND** the wrapper MUST NOT mark outdoor production readiness without separate target-route coverage and airplane-mode visible tile proof

#### Scenario: Offline base-map download failure has actionable recovery

- **GIVEN** target-region offline base-map download QA or copied device diagnostics report a failure
- **WHEN** TrailMate formats the failure evidence
- **THEN** it MUST include a recovery action that distinguishes network repair, storage repair, AMap Key/SHA1 binding, target-city mismatch, catalog retry, and target-city retry
- **AND** it MUST include user-actionable recovery steps instead of only raw SDK status
- **AND** it MUST NOT reveal the configured AMap API key in the recovery output

#### Scenario: Offline base-map download QA distinguishes missing SDK callbacks

- **GIVEN** the AMap offline catalog loads, the target city resolves, the active network is validated, and the AMap storage directory is writable
- **AND** target-region offline base-map download still has no downloaded-state evidence
- **WHEN** the SDK returns no download callback for the target-city request
- **THEN** the QA diagnostic MUST identify the missing offline-download callback as a distinct blocker
- **AND** it MUST tell the tester to keep TrailMate in the foreground and open the AMap offline manager to confirm or retry the target-city task
- **AND** it MUST NOT collapse this state into a generic unfinished-download message

#### Scenario: AMap diagnostics expose offline-download network validation

- **GIVEN** AMap key, SDK linkage, privacy consent, route geometry, and GPS are ready
- **AND** the target offline base map has not been downloaded
- **WHEN** Android reports that the active network has not passed system validation
- **THEN** TrailMate MUST show a download-network diagnostic item as not verified
- **AND** it MUST keep the AMap diagnostics status pending
- **AND** it MUST explain that the user should repair Wi-Fi or cellular network before downloading offline base maps
- **AND** expanded route diagnostics MUST expose a network-settings repair action before asking the user to retry target-route offline base-map download
- **AND** it MUST NOT expose the offline base-map manager download action while the active network is unvalidated and no downloaded offline base-map region is known to cover the active route
- **AND** if a downloaded offline base-map region already covers the active route and the remaining work is network-disabled tile proof, TrailMate MUST NOT show network repair as the next action
- **AND** when the user returns from Android network settings, TrailMate MUST refresh the download-network diagnostic state
- **AND** while the route detail screen is visible, Android foreground network changes MUST refresh the download-network diagnostic state

#### Scenario: AMap diagnostics distinguish precise permission from system GPS provider

- **GIVEN** AMap key, SDK linkage, privacy consent, route geometry, and offline base-map evidence are ready
- **WHEN** TrailMate presents AMap launch diagnostics on a physical device
- **THEN** it MUST show precise-location permission and Android system GPS provider as separate diagnostic items
- **AND** missing precise permission MUST keep the diagnostics pending with a location-authorization repair state
- **AND** disabled Android system GPS MUST keep the diagnostics pending with a system-GPS repair state
- **AND** approximate-only permission MUST NOT be reported as ready outdoor GPS evidence
- **AND** permanently denied precise-location permission MUST route the user to app-specific Android settings

#### Scenario: AMap diagnostics avoid presenting GPS as an app toggle

- **GIVEN** precise location permission is granted
- **AND** Android system location or GPS provider is disabled
- **WHEN** TrailMate presents the high-level AMap launch diagnostic status
- **THEN** the status MUST ask the user to open system location
- **AND** it MUST NOT imply that TrailMate has an internal "enable GPS" toggle
- **AND** expanded diagnostics MAY still name the system GPS/provider item for technical verification

#### Scenario: Physical-device diagnostics can be exported without revealing secrets

- **GIVEN** TrailMate presents AMap launch diagnostics and route location state on a physical device
- **WHEN** location activation or offline base-map download fails
- **THEN** TrailMate MUST be able to format a text diagnostics report containing Android SDK level, manufacturer, model, device name, app version, package name, runtime SHA1 when available, AMap launch diagnostic items, location status, location recovery action, location recovery steps, prioritized launch next action, repair action labels, and optional offline-download QA blockers, recovery action, recovery steps, and next action when that diagnostic evidence is supplied
- **AND** the report MUST NOT reveal the configured AMap API key
- **AND** the report MUST be usable as QA evidence alongside screenshots
- **AND** expanded route diagnostics MUST expose a copy action for that report

#### Scenario: Physical-device diagnostics classify location repair paths

- **GIVEN** TrailMate formats a copied physical-device diagnostics report
- **WHEN** precise permission is missing, Android system GPS is disabled, the first GPS fix is still searching, accuracy is low, or the tracker is unavailable
- **THEN** the report MUST include a `locationRecoveryAction` that distinguishes precise-location authorization, system location settings, first-fix waiting, reliable-fix waiting, and location-service retry
- **AND** it MUST include `locationRecoveryStep` lines that a tester or user can perform on the phone
- **AND** it MUST NOT add this technical repair checklist to the primary route cockpit unless the user expands diagnostics or copies the report

#### Scenario: AMap offline storage uses an app-specific directory

- **GIVEN** TrailMate initializes AMap MapView, offline status reading, offline manager launch, or offline download QA
- **WHEN** it configures the AMap SDK
- **THEN** it MUST set the AMap offline storage directory to an app-specific `amap` directory before creating AMap objects
- **AND** it MUST NOT rely on the SDK default external storage root directory for offline map downloads

#### Scenario: AMap offline download uses resolved city code when available

- **GIVEN** the AMap offline catalog resolves a target city with both city name and city code
- **WHEN** the opt-in offline download QA starts a target-city download
- **THEN** it MUST request the download by city code
- **AND** it MAY fall back to city name only when city code is missing

#### Scenario: Route refreshes offline base-map status after returning from AMap manager

- **GIVEN** TrailMate opens the AMap offline map manager from the route cockpit or diagnostics
- **WHEN** the user returns to TrailMate after downloading or changing offline base-map regions
- **THEN** TrailMate MUST refresh downloaded offline base-map status
- **AND** departure readiness and diagnostics MUST use the refreshed downloaded-region count and target-route coverage state
- **AND** if the refreshed SDK status still contains no downloaded or pending offline base-map region, route diagnostics MUST tell the user that no offline base-map download task was detected and prompt them to choose the target region in the AMap offline manager
- **AND** if a pending or downloaded offline base-map region appears after return, TrailMate MUST clear the no-download-detected return message

#### Scenario: Release evidence derives offline tile proof from saved route-region evidence

- **GIVEN** network-disabled tile proof exists for the active route key and target route region
- **WHEN** TrailMate builds outdoor production release evidence
- **THEN** offline base-map airplane-mode evidence MUST be true
- **AND** proof from a different route or target region MUST NOT satisfy the release gate

#### Scenario: AMap diagnostics require offline base map coverage for device QA

- **GIVEN** AMap key, SDK linkage, privacy consent, route geometry, and GPS are ready
- **AND** downloaded offline base-map regions exist
- **AND** the downloaded regions have not been matched to the active route region
- **WHEN** TrailMate presents AMap launch diagnostics
- **THEN** it MUST keep the diagnostics status pending
- **AND** it MUST NOT show the build as ready for physical-device QA
- **AND** it MUST explain that the downloaded offline base-map regions still need to cover the current route

#### Scenario: AMap diagnostics distinguish pending offline map downloads

- **GIVEN** AMap key, SDK linkage, privacy consent, route geometry, and GPS are ready
- **AND** the AMap SDK reports one or more offline base-map city downloads that are waiting, loading, paused, checking updates, or otherwise unfinished
- **WHEN** TrailMate presents AMap launch diagnostics or the user copies the physical-device diagnostics report
- **THEN** it MUST NOT collapse the state to a plain "not downloaded" message
- **AND** it MUST show that offline base-map download tasks exist but are not complete
- **AND** the copied report MUST include the pending offline base-map download count and region labels when available
- **AND** it MUST keep device QA pending until the target region is downloaded, matched to the active route, and verified with network disabled

#### Scenario: AMap diagnostics report runtime package identity

- **GIVEN** AMap launch diagnostics are shown on an installed Android build
- **WHEN** TrailMate can read the runtime package signing certificate
- **THEN** it MUST show the installed package SHA1 in the Package/SHA1 diagnostic item
- **AND** it MUST NOT reveal the configured AMap API key
- **AND** it MAY fall back to a manual console-check label when runtime signing data is unavailable

#### Scenario: AMap diagnostics require network-disabled tile proof for device QA

- **GIVEN** AMap key, SDK linkage, privacy consent, route geometry, GPS, and target-route offline base-map coverage are ready
- **AND** offline base-map tiles have not been verified with network disabled
- **WHEN** TrailMate presents AMap launch diagnostics
- **THEN** it MUST keep the diagnostics status pending
- **AND** it MUST NOT show the build as ready for physical-device QA
- **AND** it MUST show a separate network-disabled tile verification item

#### Scenario: Route cockpit repairs departure gaps before starting the hike

- **GIVEN** the user is in the route cockpit before starting a hike
- **AND** departure readiness is missing route pack, required target offline base map, GPS permission, or critical gear
- **WHEN** TrailMate presents the cockpit primary action
- **THEN** the primary action MUST point to the first departure repair action
- **AND** it MUST NOT present "start hike" until departure readiness is complete

#### Scenario: Route cockpit location repair does not only enter navigation

- **GIVEN** the user is in the route cockpit
- **AND** the primary action is location authorization, system location settings, or waiting for a reliable GPS fix
- **WHEN** the user taps the primary action
- **THEN** TrailMate MUST execute the location repair or calibration action from the cockpit
- **AND** it MUST NOT replace the repair action with a fullscreen-navigation transition

#### Scenario: Route cockpit waits for reliable GPS before starting the hike

- **GIVEN** route pack, required target offline base map evidence when the assessment calls for it, GPS permission, and critical gear are ready
- **AND** the current location is still searching, missing accuracy, stale for more than 60 seconds, or has low accuracy
- **WHEN** TrailMate presents the cockpit primary action before the hike starts
- **THEN** it MUST keep the user in the location calibration action
- **AND** it MUST NOT present "start hike" until a reliable location fix is available

#### Scenario: System location service is disabled after permission is granted

- **GIVEN** foreground location permission has been granted
- **AND** Android system location providers are disabled
- **WHEN** TrailMate presents route readiness or route cockpit primary actions
- **THEN** it MUST show an action to open system location settings
- **AND** it MUST NOT show the route as departure-ready or present "start hike"

#### Scenario: TrailMate retries location after returning from system settings

- **GIVEN** TrailMate opened Android system location settings for a disabled provider
- **WHEN** the user returns to TrailMate and a location provider is now enabled
- **THEN** TrailMate MUST automatically enter location calibration
- **AND** it MUST NOT require a second tap before starting location updates
- **AND** if precise location permission is still missing after returning, TrailMate MUST continue the permission request flow instead of dropping the settings-return action

#### Scenario: Route location request stops after tracker setup failure

- **GIVEN** the route page has requested outdoor location updates
- **WHEN** the Android location tracker reports missing precise permission, disabled GPS provider, disabled state, or unavailable location subscription
- **THEN** TrailMate MUST stop treating the route location request as active
- **AND** it MUST keep the failure snapshot visible for the user-facing repair action
- **AND** it MUST NOT mark route readiness, AMap diagnostics, or departure readiness as GPS-ready from the stale request state

#### Scenario: Location reliability treats stale fixes as calibration

- **GIVEN** TrailMate has a located fix with acceptable accuracy
- **AND** the fix has not updated for more than 60 seconds
- **WHEN** TrailMate presents location reliability
- **THEN** it MUST show stale-location calibration copy
- **AND** it MUST NOT present the fix as reliable navigation evidence

#### Scenario: Location reliability presents a repair action

- **GIVEN** TrailMate location reliability is searching, low accuracy, stale, missing accuracy, provider-disabled, permission-required, or unavailable
- **WHEN** TrailMate presents the location reliability panel
- **THEN** it MUST show an action for the next user repair, such as authorizing location, opening system location, continuing calibration, or retrying location
- **AND** it MUST NOT leave the user with a passive status-only panel before departure

#### Scenario: Route progress rejects stale location fixes

- **GIVEN** TrailMate has an active hike session
- **AND** the projected location fix is on the planned route with acceptable accuracy
- **AND** the fix has not updated for more than 60 seconds
- **WHEN** TrailMate applies location guidance to the route session
- **THEN** it MUST keep the reached checkpoint unchanged
- **AND** it MUST show calibration copy instead of route-progress or off-route evidence

#### Scenario: Route deviation recovery waits for reliable accuracy

- **GIVEN** TrailMate receives a route-check state
- **AND** the current location accuracy is worse than 50 meters
- **WHEN** TrailMate presents deviation recovery
- **THEN** it MUST ask the user to stabilize location first
- **AND** it MUST NOT present a precise off-route distance or recovery instruction as reliable evidence

#### Scenario: Safety share waits for reliable location accuracy

- **GIVEN** the app has latitude and longitude
- **AND** location accuracy is missing or worse than 100 meters
- **WHEN** TrailMate presents safety sharing
- **THEN** it MUST NOT generate a share link with coordinates
- **AND** it MUST ask the user to wait for a more reliable location fix

#### Scenario: Safety share does not imply live tracking without a live service

- **GIVEN** track recording is active
- **AND** TrailMate only generates local Android share text
- **WHEN** TrailMate presents safety sharing
- **THEN** it MUST label the action as sharing the current recorded position
- **AND** the shared text MUST state that the position is a static point-in-time location, not a live tracking link
- **AND** it MUST NOT call the feature realtime tracking, rescue, or emergency monitoring
