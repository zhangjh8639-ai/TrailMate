# TrailMate Samsung SM-S9480 Diagnostics

Date: 2026-06-22 07:58:29 +08:00
Device: Samsung SM-S9480 (`m3q`)
Package: `com.trailmate.app`
App version: `0.1.0(1)`

## Summary

The first physical-device diagnostics report showed that the real phone reached a usable GPS state:

- Precise location permission: ready.
- Android system GPS: ready.
- TrailMate location calibration: started.
- Runtime location status: `LOCATED`.
- Reported horizontal accuracy: `3 m`.

A later diagnostics report from the same device showed the location subscription waiting for the first fix:

- Precise location permission: ready.
- Android system GPS: ready.
- Runtime location status: `SEARCHING`.
- Reported horizontal accuracy: `unknown`.
- Recovery action: `WAIT_FOR_FIRST_FIX`.

This means the app can enter the correct GPS calibration flow on the device, but each departure/recording attempt still needs a fresh reliable fix before it can be used as outdoor evidence.

The remaining map blocker is offline base-map readiness:

- AMap Android Key: injected.
- Runtime Package/SHA1: reported as `DF:CB:37:58:6A:FA:14:D2:97:66:D6:6E:EE:77:B0:80:C6:DF:64:88`.
- Download network: validated.
- Target offline base-map region: `杭州市`.
- Target offline base map: not downloaded.
- Network-disabled tile proof: not available because the target offline base map is not downloaded.

This evidence means the previous "real phone cannot enable location" risk is no longer the active blocker for this device, but TrailMate must not treat `SEARCHING` as reliable GPS. The active map blocker is target-region AMap offline base-map download for `杭州市` plus airplane-mode tile verification.

## Copied Diagnostics

```text
TrailMate 真机诊断
title=高德上线检查
status=待离线底图
package=com.trailmate.app
androidSdk=36
manufacturer=samsung
model=SM-S9480
device=m3q
appVersion=0.1.0(1)
revealsApiKey=false
caption=尚未检测到已下载的高德离线底图；出发前请下载目标区域离线底图。
launchNextAction=打开高德离线底图管理
offlineMapAction=打开高德离线底图管理
offlineBaseMapReason=GPX 只保存路线折线与检查点；目标区域离线底图用于在弱网或无网时保留道路、地名、水系、岔路等地图上下文，帮助判断撤退参照。
items:
- Android Key=已注入 [READY]
- Package/SHA1=DF:CB:37:58:6A:FA:14:D2:97:66:D6:6E:EE:77:B0:80:C6:DF:64:88 [MANUAL_CHECK]
- SDK=已接入 [READY]
- 隐私授权=已同意 [READY]
- 在线底图=本地预览 [NEEDS_ACTION]
- 离线底图=未下载 [NEEDS_ACTION]
- 下载网络=已验证 [READY]
- 断网瓦片=先下载离线底图 [NEEDS_ACTION]
- 精确定位=已授权 [READY]
- 系统 GPS=已开启 [READY]
- 定位校准=已开始 [READY]
locationStatus=LOCATED
locationAccuracy=3
locationTimestamp=1782086309000
```

## Later Copied Diagnostics

```text
TrailMate 真机诊断
title=高德上线检查
status=待离线底图
package=com.trailmate.app
androidSdk=36
manufacturer=samsung
model=SM-S9480
device=m3q
appVersion=0.1.0(1)
revealsApiKey=false
caption=尚未检测到已下载的高德离线底图；出发前请下载目标区域离线底图。
launchNextAction=打开高德离线底图管理（杭州市）
offlineMapAction=打开高德离线底图管理（杭州市）
targetOfflineBaseMapRegion=杭州市
offlineBaseMapNextStep=下载目标区域离线底图后，开启飞行模式确认底图瓦片可见。
offlineBaseMapReason=GPX 只保存路线折线与检查点；目标区域离线底图用于在弱网或无网时保留道路、地名、水系、岔路等地图上下文，帮助判断撤退参照。
items:
- Android Key=已注入 [READY]
- Package/SHA1=DF:CB:37:58:6A:FA:14:D2:97:66:D6:6E:EE:77:B0:80:C6:DF:64:88 [MANUAL_CHECK]
- SDK=已接入 [READY]
- 隐私授权=已同意 [READY]
- 在线底图=本地预览 [NEEDS_ACTION]
- 离线底图=未下载 [NEEDS_ACTION]
- 下载网络=已验证 [READY]
- 断网瓦片=先下载离线底图 [NEEDS_ACTION]
- 精确定位=已授权 [READY]
- 系统 GPS=已开启 [READY]
- 定位校准=已开始 [READY]
locationStatus=SEARCHING
locationAccuracy=unknown
locationTimestamp=1782104937375
locationRecoveryAction=WAIT_FOR_FIRST_FIX
locationRecoveryStep=移动到开阔处并保持 TrailMate 在前台，等待首个 GPS fix。
locationRecoveryStep=不要用仅授权或请求中的状态作为出发、记录轨迹或安全分享证据。
```

Implementation follow-up: AMap launch diagnostics now accepts the current reliable-location-fix state, so future copies should show `定位校准=等待可靠位置 [NEEDS_ACTION]` while the phone is still `SEARCHING` instead of presenting the calibration row as ready.

## Next Evidence Needed

1. Keep the phone in open sky until TrailMate receives a fresh reliable GPS fix.
2. Download the route's target AMap offline base-map region, currently `杭州市`.
3. Return to TrailMate and confirm diagnostics change from `离线底图=未下载` to target-route coverage.
4. Disable network or enable airplane mode.
5. Reopen the same route and verify AMap base-map tiles are visibly available around the route.
6. Record the in-app `我已断网并看到底图` proof only while the phone is offline and tiles are visible.
7. Save screenshots plus the updated copied diagnostics report.
