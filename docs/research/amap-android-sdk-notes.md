# AMap Android SDK Notes

Date: 2026-06-19

TrailMate map integration should keep AMap online tiles behind explicit readiness gates:

- AMap Android keys are bound to Android app identity. Production verification still needs a project-owned Key, package name, and signing SHA1 configured in the AMap console.
- The app injects the non-secret Android Key through `TRAILMATE_AMAP_API_KEY`, passes it to the manifest placeholder `AMAP_API_KEY`, and writes `com.amap.api.v2.apikey` in `AndroidManifest.xml`.
- AMap privacy compliance APIs must be called before constructing SDK map/location components. TrailMate currently calls `MapsInitializer.updatePrivacyShow(...)` and `MapsInitializer.updatePrivacyAgree(...)` immediately before creating `MapView`, and the UI only reaches AMap mode after local consent has been accepted.
- AMap's Android docs expose both Map SDK and Location SDK setup paths. TrailMate can keep using Android platform GPS for local-first recording while presenting AMap map readiness separately, but the UI should still show whether the current fix is accurate enough for route matching.
- AMap navigation and route-planning capabilities are useful for future online routing, but imported GPX hiking routes should not silently reroute in the first production light-navigation flow. Off-route handling should first show recovery guidance and keep manual progress under user control.
- Continuous real track recording should remain in Android foreground-service location mode with a persistent notification; this matches Android background-location expectations for a hiking recorder.
- AMap URI marker links should declare `coordinate=wgs84` when using raw Android GPS fixes. Without this, a shared safety-location marker can be interpreted in the wrong coordinate system and appear offset.
- Safety-share marker links should URL-encode the route-aware `name` parameter, include `src=TrailMate`, and set `callnative=1` so Chinese route names or `&` characters do not break the URI and mobile devices can prefer the native AMap app.

## 2026-06-19 Field UX Update

- AMap readiness is now exposed as part of a route field status summary, not only in the detailed map setup card.
- The summary should stay scan-first: `GPS`, `轨迹`, `底图`, and `通知` are the four visible field checks before the user digs into detailed GPS reliability or map setup panels.
- This keeps AMap SDK gates understandable in the real hiking flow: the user sees whether they are in local GPX preview, field-ready GPS/offline mode, or AMap-backed production map mode.
- The next SDK implementation slice should verify AMap `MyLocationStyle` blue-dot behavior, imported GPX polyline rendering, checkpoint markers, and privacy preflight on a real project key before any release claim.

## 2026-06-19 Launch Diagnostics Update

- The light-navigation screen now includes a non-secret `高德上线检查` panel for Android Key presence, Package/SHA1 console binding, SDK linkage, privacy authorization, online base map readiness, package name, and GPS.
- The panel intentionally reports `已注入` or `待配置` instead of rendering the API key value. This keeps Key ownership visible without leaking credentials in screenshots or bug reports.
- Package/SHA1 remains a manual console check even when the Key is present, because TrailMate cannot prove the AMap console binding from the device runtime.
- A project-owned Key, debug/release SHA1, `com.trailmate.app` package binding, network access, and final privacy policy copy are still required for the remaining real-device online tile QA task.

## 2026-06-19 AMap Camera UX Update

- AMap route overlays should fit route bounds only when a usable route geometry is first rendered or when imported route geometry changes.
- GPS status refreshes, checkpoint overlay refreshes, or recorded-track point updates for the same route should not call route-bounds camera updates again, because that would interrupt manual pan and zoom during light navigation.
- TrailMate now builds the AMap camera identity from route metadata plus usable geometry endpoints. This keeps same-route overlay refreshes stable while still re-fitting if a user re-imports a revised GPX with different coordinates.
- The implementation remains compatible with AMap's documented camera update model: `moveCamera` and `CameraUpdateFactory.newLatLngBounds(...)` are treated as deliberate view changes, not as work to repeat on every Compose recomposition.

Official references checked:

- AMap Android Map SDK project setup: https://lbs.amap.com/api/android-sdk/guide/create-project/android-studio-create-project
- AMap Android SDK privacy/compliance notes: https://lbs.amap.com/news/sdkhgsy
- AMap Android location SDK project setup: https://lbs.amap.com/api/android-location-sdk/guide/create-project/android-studio-create-project
- AMap Android Map SDK location display guide: https://lbs.amap.com/api/android-sdk/guide/create-map/mylocation
- AMap Android Map SDK API reference: https://a.amap.com/lbs/static/unzip/Android_Map_Doc/index.html
- AMap URI marker API: https://lbs.amap.com/api/uri-api/guide/mobile-web/point
