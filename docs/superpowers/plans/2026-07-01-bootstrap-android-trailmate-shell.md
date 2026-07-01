# Bootstrap Android TrailMate Shell Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the first production-grade Android native foundation for TrailMate with a runnable Compose shell and fixed five-tab information architecture.

**Architecture:** The first PR creates only the buildable Android shell and durable package boundaries. It uses the Lovable prototype as design reference, while `AGENTS.md` and OpenSpec define the implementation contract. GPS, GPX parsing, MapLibre, Foreground Service tracking, and route algorithms are deferred to later PRs.

**Tech Stack:** Kotlin, Gradle Kotlin DSL, Android Gradle Plugin, Jetpack Compose, Material 3, JUnit, Android SDK 36, JDK 17.

---

## File Structure

- Create: `settings.gradle.kts` - Gradle project/module registration.
- Create: `build.gradle.kts` - root plugin declarations.
- Create: `gradle/libs.versions.toml` - dependency versions.
- Create: `gradle.properties` - Android/Kotlin build flags.
- Create: `app/build.gradle.kts` - Android app module config.
- Create: `app/src/main/AndroidManifest.xml` - app manifest.
- Create: `app/src/main/res/values/styles.xml` - launcher activity theme bridge.
- Create: `app/src/main/java/com/trailmate/app/MainActivity.kt` - Compose activity entry.
- Create: `app/src/main/java/com/trailmate/app/ui/TrailMateApp.kt` - five-tab app shell.
- Create: `app/src/main/java/com/trailmate/app/ui/TrailMateTabs.kt` - tab definitions.
- Create: `app/src/main/java/com/trailmate/app/ui/theme/Color.kt` - TrailMate colors.
- Create: `app/src/main/java/com/trailmate/app/ui/theme/Theme.kt` - Material 3 theme.
- Create: `app/src/test/java/com/trailmate/app/ui/TrailMateTabsTest.kt` - JVM tab IA test.
- Create: `docs/android-shell.md` - shell verification and Lovable reference notes.
- Modify: `local.properties` - local SDK path only, ignored by git.

## Task 1: Confirm Worktree And Toolchain

**Files:**
- Verify only: `D:\workSpace\TrailMate\.worktrees\android-environment-bootstrap`

- [ ] **Step 1: Confirm isolated worktree**

Run:

```powershell
git -C D:\workSpace\TrailMate\.worktrees\android-environment-bootstrap rev-parse --git-dir
git -C D:\workSpace\TrailMate\.worktrees\android-environment-bootstrap rev-parse --git-common-dir
git -C D:\workSpace\TrailMate\.worktrees\android-environment-bootstrap branch --show-current
```

Expected: branch is `codex/android-environment-bootstrap` or a renamed implementation branch, and git dir differs from common dir.

- [ ] **Step 2: Verify Android toolchain**

Run:

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'
$env:ANDROID_HOME='D:\software\Android\Sdk'
$env:ANDROID_SDK_ROOT=$env:ANDROID_HOME
$env:Path="$env:JAVA_HOME\bin;D:\software\gradle-9.4.1\bin;$env:ANDROID_HOME\platform-tools;$env:ANDROID_HOME\cmdline-tools\latest\bin;$env:ANDROID_HOME\emulator;$env:Path"
java -version
gradle -v
adb devices
```

Expected: Java 17, Gradle 9.4.1, and at least one connected device or a clear note that no device is connected.

## Task 2: Add Gradle Android Project

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle/libs.versions.toml`
- Create: `gradle.properties`
- Modify: `local.properties`
- Create/keep: `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`, `gradle/wrapper/gradle-wrapper.properties`

- [ ] **Step 1: Create Gradle settings**

Create `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "TrailMate"
include(":app")
```

- [ ] **Step 2: Create root build file**

Create `build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
```

- [ ] **Step 3: Create version catalog**

Create `gradle/libs.versions.toml`:

```toml
[versions]
agp = "9.2.1"
kotlin = "2.3.0"
coreKtx = "1.17.0"
activityCompose = "1.13.0"
composeBom = "2026.06.00"
junit = "4.13.2"

[libraries]
androidx-core-ktx = { module = "androidx.core:core-ktx", version.ref = "coreKtx" }
androidx-activity-compose = { module = "androidx.activity:activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { module = "androidx.compose:compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { module = "androidx.compose.ui:ui" }
androidx-compose-ui-graphics = { module = "androidx.compose.ui:ui-graphics" }
androidx-compose-ui-tooling-preview = { module = "androidx.compose.ui:ui-tooling-preview" }
androidx-compose-material3 = { module = "androidx.compose.material3:material3" }
junit = { module = "junit:junit", version.ref = "junit" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
```

- [ ] **Step 4: Create Gradle properties**

Create `gradle.properties`:

```properties
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
```

- [ ] **Step 5: Verify Gradle can list tasks**

Run:

```powershell
.\gradlew.bat tasks --console=plain
```

Expected: task list includes `:app` tasks after Task 3; before app module exists, Gradle reports missing included project. Continue to Task 3 if the only failure is missing `app`.

## Task 3: Add Android App Module

**Files:**
- Create: `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/res/values/styles.xml`
- Create: `app/src/main/java/com/trailmate/app/MainActivity.kt`

- [ ] **Step 1: Create app module build file**

Create `app/build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.trailmate.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.trailmate.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    testImplementation(libs.junit)
}
```

- [ ] **Step 2: Create manifest**

Create `app/src/main/AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:allowBackup="true"
        android:label="TrailMate"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

- [ ] **Step 3: Create styles**

Create `app/src/main/res/values/styles.xml`:

```xml
<resources>
    <style name="AppTheme" parent="android:style/Theme.Material.Light.NoActionBar">
        <item name="android:windowLightStatusBar">true</item>
        <item name="android:navigationBarColor">#FFFFFF</item>
        <item name="android:statusBarColor">#F6F8F5</item>
    </style>
</resources>
```

- [ ] **Step 4: Create MainActivity**

Create `app/src/main/java/com/trailmate/app/MainActivity.kt`:

```kotlin
package com.trailmate.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.trailmate.app.ui.TrailMateApp
import com.trailmate.app.ui.theme.TrailMateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TrailMateTheme {
                TrailMateApp()
            }
        }
    }
}
```

## Task 4: Implement Five-Tab Compose Shell

**Files:**
- Create: `app/src/main/java/com/trailmate/app/ui/TrailMateTabs.kt`
- Create: `app/src/main/java/com/trailmate/app/ui/TrailMateApp.kt`
- Create: `app/src/main/java/com/trailmate/app/ui/theme/Color.kt`
- Create: `app/src/main/java/com/trailmate/app/ui/theme/Theme.kt`
- Create: `app/src/test/java/com/trailmate/app/ui/TrailMateTabsTest.kt`

- [ ] **Step 1: Create tab model**

Create `TrailMateTabs.kt`:

```kotlin
package com.trailmate.app.ui

enum class TrailMateTab(val label: String) {
    Discover("发现"),
    Routes("路线"),
    Navigation("导航"),
    Records("记录"),
    Profile("我的")
}
```

- [ ] **Step 2: Create tab invariant test**

Create `TrailMateTabsTest.kt`:

```kotlin
package com.trailmate.app.ui

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class TrailMateTabsTest {
    @Test
    fun bottomTabsMatchCurrentInformationArchitecture() {
        val labels = TrailMateTab.entries.map { it.label }
        assertEquals(listOf("发现", "路线", "导航", "记录", "我的"), labels)
        assertFalse(labels.contains("规划"))
    }
}
```

- [ ] **Step 3: Create theme colors**

Create `Color.kt`:

```kotlin
package com.trailmate.app.ui.theme

import androidx.compose.ui.graphics.Color

val ForestGreen = Color(0xFF0F3D2E)
val SoftGreen = Color(0xFFE7F3EC)
val AppBackground = Color(0xFFF6F8F5)
val TextPrimary = Color(0xFF1F2933)
```

- [ ] **Step 4: Create theme**

Create `Theme.kt`:

```kotlin
package com.trailmate.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val TrailMateLightColors = lightColorScheme(
    primary = ForestGreen,
    secondary = SoftGreen,
    background = AppBackground,
    onBackground = TextPrimary,
)

@Composable
fun TrailMateTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TrailMateLightColors,
        content = content,
    )
}
```

- [ ] **Step 5: Create app shell**

Create `TrailMateApp.kt`:

```kotlin
package com.trailmate.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TrailMateApp() {
    var selectedTab by rememberSaveable { mutableStateOf(TrailMateTab.Discover) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                TrailMateTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = {
                            Text(
                                text = tab.label.first().toString(),
                                fontWeight = FontWeight.Bold,
                            )
                        },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { innerPadding ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            TabContent(
                tab = selectedTab,
                paddingValues = innerPadding,
            )
        }
    }
}

@Composable
private fun TabContent(
    tab: TrailMateTab,
    paddingValues: PaddingValues,
) {
    val copy = when (tab) {
        TrailMateTab.Discover -> ScreenCopy(
            title = "发现可信路线",
            body = "查看今日天气、轻量风险提示和已验证徒步路线。",
        )
        TrailMateTab.Routes -> ScreenCopy(
            title = "路线资产",
            body = "管理已离线、已导入、收藏和最近导航的路线。",
        )
        TrailMateTab.Navigation -> ScreenCopy(
            title = "轨迹导航",
            body = "开始后显示计划路线、当前位置、偏航状态和紧急卡片。",
        )
        TrailMateTab.Records -> ScreenCopy(
            title = "记录复盘",
            body = "复盘轨迹、偏航、停留点，并提交结构化路况反馈。",
        )
        TrailMateTab.Profile -> ScreenCopy(
            title = "我的安全设置",
            body = "管理离线数据、隐私、紧急联系人和设备导航权限。",
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = copy.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = copy.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
    }
}

private data class ScreenCopy(
    val title: String,
    val body: String,
)
```

- [ ] **Step 6: Run tab test**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.trailmate.app.ui.TrailMateTabsTest" --console=plain
```

Expected: PASS.

## Task 5: Add Documentation And Verification

**Files:**
- Create: `docs/android-shell.md`
- Modify: `README.md` if it exists; otherwise create it.

- [ ] **Step 1: Create shell documentation**

Create `docs/android-shell.md` with:

```markdown
# Android Shell

TrailMate is implemented as a native Android app. The first shell owns only the fixed 5-tab information architecture: 发现 / 路线 / 导航 / 记录 / 我的.

Lovable reference prototype: D:\workSpace\trailguide-pro

The prototype guides visual rhythm and interaction copy. If it conflicts with AGENTS.md, AGENTS.md wins.

## Verify

.\gradlew.bat :app:assembleDebug
.\gradlew.bat :app:testDebugUnitTest
adb devices
.\gradlew.bat :app:installDebug
```

- [ ] **Step 2: Run build**

Run:

```powershell
.\gradlew.bat :app:assembleDebug --console=plain
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 3: Run tests**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --console=plain
```

Expected: BUILD SUCCESSFUL.

- [ ] **Step 4: Validate OpenSpec**

Run:

```powershell
openspec validate bootstrap-android-trailmate-shell
```

Expected: valid.

- [ ] **Step 5: Commit**

Run:

```powershell
git status --short
git add .
git commit -m "feat(android): bootstrap compose app shell"
```

Expected: one focused commit on the implementation worktree branch.
