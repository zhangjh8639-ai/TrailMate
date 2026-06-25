# TrailMate AMap Offline Base Map Manager QA

Date: 2026-06-21
Scope: Verify the AMap SDK offline base-map manager separately from TrailMate's local GPX route pack.

Companion physical-device runbook: `docs/qa/trailmate-physical-device-map-gps-runbook.md`.

## Preconditions

- Physical Android device or emulator with Google/AMap networking available.
- Release-like package name and SHA1 bound in the AMap console.
- `TRAILMATE_AMAP_API_KEY` configured.
- AMap SDK linked and `OfflineMapActivity` registered.
- User has accepted map-service privacy consent in TrailMate onboarding.
- TrailMate initializes AMap with an app-specific offline storage directory under the app files directory before opening MapView, OfflineMapActivity, or OfflineMapManager.

## Steps

1. Import a GPX route and open the route cockpit.
2. Expand route diagnostics and confirm the AMap check status allows the offline base-map action.
3. Confirm diagnostics separately show `精确定位`, `系统 GPS`, `定位校准`, and `下载网络`.
4. Tap `打开高德离线底图管理`.
5. Confirm the AMap offline map manager opens instead of failing silently.
6. Download or select a small city/region package if the SDK environment supports it.
7. Return to TrailMate with the system back action.
8. Reopen the route cockpit and confirm the route state and active recording state are preserved.
9. Reopen diagnostics and confirm `离线底图` shows either `未下载` or `已下载 N 个区域` from the AMap SDK saved-region reader.
10. Confirm the downloaded city/region is the same region that covers the active GPX route, not merely any saved offline map package.
11. Disable network after the offline manager has completed its own download step.
12. Reopen the same route and verify whether AMap base-map tiles are available offline.

## Pass Criteria

- The offline manager is only reachable when Key, SDK, privacy consent, and activity registration gates are ready.
- The `Package/SHA1` diagnostic item shows the runtime installed APK SHA1 when Android exposes signing data, and the app never displays the AMap key.
- The `精确定位` and `系统 GPS` diagnostic items distinguish permission denial from disabled Android system GPS instead of collapsing both into one GPS state.
- If Android reports missing precise permission, disabled GPS provider, disabled state, or unavailable location subscription after TrailMate requests location, the route must return to a repair state instead of continuing to show GPS as active.
- Starting a location request must show calibration/waiting until a fresh fix with acceptable accuracy exists; TrailMate must not mark map readiness as field-walkable from the request flag alone.
- AMap diagnostics must keep `定位校准` as waiting or needing action while the current location is still `SEARCHING`, missing accuracy, stale, or low accuracy.
- If the first GPS fix is slow, TrailMate must keep the GPS subscription active while changing the user-facing copy to a calibration hint that asks the user to move to open sky and keep waiting.
- After TrailMate opens Android system location settings, returning to TrailMate must continue the correct repair flow: start location calibration when precise permission and GPS provider are ready, or request precise permission when it is still missing.
- If precise location was previously denied and Android no longer shows the precise-location permission dialog, TrailMate must open app-specific Android settings. Returning without precise permission must show the permission-required repair state instead of automatically launching another permission or settings screen.
- If Android cannot open the direct location settings screen, TrailMate must fall back to general Android settings; if neither settings screen opens, the route must show `定位不可用` / retry copy instead of waiting for a settings-return event.
- The action label says `离线底图`, not `路线包`.
- Required-route departure copy explains that GPX/local route packs save only the planned track, while offline base maps keep roads, place names, water features, junctions, and retreat references available in weak network conditions.
- Returning from the manager does not reset route, recording, or selected tab state.
- Returning from the manager refreshes downloaded offline-region status without restarting TrailMate or switching routes.
- Returning from the manager with no downloaded or pending offline-region status shows a route diagnostic message that no offline base-map download task was detected and asks the user to choose the target region in the AMap offline manager.
- If the AMap SDK reports unfinished offline city downloads, route diagnostics show them as download tasks that still need completion instead of collapsing them into a plain `未下载` state.
- Returning from the manager with a newly pending or downloaded offline-region status clears the no-download-detected message.
- Copied TrailMate diagnostics include `offlineBaseMapPendingRegionCount` and `offlineBaseMapPendingRegion` lines when the AMap SDK reports unfinished offline city downloads.
- TrailMate does not claim departure-ready offline base maps from downloaded-region count alone; the saved region must cover the active route area.
- A downloaded saved-region count is treated as diagnostic evidence until the target route region and network-off map behavior are verified.
- TrailMate may mark a required offline base-map departure check as covered only when the active route reverse-geocodes to the same downloaded city/province region and network-disabled tiles have been verified.
- Recommended routes may show missing offline base maps as `建议下载` without blocking departure, but this state is not production map evidence and must not satisfy AMap launch diagnostics or release readiness.
- AMap launch diagnostics must not show `可真机验证` when downloaded offline regions exist but do not match the current route.
- If `下载网络` is `未验证`, expanded route diagnostics must expose `打开网络设置` before the offline manager retry path only when the active route still lacks target-region offline base-map coverage; returning from Android network settings and foreground Android network changes must refresh the download-network diagnostic.
- When `下载网络` is `未验证` and no offline base-map region is known to cover the active route, including while downloaded-region status is still unknown or saved regions only cover another area, TrailMate must hide the offline manager download action and send the user to network settings first.
- When a downloaded offline base-map region already covers the active route and the remaining step is network-disabled tile proof, TrailMate must show `下载网络：无需下载` and must not send the user back to network settings.
- The outdoor production release gate must stay blocked until the same target region is also verified with network disabled.
- The network-disabled tile proof must be captured only after the current route session has visibly loaded AMap base-map tiles while offline.
- If offline base-map tiles are not available, TrailMate still shows the GPX route geometry and a clear fallback state.

## Evidence

- Screenshot of diagnostics before opening the manager, including the runtime `Package/SHA1` value or the manual-check fallback.
- Screenshot of diagnostics showing `精确定位` and `系统 GPS`; if location fails, capture whether the blocker is `待授权`, `待授权后检测`, or `待开启`.
- Screenshot of the route cockpit after a failed location request, showing the repair action instead of an active GPS/departure-ready state.
- Text diagnostics report copied from the expanded route diagnostics panel, including `androidSdk`, `manufacturer`, `model`, `device`, `appVersion`, package name, `Package/SHA1`, launch diagnostic items such as `下载网络`, `locationStatus`, `locationRecoveryAction`, `locationRecoveryStep`, `launchNextAction`, repair action labels such as `networkSettingsAction`, `targetOfflineBaseMapRegion`, `offlineBaseMapNextStep`, and `offlineBaseMapReason`. The report must keep `revealsApiKey=false`; full offline-download summary, blocker list, and next action come from the opt-in download QA diagnostic when that harness is run.
- If real-device location fails, keep the copied `locationRecoveryAction` and each `locationRecoveryStep`; missing precise permission, disabled system GPS, first-fix waiting, low accuracy, and unavailable location service must produce different repair paths.
- If offline download QA fails, keep the `recoveryAction` and each `recoveryStep`; network, storage, Key/SHA1, target-city, catalog, and retry failures must produce different recovery paths.
- Screenshot of AMap offline manager.
- Screenshot after returning to TrailMate.
- Screenshot of the route diagnostics return message when the user returns from AMap offline manager without starting a target-region offline base-map download.
- Screenshot of diagnostics showing `离线底图：已下载 N 个区域` or `离线底图：未下载`.
- Screenshot of diagnostics showing `下载网络：已验证` before starting offline base-map download, or `下载网络：未验证` when the device network is the blocker.
- If the opt-in offline download QA fails, keep the structured failure text showing `amapKeyConfigured`, runtime `package`, runtime `sha1`, request method/value, network validation, storage directory, and SDK status.
- If SDK status is `EXCEPTION_NETWORK_LOADING(101)`, `EXCEPTION_AMAP(102)`, `EXCEPTION_SDCARD(103)`, or `START_DOWNLOAD_FAILED(1002)`, keep that exact status in the QA notes and follow the diagnostic next action.
- Network-off screenshot showing either offline base-map availability or fallback behavior.
- Device evidence folder captured with `tools/qa/collect-trailmate-device-evidence.ps1` before and after target-region offline download plus airplane-mode tile proof.

## Automated QA Harness

Preferred physical-device evidence bundle:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'
$env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'
$env:ANDROID_SDK_ROOT=$env:ANDROID_HOME
$env:Path="$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:Path"
.\tools\qa\run-amap-offline-download-qa.ps1 -DeviceId <device-id> -CityName "杭州市" -TimeoutMs 600000
```

This wrapper saves before/after device evidence plus `amap-offline-download-gradle.txt` in one timestamped folder. A successful wrapper run still does not prove production offline readiness until TrailMate shows the target region covers the active route and the tester verifies visible base-map tiles in airplane mode.

Focused emulator smoke test:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat :android-app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.trailmate.app.AmapOfflineMapLauncherSmokeTest" --no-daemon
```

Opt-in download gate:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
.\gradlew.bat :android-app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.trailmate.app.AmapOfflineBaseMapDownloadQaTest" "-Pandroid.testInstrumentationRunnerArguments.trailmateOfflineDownloadQa=true" "-Pandroid.testInstrumentationRunnerArguments.trailmateOfflineCityName=杭州市" "-Pandroid.testInstrumentationRunnerArguments.trailmateOfflineDownloadTimeoutMs=600000" --no-daemon
```

Latest 2026-06-21 opt-in result on emulator after the app-specific storage-directory fix: failed fast with structured diagnostics after the offline catalog verified and the target city resolved as `杭州市` with `code=0571` and `adcode=330100`. The QA snapshot reported `package=com.trailmate.app`, `sha1=DF:CB:37:58:6A:FA:14:D2:97:66:D6:6E:EE:77:B0:80:C6:DF:64:88`, `request=CITY_CODE:0571`, `regions=0->0`, `downloadedStateVerified=false`, `lastStatus=NO_CALLBACK`, city state `CHECKUPDATES(6)`, completion `0`, `networkValidated=false`, and `amapStorageWritable=true` for `/storage/emulated/0/Android/data/com.trailmate.app/files/amap`. `adb shell dumpsys connectivity` showed the active default network was still `PARTIAL_CONNECTIVITY`, and `adb shell ping -c 1 amap.com` had 100% packet loss. This is an environment/network blocker, not production offline-map evidence. TrailMate still needs a successful target-region download plus airplane-mode tile verification on a validated network/device before release.

Current implementation note: TrailMate now sets `MapsInitializer.sdcardDir` to an app-specific `amap` directory before creating AMap objects, refreshes downloaded offline-region status after returning from the AMap offline manager, shows a no-download-detected return message when the SDK still reports no downloaded or pending offline regions after the user comes back, refreshes download-network diagnostics after returning from Android network settings or after foreground network callbacks, reports the runtime installed package SHA1, precise-location permission, Android system GPS provider state, and offline-download network validation in AMap diagnostics when available, and exposes a `复制诊断报告` action from expanded route diagnostics. The copied report includes `launchNextAction`, repair actions such as `networkSettingsAction` when the device network is not validated and target-route offline base-map coverage is still missing, and `offlineBaseMapReason` so field QA can distinguish a saved GPX route from network-independent base-map context. The opt-in download QA uses the resolved city code first, falling back to city name only when code is missing. The diagnostic now reports AMap key injection state, runtime package identity, download request method/value, AMap offline storage directory, AMap offline storage-directory writeability, specific SDK failure states such as network, AMap service/auth, storage, or start-download failure, plus `recoveryAction` and `recoveryStep` output so real-device failures route to network repair, storage repair, Key/SHA1 binding, target-city checks, catalog retry, or target-city retry instead of a generic failure. If system network validation is false, the opt-in download QA now fails immediately with diagnostics instead of waiting for the download timeout, expanded diagnostics keep the offline manager hidden until downloaded-region evidence proves an offline base-map region covers the active route, and network repair is suppressed when the remaining task is network-disabled tile proof.
