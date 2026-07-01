# TrailMate

TrailMate is a native Android hiking track-navigation app. The current product direction is focused on credible route discovery, route asset management, offline-ready track navigation, recording, safety, privacy, and structured trail-condition feedback.

## Android Shell

This repository starts with a Kotlin + Jetpack Compose + Material 3 app shell.

Primary tabs:

```text
发现 / 路线 / 导航 / 记录 / 我的
```

The shell intentionally does not include a standalone `规划` tab, equipment system, community feed, marketplace, or complex pre-trip checklist.

## Verify

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'
$env:ANDROID_HOME='D:\software\Android\Sdk'
$env:ANDROID_SDK_ROOT=$env:ANDROID_HOME
$env:Path="$env:JAVA_HOME\bin;D:\software\gradle-9.4.1\bin;$env:ANDROID_HOME\platform-tools;$env:ANDROID_HOME\cmdline-tools\latest\bin;$env:ANDROID_HOME\emulator;$env:Path"

.\gradlew.bat :app:assembleDebug --console=plain
.\gradlew.bat :app:testDebugUnitTest --console=plain
```
