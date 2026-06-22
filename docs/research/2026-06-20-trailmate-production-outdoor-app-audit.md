# TrailMate Production Outdoor App Audit

Date: 2026-06-20

Scope: Android Compose prototype in `.worktrees/android-compose-prototype`, with emulator QA on `emulator-5554`.

## Benchmark Pattern

Mature outdoor apps converge on a few field-use patterns:

- AllTrails-like flow: discover or import a route, review route fit and conditions, then record/follow with GPS status visible.
- Komoot-like flow: plan first, make offline availability explicit, then enter navigation as a dedicated in-field mode.
- Gaia GPS-like flow: map, current location, offline maps, and track recording are core tools, not decorative previews.
- Strava Beacon-like safety: sharing belongs near the recording/navigation context, not buried in settings.

TrailMate should keep its unique value as personal route assessment, but the mobile flow should now be:

1. First-use setup: account/local profile, baseline capability inputs, map service consent, foreground location permission.
2. Home: today route entry, current target route summary, quick entry to assessment, route, and gear.
3. Route workspace: import or replace the target GPX, confirm route metrics.
4. Route detail: assessment first, with tabs for route cockpit, plan, and gear.
5. Route cockpit: review map/checkpoints, location/offline/recording readiness, then enter full-screen navigation.
6. Full-screen navigation: hide app chrome, show map, current checkpoint, progress, safety share, mark checkpoint, and track recording controls.
7. Data: activity review and historical GPX management, kept secondary because history/profile evidence feeds AI/assessment rather than being the main field UI.

## Current Product Health

### Strengths

- The main tab structure is recognizable and market-aligned: `首页`, `路线`, `装备`, `数据`, `我的`.
- Onboarding now gathers baseline experience before route assessment and puts map/location authorization before the user reaches route navigation.
- Home is clear in empty state: the first user action is importing a target GPX.
- Route workspace separates route import from route detail, which avoids the earlier "everything in one page" problem.
- Route detail starts on assessment, matching the D direction: evaluate first, then route/plan/gear.
- The route tab now has a separate full-screen navigation mode; bottom navigation is hidden there.
- The demo "使用示例 GPX" route entry is hidden by default and only appears when a test/demo caller explicitly opts in.
- AMap surfaces now expose loading/slow-load fallback copy instead of leaving a blank gray map without explanation.
- Full-screen navigation applies system-bar safe-area padding to the top bar and bottom dock.
- `数据` no longer presents historical/profile inputs as internal AI evidence in the first viewport.
- Track recording is real Android foreground-service work, not a mock state.
- Safety copy consistently avoids rescue-grade or turn-by-turn claims.

### UX Risks

- The gear page is useful but the add-equipment form is always inline. Production UX should use a true bottom sheet or route from a specific missing item into a focused add/edit flow.
- AMap offline base maps are still not field-proven. The app now separates local route packs from true AMap offline base-map management and can read SDK saved-region state, but production copy must not imply the target area is available offline until a region covering the active route is downloaded and verified.
- Physical-device field behavior is still the biggest product risk: emulator GPS and foreground-service proof are necessary, but not enough for outdoor release.

### Accessibility Risks

- Onboarding initial launch had a low-contrast partially hidden lower card until scrolling. Some body text and disabled action labels are close to insufficient contrast.
- Several large cards use text inside heavily rounded button-like surfaces. Most are readable, but the repeated card density may be difficult for low-vision users.
- UIAutomator returned `ERROR: null root node returned by UiTestAutomationBridge` for the Compose tree in this QA run, so semantic accessibility coverage could not be proven from this capture method.
- Full-screen navigation relies on map visuals and color-coded route/checkpoint markers; it still needs talkback labels and non-color status text verification.

## Production Map And GPS Evidence

### Verified In This Run

- `TRAILMATE_AMAP_API_KEY` is present in local properties without exposing the key.
- The debug APK installs and launches on `emulator-5554`.
- Onboarding triggers Android foreground location permission.
- AMap tiles eventually render in the route tab and full-screen navigation.
- The imported route polyline and checkpoint markers render on the AMap surface.
- Full-screen navigation hides bottom app navigation and shows field-critical controls.
- Full-screen navigation dock is covered by a focused Compose instrumentation test after safe-area padding was added.
- Tapping start eventually starts `TrackRecordingForegroundService` as an Android foreground service.
- The service notification uses channel `trailmate_track_recording`, title `TrailMate 正在记录轨迹`, and actions `暂停` / `结束`.
- The recording state persisted at least one point on emulator GPS.
- Ending recording from the UI returns to a saved review state and dumpsys no longer reports an active service record.
- AMap loading/slow-load fallback behavior is covered by unit tests.
- AMap route-map readiness no longer uses the user-facing label `生产可用` when only the online base map and local route pack are ready; it now says `在线可用` and points users back to target-region offline base-map preparation.
- AMap online map rendering is now separated from the production-readiness flag: the app can render the online AMap surface while `isProductionMapReady` remains false until offline base-map and field evidence exist.
- Departure readiness now treats the saved GPX route pack and the target-region AMap offline base map as separate checks. A route can be locally saved while departure remains pending until offline base-map regions are verified against the active route area; count-only saved regions are not enough.
- AMap offline coverage now has code-level route-region matching: the app samples the active route, reverse-geocodes it to province/city/adcode, and compares that with downloaded offline city/province metadata before allowing the offline base-map departure check.
- AMap launch diagnostics now use the same route-region coverage gate, so count-only downloaded offline regions cannot move the app into `可真机验证`.
- AMap launch diagnostics now also require separate network-disabled tile proof. A downloaded region that covers the route still stays in `待断网验证` until `断网瓦片` evidence exists.
- AMap launch diagnostics now expose offline-download network validation as a separate `下载网络` item, so a device stuck in Android `PARTIAL_CONNECTIVITY` is shown as a network blocker before the user spends time retrying offline base-map download.
- When `下载网络` is not verified, expanded route diagnostics now shows `打开网络设置` before the AMap offline manager retry, which matches the observed emulator blocker and gives real-device testers a direct repair path.
- Returning from Android network settings now refreshes TrailMate's offline-download network diagnostic, so `下载网络` and `launchNextAction` can update without requiring the user to leave and reopen the route.
- The route detail screen now also listens for foreground Android network changes and refreshes the offline-download network diagnostic while the user stays on the route.
- Expanded route diagnostics now expose a `复制诊断报告` action so real-device GPS, package/SHA1, AMap launch, and download-network blocker evidence can be shared without revealing the AMap key.
- The copied diagnostics report now carries `offlineBaseMapReason`, keeping the GPX-vs-offline-base-map rationale available for AI/debugging evidence without adding explanatory clutter to the primary route cockpit.
- Copied device diagnostics now include repair action labels such as `networkSettingsAction=打开网络设置`, so testers can share both the blocker and the next UI action when offline map download is blocked by network validation.
- Copied device diagnostics now also include `launchNextAction`, prioritizing `打开网络设置` over offline-manager retry when the download network is not validated.
- Departure readiness now uses route-assessment risk to decide whether offline base maps are a hard gate. Recommended routes show missing offline base maps as `建议下载` and can still start when route pack, GPS, and gear are ready; caution or not-recommended routes still require target-region coverage and network-disabled tile proof before `开始徒步`.
- Production release and AMap launch diagnostics still require the same network-disabled tile proof. A matched offline base-map region without airplane-mode tile evidence stays out of `可真机验证`.
- Network-disabled tile proof can now be stored locally with route-key and target-adcode scope. The app will not record the proof while network is still available, and the saved proof cannot unlock a different route or region.
- Network-disabled tile proof is now gated by current-session AMap base-map rendering, so tapping the proof button before visible AMap tiles have loaded does not create release evidence.
- Outdoor release evidence now derives airplane-mode offline tile verification from saved route-key and target-region proof instead of a loose manual flag.
- Outdoor release evidence now also derives physical-device QA, background recording, weak-signal, battery, and safety-share readiness from a structured field protocol record. Emulator evidence, short walks, missing lock/background proof, high battery drain, crashes, or recording loss cannot satisfy the release gate.
- Route cockpit primary action now follows departure readiness repair order. Before the hike starts, it points to saving the route pack, downloading offline base maps, authorizing GPS, or fixing gear before it can show `开始徒步`.
- Route cockpit now also waits for a reliable GPS fix before showing `开始徒步`; permission granted, GPS searching, or missing accuracy is not enough for a field start.
- Map readiness now uses the same reliable-fix distinction: a started location request is shown as calibration/waiting until TrailMate has a fresh fix within the field accuracy threshold, so the route page does not imply GPS is ready merely because the request flag is active.
- Route readiness now distinguishes permission from Android system location availability. If providers are disabled after permission is granted, TrailMate shows `打开系统定位`, opens system location settings, keeps departure readiness blocked, and automatically retries location calibration when the user returns with providers enabled.
- Returning from Android system location settings now preserves the full repair flow: if GPS is enabled but precise permission is still missing, TrailMate continues the precise-permission request instead of silently dropping the settings-return action.
- Outdoor location readiness now requires precise Android location permission and the Android GPS provider. Approximate-only permission and network-provider fallback no longer satisfy GPS provider selection, route readiness, or foreground track recording.
- AMap launch diagnostics now separate `精确定位`, `系统 GPS`, and `定位校准`, so a physical-device location failure can be distinguished as missing precise permission, disabled Android GPS provider, or not-yet-started calibration instead of one vague GPS state.
- TrailMate can now format a physical-device diagnostics report combining Android SDK level, manufacturer/model/device identity, app version, AMap launch items, runtime package identity, location status, prioritized repair actions, and optional opt-in offline-download QA summary/blockers while keeping the AMap API key hidden. This gives real-device testers text evidence instead of relying only on screenshots.
- Copied physical-device diagnostics now classify location repair paths with `locationRecoveryAction` and `locationRecoveryStep` output. Missing precise permission, disabled Android system GPS, slow first fix, low accuracy, unavailable tracker, and not-yet-started calibration produce different next steps without adding technical checklists to the primary route cockpit.
- Route location requests now collapse back to repair state when the tracker reports permission required, provider disabled, disabled, or unavailable. A stale `gpsEnabled` request flag can no longer keep route readiness or diagnostics looking active after location setup has failed.
- The track-recording foreground service now has its own startup and runtime guard. Direct service start/resume attempts without precise location permission, an enabled GPS provider, or a successful location-update subscription do not move an idle recording into `RECORDING`; an active recording pauses if precise permission is revoked, GPS is disabled, or subscription fails.
- Track recording start/resume now uses the same reliable-location gate. The route detail UI does not show `开始记录` while location is unauthorized, searching, missing accuracy, or worse than 50 m, and permission callbacks no longer bypass the gate.
- Active recording with weak GPS now uses conservative field-status copy: it tells users TrailMate is waiting for stable location and will only write trusted points, instead of implying every recorded moment is reliable.
- Track recording now tracks the active recording window after resume. Android last-known locations from a paused interval are ignored, so paused movement is not backfilled into the saved track.
- Finished recording review now requires at least two points and non-zero movement distance, so a one-point or stationary recording is not presented as a saved reviewable track.
- Route deviation recovery now has its own accuracy guard: if current accuracy is worse than 50 m, it asks the user to stabilize location instead of presenting a precise off-route distance.
- AMap user-location markers now distinguish precise and approximate fixes: low-accuracy or accuracy-missing locations are labeled `大致位置` with uncertainty copy and a warning-colored marker instead of presenting them as exact `当前位置`.
- GPS freshness is now part of reliability: fixes older than 60 seconds are rejected for start/recording gates, shown as stale in reliability copy, and downgraded to approximate map markers.
- Route progress now also rejects stale GPS fixes: a projected fix older than 60 seconds does not advance checkpoints, complete the route, or drive off-route guidance copy even when its accuracy and route projection look acceptable.
- Safety sharing now requires a reliable location fix. If accuracy is missing or worse than 100 m, TrailMate withholds coordinate links and asks the user to wait for a more reliable fix.
- Safety sharing no longer labels static Android share text as realtime location. During recording it now shares the current recorded position and states that it is not a live tracking link.
- Outdoor production release readiness now has a code-level gate. Emulator map/recording regression evidence alone returns `不可发布`; the gate requires release Package/SHA1, production AMap Key, target offline base-map download, active-route coverage, network-disabled tile proof, physical-device field QA, background recording, weak-signal, battery, and safety-share evidence before `生产候选`.
- AMap `OfflineMapActivity` can be started from the app context on emulator, proving the manager entry is registered and creatable.
- AMap offline base-map saved-region status can be read from the SDK on emulator, so TrailMate can distinguish `未下载` from `已下载 N 个区域`. Count-only status is diagnostic evidence, not departure-ready coverage.
- Returning from the AMap offline map manager now refreshes downloaded-region status, so a region downloaded in the manager can update route diagnostics and departure readiness without restarting TrailMate.
- AMap launch diagnostics now report the runtime installed package SHA1 when Android exposes signing data, giving physical-device testers a direct value to compare against the AMap console without exposing the API key.
- The opt-in AMap offline download QA now emits structured diagnostics for AMap key injection state, runtime package name/SHA1, catalog loading, target city resolution, downloaded-region count delta, SDK status, callback completion, system network validation, and AMap offline storage-directory writeability. Specific SDK states such as network exception, AMap service/auth exception, storage exception, and start-download failure now map to explicit blockers and next actions.
- AMap SDK initialization now sets `MapsInitializer.sdcardDir` to an app-specific `amap` directory before MapView, OfflineMapActivity, OfflineMapManager status reading, or offline download QA is created. The opt-in download QA also prefers resolved city code over city name when starting a target-city download, and reports the request method/value, storage directory, and AMap offline storage-directory writeability.
- Offline download failures now produce a recovery action and user-actionable recovery steps. Network validation, AMap Key/SHA1 binding, storage writeability, target-city mismatch, catalog loading, and target-city retry no longer collapse into one generic retry state in copied diagnostics or opt-in QA output.
- The explicit opt-in AMap offline base-map download QA test exists, but the latest `杭州市` emulator attempt did not finish: the offline catalog verified, city resolved to `code=0571` / `adcode=330100`, the SDK city state stayed at `CHECKUPDATES(6)`, completion stayed `0`, no download callback arrived, saved-region count stayed `0 -> 0`, system network validation was false, and `adb shell ping -c 1 amap.com` had 100% packet loss. The QA now fails fast on this network blocker instead of waiting for the download timeout.
- Onboarding local-only and already-granted location-permission paths are covered by focused Compose instrumentation tests.
- The `数据` tab is covered by instrumentation tests that assert internal `供 AI 评估使用。` wording is absent.

### Verified Local GPX Import

The GPX at `D:\workSpace\TrailMate\2026-05-16 0834杭州上城区.gpx` was copied to a temporary ASCII path and parsed through the app's `TargetRouteImporter.importText(...)`.

Result:

- Route name: `2026-05-16 08:34杭州上城区`
- Distance: `21.6 km`
- Ascent: `767 m`
- Points: `1858`
- Duration: `496 min`

The real GPX file was not added to test resources or committed into the worktree.

### Not Yet Proven

- Physical-device GPS stability outdoors under weak signal, screen lock, power saving, and app backgrounding.
- AMap online tile reliability with the final release SHA1/package binding.
- Target offline base-map download and offline tile availability for the actual hiking region. Active-route region matching is now implemented in code and enforced by the release gate, but current emulator evidence is still blocked at AMap offline download start/completion.
- Offline route packs and offline base maps remain separate readiness layers: a saved GPX route can support local route preview, but it does not prove base-map tiles are available without network.
- Live safety sharing link similar to Strava Beacon. Current safety share is intentionally labeled as static Android share text and not full live tracking.
- Route deviation/wrong-turn behavior in real movement. Current logic is unit-tested, but not field-tested.
- Battery consumption over multi-hour recording.

## Recommended Redesign Direction

### Offline Map Design Rationale

离线地图不应该成为 TrailMate 的通用使用门槛。用户导入 GPX、查看评估、看计划、检查装备、预览路线时，都应能继续使用在线底图或本地 GPX 折线。

它在当前设计里被放进风险分层的出发检查和生产发布门槛，是因为户外实走场景里的核心风险不是“地图好不好看”，而是手机进入弱网、无网、锁屏、低电量后，用户是否仍能看见路线周边地图上下文与位置参照。仅保存 GPX 路线包只能保证路线折线还在，不能保证山路、岔路、水系、道路、地名等底图瓦片可见。

因此产品上应分三层表达：

1. 日常准备：不强制离线底图，允许评估、计划和路线预览。
2. 实走前检查：推荐路线提示并优先引导下载目标区域离线底图；谨慎尝试或不建议路线把目标区域离线底图作为硬门槛。
3. 生产级可用声明：必须有目标区域离线底图、当前路线覆盖匹配和断网瓦片验证，才可以声称具备户外生产级地图能力。

这个设计与成熟户外应用的习惯一致：离线地图是高风险路线和出发前准备的关键安全能力，但不应该挡住用户在家里做评估和计划。

2026 年官方公开材料继续支持这个判断：AllTrails 的 Offline Areas 让用户选择一个地理区域并下载其中的路线和地图瓦片；Gaia GPS 的帮助文档把下载地图定义为离线和无蜂窝服务时使用；Komoot 的离线地图文档要求下载覆盖整个活动区域，包括起点和返程。这些产品都没有把离线地图当作普通装饰功能，而是把它放在“出发前准备/导航可靠性”的位置。

在 UI 表达上，普通路线页只呈现状态和修复动作；更完整的理由进入展开诊断和复制报告。这样用户不会被技术解释打断，但当真机定位、下载网络或离线瓦片验证失败时，报告里仍能说明为什么 GPX 路线包不能替代目标区域离线底图。

### Page Responsibilities

- `首页`: today's target route and the shortest path into route preparation. No historical evidence details.
- `路线`: target route import and route detail. Route tab should be a preparation cockpit, not a settings page.
- `全屏导航`: the only field-walking mode. Map first, bottom panel second, no standard bottom nav.
- `装备`: route-required checklist and user's gear library, with missing-item repair flows.
- `数据`: track review, completed activity history, import/manage history GPX. Keep AI evidence language out of primary copy.
- `我的`: profile, privacy, permissions, offline/map setup, data export/delete.

### Priority Fixes

1. Prove target AMap offline base-map behavior: download a city/region pack, return to TrailMate, confirm saved-region status, then disable network and verify map/fallback behavior.
2. Execute the physical-device field QA checklist for GPS, power, background service, notification controls, safety share text, and route deviation.
3. Replace the inline gear add form with a focused bottom-sheet/add-edit flow when a user repairs a missing route-required item.
4. Add TalkBack labels and non-color status verification for map markers, checkpoint state, and full-screen navigation controls.

## Evidence Files

Screenshots and dumps are saved under:

`outputs/qa/production-outdoor-readiness/`

Key files:

- `14-home-empty-route.png`: home empty route state.
- `15-route-workspace-empty.png`: route workspace before import.
- `18-route-detail-assessment.png`: assessment-first route detail.
- `19-route-detail-route-tab.png`: initial route tab map state.
- `20-route-tab-action-area.png`: AMap route tab loaded.
- `23-route-fullscreen-navigation-loaded.png`: full-screen navigation loaded.
- `26-track-recording-active.png`: active foreground recording state.
- `26-track-service-dumpsys.txt`: foreground-service proof.
- `26-notification-dumpsys.txt`: notification/channel/actions proof.
- `27-track-finished.png`: saved track review state.

## Market Sources Checked

- AllTrails Offline Areas: https://support.alltrails.com/hc/en-us/articles/37758009767444-Download-custom-areas-for-offline-use
- AllTrails offline map navigation: https://support.alltrails.com/hc/en-us/articles/37213318235028-How-to-download-maps-to-your-phone-for-offline-use
- Gaia GPS offline maps: https://help.gaiagps.com/hc/en-us/articles/360047131513-Download-Maps-for-Offline-Use
- Gaia GPS Android route/area download: https://help.gaiagps.com/hc/en-us/articles/115003524987-Download-Maps-for-a-Track-Route-or-Area-in-Android
- Komoot offline routes and maps: https://support.komoot.com/hc/en-us/articles/10356476920986-Download-routes-and-maps-for-offline-use
