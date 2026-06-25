# TrailMate Physical Device Map / GPS Runbook

Date: 2026-06-22
Scope: Real Android phone, AMap online and offline base maps, GPX route import, route cockpit, full-screen navigation, GPS track recording, and evidence capture.

## Product Flow To Verify

TrailMate should behave like a field-ready outdoor app, not a debugging console.

1. First-use setup collects account/profile and outdoor baseline only as AI input, then hides those details from primary route screens.
2. Home helps the user choose or import a route.
3. Route detail separates the four jobs:
   - `评估`: decide whether this route fits the user.
   - `路线`: map cockpit for current position, checkpoint, progress, and one primary action.
   - `计划`: timing, rest, supply, weather recheck, and rollback decisions.
   - `装备`: route equipment checklist and the user's own matched gear.
4. Full-screen navigation is the field mode. Bottom navigation and diagnostic panels should disappear.
5. Data/review appears after a usable recording, not from insufficient one-point or zero-distance evidence.

The route tab must not show SDK, key, SHA1, privacy, map activity registration, or raw evidence wording in its first viewport. Those belong in diagnostics or QA evidence.

## Preconditions

- One physical Android phone with USB debugging enabled.
- Installed debug or release candidate APK.
- AMap console binding matches the installed package and signing SHA1.
- Debug identity currently used during development:
  - Package name: `com.trailmate.app`
  - Debug SHA1: `DF:CB:37:58:6A:FA:14:D2:97:66:D6:6E:EE:77:B0:80:C6:DF:64:88`
- Route GPX has at least 2 km distance and 100+ points.
- Phone starts with at least 80% battery.
- Network is available for the online-map and offline-download phase.
- Tester can safely walk outdoors for at least 30 minutes.

## Evidence Folder

Run this before the field walk and again after the walk:

```powershell
$env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'
$env:Path="$env:ANDROID_HOME\platform-tools;$env:Path"
$stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
.\tools\qa\collect-trailmate-device-evidence.ps1 -PackageName com.trailmate.app -OutputDir "D:\workSpace\TrailMate\outputs\qa\physical-device-before-$stamp"
```

If multiple devices are connected:

```powershell
adb devices -l
$stamp = Get-Date -Format 'yyyyMMdd-HHmmss'
.\tools\qa\collect-trailmate-device-evidence.ps1 -DeviceId <device-id> -PackageName com.trailmate.app -OutputDir "D:\workSpace\TrailMate\outputs\qa\physical-device-before-$stamp"
```

The script is read-only. It captures device identity, package state, app ops, location provider state, connectivity, battery, foreground services, and recent logcat. It redacts AMap API keys and bare 32-character hex tokens.

For target-region offline base-map download evidence, use the wrapper below while the phone is connected:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'
$env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'
$env:ANDROID_SDK_ROOT=$env:ANDROID_HOME
$env:Path="$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:Path"
.\tools\qa\run-amap-offline-download-qa.ps1 -DeviceId <device-id> -CityName "杭州市" -TimeoutMs 600000
```

The wrapper captures before/after device evidence and the opt-in AMap offline download QA output in one folder. It does not toggle airplane mode or mark TrailMate's offline tile proof; those still require manual visual confirmation in the app.

## One-Pass Test Protocol

### 1. Route Import And Page Responsibility

1. Launch TrailMate.
2. Confirm Home asks the user to import or choose a route.
3. Import the target GPX.
4. Confirm the route detail opens with `评估 / 路线 / 计划 / 装备`.
5. Confirm `评估` shows conclusion, risk chips, and next action without exposing user profile evidence or AI prompt inputs.
6. Confirm `路线` is a cockpit: map, checkpoint, progress, GPS state, and one primary action.
7. Confirm `计划` contains timing/rest/supply decisions instead of map diagnostics.
8. Confirm `装备` contains route checklist and matched owned equipment.

Fail if route detail collapses into a single long page, or if internal evidence/profile/SDK details dominate a primary tab.

### 2. Online Map And Location Repair

1. Open `路线`.
2. Confirm AMap online tiles or clear fallback copy appears.
3. Grant Android location with precise location enabled.
4. Turn Android system location off.
5. Confirm TrailMate shows a system-location repair action and does not present `开始徒步`.
6. Turn Android system location back on and return to TrailMate.
7. Confirm TrailMate enters GPS calibration automatically.
8. Wait for a reliable fix before starting.

Fail if approximate-only permission, disabled GPS provider, searching GPS, missing accuracy, low accuracy, or stale fix unlocks `开始徒步` or `开始记录`.

### 3. Target Offline Base Map

Run the opt-in download QA on the phone before airplane-mode proof:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'
$env:ANDROID_HOME='D:\workSpace\TrailMate\.worktrees\android-compose-prototype\.android-sdk'
$env:ANDROID_SDK_ROOT=$env:ANDROID_HOME
$env:Path="$env:JAVA_HOME\bin;$env:ANDROID_HOME\platform-tools;$env:Path"
.\gradlew.bat :android-app:connectedDebugAndroidTest "-Pandroid.testInstrumentationRunnerArguments.class=com.trailmate.app.AmapOfflineBaseMapDownloadQaTest" "-Pandroid.testInstrumentationRunnerArguments.trailmateOfflineDownloadQa=true" "-Pandroid.testInstrumentationRunnerArguments.trailmateOfflineCityName=杭州市" "-Pandroid.testInstrumentationRunnerArguments.trailmateOfflineDownloadTimeoutMs=600000" --no-daemon
```

Then verify in the app:

1. Open route diagnostics only when needed.
2. Confirm downloaded AMap offline base-map region covers the active route city/province/adcode.
3. Disable Wi-Fi and cellular, or enable airplane mode.
4. Reopen the same route.
5. Confirm GPX geometry remains visible.
6. Confirm AMap base-map tiles around the active route remain visibly available offline.
7. Use `我已断网并看到底图` only while the phone is offline and visible base-map tiles are present.

Fail if any downloaded region is accepted without matching the active route, if network is still active during proof, or if GPX geometry is described as an offline base map.

### 4. Full-Screen Navigation And Recording

1. Enter full-screen navigation from the route cockpit after GPS is reliable.
2. Confirm bottom navigation and normal page chrome are hidden.
3. Start hiking and track recording.
4. Walk 10 minutes with screen on.
5. Lock the screen for 5 minutes.
6. Unlock, pause for 2 minutes, then resume.
7. Put TrailMate in background for 8 minutes, then return.
8. Intentionally move at least 50 m away from the planned route, then return.
9. Finish recording and check Data review.

Pass only if the foreground service remains active, notification controls match recording state, point count increases while moving, paused movement is not backfilled, and the saved review has enough movement evidence.

### 5. Safety Share

1. Trigger safety share while a reliable fix exists.
2. Confirm shared text includes route name, static current location, and recorded context.
3. Trigger safety share while accuracy is missing or worse than 100 m.
4. Confirm TrailMate blocks coordinate sharing and asks the user to wait.

Fail if copy implies rescue, emergency monitoring, or realtime tracking.

## Evidence To Save

- Screenshots: Home, route detail `评估`, route cockpit, full-screen navigation, offline manager, airplane-mode map with visible tiles, recording notification, pause/resume, off-route warning, Data review.
- Copied TrailMate diagnostics report, including `androidSdk`, `manufacturer`, `model`, `device`, `appVersion`, package name, SHA1, `launchNextAction`, `locationRecoveryAction`, `locationRecoveryStep`, offline recovery action/steps, `targetOfflineBaseMapRegion`, `offlineBaseMapNextStep`, and `offlineBaseMapReason`.
- Output folder from `collect-trailmate-device-evidence.ps1` before and after the walk.
- Gradle output from opt-in AMap offline download QA.
- Battery start/end percentage.
- Any crash, ANR, recording loss, or foreground-service failure notes.

## Production Pass Gate

TrailMate cannot be called outdoor production-ready until the same physical-device evidence proves:

- Runtime package and SHA1 match the AMap console binding.
- Real GPS reaches a reliable fix and keeps recording through screen lock/background.
- Target AMap offline base-map region covers the active route.
- AMap base-map tiles render around the active route while network is disabled.
- Route cockpit and full-screen navigation keep page responsibilities clean.
- Safety share uses conservative static-position copy.
- No crash, ANR, active-recording loss, or misleading location state occurs.
