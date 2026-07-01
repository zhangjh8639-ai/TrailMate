# Android Shell

TrailMate is implemented as a native Android app. The first shell owns only the fixed five-tab information architecture:

```text
发现 / 路线 / 导航 / 记录 / 我的
```

Lovable reference prototype:

```text
D:\workSpace\trailguide-pro
```

The prototype guides visual rhythm, Chinese copy tone, and interaction hierarchy. If it conflicts with `AGENTS.md`, `AGENTS.md` wins.

## Reserved Package Boundaries

Future PRs should introduce code in these boundaries only when the relevant tested behavior exists:

| Area | Future package or module |
| --- | --- |
| Discover routes | `feature.discover` |
| Route assets and import | `feature.routes`, `feature.gpximport` |
| Active track navigation | `feature.navigation` |
| Track records and feedback | `feature.records` |
| Profile, privacy, offline data | `feature.profile` |
| Emergency card and safety surfaces | `feature.safety` |
| Route and navigation models | `core.model` |
| Projection, progress, off-route, return logic | `core.geo` |
| Route package manifest and checksum | `core.offline` |
| Location providers and sampling policy | `core.location` |
| Foreground tracking service | `services.tracking` |

Do not introduce `planner`, `equipment`, `community`, or `pretrip_check` feature packages.

## Verify

```powershell
.\gradlew.bat :app:assembleDebug --console=plain
.\gradlew.bat :app:testDebugUnitTest --console=plain
adb devices
.\gradlew.bat :app:installDebug --console=plain
```

## Last Local Smoke Test

Verified on 2026-07-01 with connected device `R5CX12KKJNJ` (`SM-S9260`):

- `.\gradlew.bat :app:assembleDebug --console=plain`: passed.
- `.\gradlew.bat :app:testDebugUnitTest --console=plain`: passed.
- `.\gradlew.bat :app:installDebug --console=plain`: installed.
- `adb shell am start -n com.trailmate.app/.MainActivity`: launched.
- UI tree showed `发现`, `路线`, `导航`, `记录`, and `我的`.
