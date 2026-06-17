# TrailMate Android Compose Prototype Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the first runnable Android client prototype for TrailMate direction D: assessment, light navigation, baseline profile, personal gear, and AI gear checklist UI.

**Architecture:** Create an Android-first Compose app in `android-app/` with a single `MainActivity`, focused UI state models, mock repositories, and screen-level composables. This plan intentionally ships the polished client prototype before the Spring Boot backend and real GPX parser so visual interaction and flow can be validated early.

**Tech Stack:** Kotlin, Android Gradle Plugin, Jetpack Compose Material 3, Compose Navigation, JUnit, Compose UI tests, minSdk 26. The implementation must lock versions in `gradle/libs.versions.toml` after the Android toolchain is available.

---

## Scope

This plan implements the Android UI prototype only.

In scope:

- Android monorepo scaffold with `android-app/`
- iOS-inspired visual system in Compose: soft surfaces, large readable titles, segmented controls, bottom-sheet style panels, smooth state transitions, restrained shadows
- Login/register entry screen with privacy copy
- Baseline profile questionnaire after authentication
- Temporary low-confidence profile summary
- My Gear tab with branded gear examples and add/edit affordances
- Route detail tabs: Assessment, Route, Plan, Gear
- AI gear advisor UI with deterministic fallback messaging
- Unit tests for state rules and AI boundary
- Compose tests for major screen text and tab switching

Out of scope for this plan:

- Spring Boot backend
- real auth
- real GPX parsing
- Room persistence
- real LLM calls
- full turn-by-turn navigation
- app store packaging

## Toolchain Findings

Observed on 2026-06-16:

- `java -version` returns Java 17.
- `gradle -v` fails with `Failed to load native library 'native-platform.dll' for Windows 10 amd64`.
- `ANDROID_HOME` and `ANDROID_SDK_ROOT` are not set.

The first implementation task must repair or install the Android build toolchain before Android code verification can pass.

## File Structure

Create:

- `settings.gradle.kts`: Gradle project modules.
- `build.gradle.kts`: root plugin declarations.
- `gradle.properties`: Android and Kotlin build flags.
- `gradle/libs.versions.toml`: locked dependency versions.
- `android-app/build.gradle.kts`: Android app build configuration.
- `android-app/src/main/AndroidManifest.xml`: app manifest.
- `android-app/src/main/java/com/trailmate/app/MainActivity.kt`: Compose entry point.
- `android-app/src/main/java/com/trailmate/app/TrailMateApp.kt`: root app shell and navigation state.
- `android-app/src/main/java/com/trailmate/app/core/design/TrailMateTheme.kt`: colors, typography, shape, and motion helpers.
- `android-app/src/main/java/com/trailmate/app/core/design/TrailMateComponents.kt`: shared UI controls.
- `android-app/src/main/java/com/trailmate/app/core/model/TrailMateModels.kt`: prototype models.
- `android-app/src/main/java/com/trailmate/app/core/model/TrailMateSampleData.kt`: mock data.
- `android-app/src/main/java/com/trailmate/app/feature/onboarding/OnboardingScreens.kt`: auth and baseline profile UI.
- `android-app/src/main/java/com/trailmate/app/feature/home/HomeScreen.kt`: home/profile entry UI.
- `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`: assessment, route, plan, gear tabs.
- `android-app/src/test/java/com/trailmate/app/core/model/BaselineProfileRulesTest.kt`: profile rules.
- `android-app/src/test/java/com/trailmate/app/core/model/GearAdvisorRulesTest.kt`: gear advisor boundaries.
- `android-app/src/androidTest/java/com/trailmate/app/TrailMateAppSmokeTest.kt`: Compose smoke tests.

Modify:

- `openspec/changes/trailmate-d-light-navigation-gear/tasks.md`: mark implementation plan created.
- `CHANGELOG.md`: create an unreleased entry after the first code task.

## Task 1: Android Toolchain Gate

**Files:**
- No repository files changed unless installing a wrapper after Gradle is usable.

- [ ] **Step 1: Verify Java**

Run:

```powershell
java -version
```

Expected:

```text
java version "17...
```

- [ ] **Step 2: Verify Android SDK**

Run:

```powershell
$env:ANDROID_HOME
$env:ANDROID_SDK_ROOT
Get-ChildItem "$env:LOCALAPPDATA\Android\Sdk" -ErrorAction SilentlyContinue
```

Expected: one of `ANDROID_HOME`, `ANDROID_SDK_ROOT`, or `%LOCALAPPDATA%\Android\Sdk` points to an SDK directory containing `platforms`, `platform-tools`, and `build-tools`.

- [ ] **Step 3: Repair missing SDK**

If no SDK exists, install Android Studio or Android command line tools, then set:

```powershell
[Environment]::SetEnvironmentVariable("ANDROID_HOME", "$env:LOCALAPPDATA\Android\Sdk", "User")
[Environment]::SetEnvironmentVariable("ANDROID_SDK_ROOT", "$env:LOCALAPPDATA\Android\Sdk", "User")
```

Open a new shell and re-run Step 2.

- [ ] **Step 4: Avoid broken global Gradle**

Do not rely on the current global `gradle` command. Use a project Gradle Wrapper after it is generated from Android Studio, repaired Gradle, or a downloaded wrapper distribution.

Run:

```powershell
.\gradlew.bat --version
```

Expected: Gradle version prints without `native-platform.dll` failure.

## Task 2: Scaffold Android Project

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle.properties`
- Create: `gradle/libs.versions.toml`
- Create: `android-app/build.gradle.kts`
- Create: `android-app/src/main/AndroidManifest.xml`
- Create: `android-app/src/main/java/com/trailmate/app/MainActivity.kt`

- [ ] **Step 1: Create project settings**

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
include(":android-app")
```

- [ ] **Step 2: Create root build file**

Create `build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
}
```

- [ ] **Step 3: Create Gradle properties**

Create `gradle.properties`:

```properties
org.gradle.jvmargs=-Xmx3072m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
android.nonTransitiveRClass=true
```

- [ ] **Step 4: Create version catalog**

Create `gradle/libs.versions.toml` with versions verified from official release notes during execution:

```toml
[versions]
agp = "9.2.0"
kotlin = "2.4.0"
composeBom = "2026.04.00"
activityCompose = "1.12.0"
navigationCompose = "2.9.0"
junit = "4.13.2"
androidxJunit = "1.3.0"
espresso = "3.7.0"

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }

[libraries]
androidx-activity-compose = { module = "androidx.activity:activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { module = "androidx.compose:compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { module = "androidx.compose.ui:ui" }
androidx-compose-ui-graphics = { module = "androidx.compose.ui:ui-graphics" }
androidx-compose-ui-tooling-preview = { module = "androidx.compose.ui:ui-tooling-preview" }
androidx-compose-material3 = { module = "androidx.compose.material3:material3" }
androidx-navigation-compose = { module = "androidx.navigation:navigation-compose", version.ref = "navigationCompose" }
junit = { module = "junit:junit", version.ref = "junit" }
androidx-junit = { module = "androidx.test.ext:junit", version.ref = "androidxJunit" }
espresso-core = { module = "androidx.test.espresso:espresso-core", version.ref = "espresso" }
androidx-compose-ui-test-junit4 = { module = "androidx.compose.ui:ui-test-junit4" }
androidx-compose-ui-tooling = { module = "androidx.compose.ui:ui-tooling" }
androidx-compose-ui-test-manifest = { module = "androidx.compose.ui:ui-test-manifest" }
```

- [ ] **Step 5: Create Android app build file**

Create `android-app/build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.trailmate.app"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.trailmate.app"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        compose = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.navigation.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
```

- [ ] **Step 6: Create manifest**

Create `android-app/src/main/AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <application
        android:allowBackup="false"
        android:icon="@mipmap/ic_launcher"
        android:label="TrailMate"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.TrailMate">
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

- [ ] **Step 7: Create minimal activity**

Create `android-app/src/main/java/com/trailmate/app/MainActivity.kt`:

```kotlin
package com.trailmate.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.trailmate.app.core.design.TrailMateTheme

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

- [ ] **Step 8: Run scaffold verification**

Run:

```powershell
.\gradlew.bat :android-app:tasks
```

Expected: task list prints for `:android-app`.

- [ ] **Step 9: Commit scaffold**

Run:

```powershell
git add settings.gradle.kts build.gradle.kts gradle.properties gradle android-app
git commit -m "chore(android): scaffold compose app"
```

## Task 3: Add Prototype Models With TDD

**Files:**
- Create: `android-app/src/test/java/com/trailmate/app/core/model/BaselineProfileRulesTest.kt`
- Create: `android-app/src/test/java/com/trailmate/app/core/model/GearAdvisorRulesTest.kt`
- Create: `android-app/src/main/java/com/trailmate/app/core/model/TrailMateModels.kt`
- Create: `android-app/src/main/java/com/trailmate/app/core/model/TrailMateSampleData.kt`

- [ ] **Step 1: Write failing baseline profile test**

Create `BaselineProfileRulesTest.kt`:

```kotlin
package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BaselineProfileRulesTest {
    @Test
    fun questionnaireProfileStartsWithLowConfidence() {
        val profile = BaselineProfile(
            exerciseFrequency = ExerciseFrequency.ONE_TO_TWO_PER_WEEK,
            typicalDuration = TypicalDuration.MIN_30_TO_60,
            experienceLevel = ExperienceLevel.REGULAR,
            ascentExperience = AscentExperience.M300_TO_800,
            heightCm = 172,
            weightKg = 68,
            commonPackWeightKg = 5
        )

        assertEquals(ConfidenceLevel.LOW, profile.initialConfidence())
        assertTrue(profile.explanation().contains("GPX"))
    }
}
```

- [ ] **Step 2: Verify red**

Run:

```powershell
.\gradlew.bat :android-app:testDebugUnitTest --tests "*BaselineProfileRulesTest"
```

Expected: FAIL because `BaselineProfile` is not defined.

- [ ] **Step 3: Implement models**

Create `TrailMateModels.kt`:

```kotlin
package com.trailmate.app.core.model

enum class ExerciseFrequency { RARELY, ONE_TO_TWO_PER_WEEK, THREE_PLUS_PER_WEEK }
enum class TypicalDuration { UNDER_30, MIN_30_TO_60, OVER_60 }
enum class ExperienceLevel { BEGINNER, REGULAR, EXPERIENCED }
enum class AscentExperience { UNDER_300, M300_TO_800, OVER_800 }
enum class ConfidenceLevel { LOW, MEDIUM, HIGH }
enum class MatchLevel { RECOMMENDED, CAUTION, NOT_RECOMMENDED }
enum class GearStatus { COVERED, CHECK, MISSING, OPTIONAL }

data class BaselineProfile(
    val exerciseFrequency: ExerciseFrequency,
    val typicalDuration: TypicalDuration,
    val experienceLevel: ExperienceLevel,
    val ascentExperience: AscentExperience,
    val heightCm: Int?,
    val weightKg: Int?,
    val commonPackWeightKg: Int?
) {
    fun initialConfidence(): ConfidenceLevel = ConfidenceLevel.LOW

    fun explanation(): String =
        "This temporary profile uses questionnaire defaults until enough GPX history is imported."
}

data class GearItem(
    val id: String,
    val category: String,
    val brand: String?,
    val model: String?,
    val weightGrams: Int?,
    val available: Boolean
)

data class GearRecommendation(
    val category: String,
    val status: GearStatus,
    val rationale: String,
    val matchedGearItemId: String? = null
)

data class RouteAssessmentSummary(
    val routeName: String,
    val distanceKm: Double,
    val ascentMeters: Int,
    val matchLevel: MatchLevel,
    val confidenceLevel: ConfidenceLevel,
    val estimatedDurationRange: String,
    val risks: List<String>
)
```

- [ ] **Step 4: Verify green**

Run:

```powershell
.\gradlew.bat :android-app:testDebugUnitTest --tests "*BaselineProfileRulesTest"
```

Expected: PASS.

- [ ] **Step 5: Write failing AI boundary test**

Create `GearAdvisorRulesTest.kt`:

```kotlin
package com.trailmate.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class GearAdvisorRulesTest {
    @Test
    fun gearRecommendationsDoNotChangeRouteAssessment() {
        val assessment = TrailMateSampleData.routeAssessment
        val checklist = TrailMateSampleData.gearRecommendations

        assertEquals(MatchLevel.CAUTION, assessment.matchLevel)
        assertEquals("6:40-7:50", assessment.estimatedDurationRange)
        assertEquals(GearStatus.MISSING, checklist.first { it.category == "Trekking poles" }.status)
    }
}
```

- [ ] **Step 6: Verify red**

Run:

```powershell
.\gradlew.bat :android-app:testDebugUnitTest --tests "*GearAdvisorRulesTest"
```

Expected: FAIL because `TrailMateSampleData` is not defined.

- [ ] **Step 7: Add sample data**

Create `TrailMateSampleData.kt`:

```kotlin
package com.trailmate.app.core.model

object TrailMateSampleData {
    val baselineProfile = BaselineProfile(
        exerciseFrequency = ExerciseFrequency.ONE_TO_TWO_PER_WEEK,
        typicalDuration = TypicalDuration.MIN_30_TO_60,
        experienceLevel = ExperienceLevel.REGULAR,
        ascentExperience = AscentExperience.M300_TO_800,
        heightCm = 172,
        weightKg = 68,
        commonPackWeightKg = 5
    )

    val gearItems = listOf(
        GearItem("shoes-1", "Hiking shoes", "Salomon", "X Ultra 4 GTX", 760, true),
        GearItem("shell-1", "Rain shell", "Patagonia", "Torrentshell", 400, true),
        GearItem("headlamp-1", "Headlamp", "Black Diamond", "Spot 400", 86, true)
    )

    val routeAssessment = RouteAssessmentSummary(
        routeName = "Longjing Ridge",
        distanceKm = 15.2,
        ascentMeters = 860,
        matchLevel = MatchLevel.CAUTION,
        confidenceLevel = ConfidenceLevel.MEDIUM,
        estimatedDurationRange = "6:40-7:50",
        risks = listOf("Late-stage ascent remains high", "Long climb before checkpoint")
    )

    val gearRecommendations = listOf(
        GearRecommendation("Rain shell", GearStatus.COVERED, "Existing shell covers wind and light rain.", "shell-1"),
        GearRecommendation("Headlamp", GearStatus.CHECK, "Expected finish may be late; check battery.", "headlamp-1"),
        GearRecommendation("Trekking poles", GearStatus.MISSING, "Long descent and late climb make poles useful."),
        GearRecommendation("Warm layer", GearStatus.MISSING, "High point stops and late finish may feel cold.")
    )
}
```

- [ ] **Step 8: Verify green**

Run:

```powershell
.\gradlew.bat :android-app:testDebugUnitTest
```

Expected: all unit tests PASS.

- [ ] **Step 9: Commit models**

Run:

```powershell
git add android-app/src/main/java/com/trailmate/app/core/model android-app/src/test/java/com/trailmate/app/core/model
git commit -m "test(android): model prototype profile and gear rules"
```

## Task 4: Build iOS-Inspired Design System

**Files:**
- Create: `android-app/src/main/java/com/trailmate/app/core/design/TrailMateTheme.kt`
- Create: `android-app/src/main/java/com/trailmate/app/core/design/TrailMateComponents.kt`

- [ ] **Step 1: Create theme**

Create `TrailMateTheme.kt`:

```kotlin
package com.trailmate.app.core.design

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFF27694B),
    secondary = Color(0xFFE77346),
    background = Color(0xFFF5F5F2),
    surface = Color(0xFFFFFFFF),
    onPrimary = Color.White,
    onBackground = Color(0xFF18211C),
    onSurface = Color(0xFF18211C)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF97D8AF),
    secondary = Color(0xFFF0B28D),
    background = Color(0xFF101615),
    surface = Color(0xFF1B2721),
    onPrimary = Color(0xFF102017),
    onBackground = Color(0xFFF4F8F2),
    onSurface = Color(0xFFF4F8F2)
)

@Composable
fun TrailMateTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
```

- [ ] **Step 2: Create shared components**

Create `TrailMateComponents.kt`:

```kotlin
package com.trailmate.app.core.design

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun TrailMatePanel(
    title: String,
    value: String,
    caption: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.11f)
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
            Text(value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(caption, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f))
        }
    }
}

@Composable
fun TrailMateSegmentedControl(
    labels: List<String>,
    selected: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(999.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.09f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        labels.forEach { label ->
            val active = label == selected
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(999.dp))
                    .clickable { onSelected(label) },
                color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface.copy(alpha = 0f),
                shape = RoundedCornerShape(999.dp)
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = if (active) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.72f),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
```

- [ ] **Step 3: Build compile check**

Run:

```powershell
.\gradlew.bat :android-app:compileDebugKotlin
```

Expected: Kotlin compilation PASS.

- [ ] **Step 4: Commit design system**

Run:

```powershell
git add android-app/src/main/java/com/trailmate/app/core/design
git commit -m "feat(android): add trailmate compose design system"
```

## Task 5: Build App Shell And Onboarding Flow

**Files:**
- Create: `android-app/src/main/java/com/trailmate/app/TrailMateApp.kt`
- Create: `android-app/src/main/java/com/trailmate/app/feature/onboarding/OnboardingScreens.kt`

- [ ] **Step 1: Write Compose smoke test for onboarding copy**

Create `TrailMateAppSmokeTest.kt`:

```kotlin
package com.trailmate.app

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.trailmate.app.core.design.TrailMateTheme
import org.junit.Rule
import org.junit.Test

class TrailMateAppSmokeTest {
    @get:Rule
    val compose = createComposeRule()

    @Test
    fun showsTrailMateOnboarding() {
        compose.setContent {
            TrailMateTheme {
                TrailMateApp()
            }
        }

        compose.onNodeWithText("TrailMate").assertExists()
        compose.onNodeWithText("Start baseline profile").assertExists()
    }
}
```

- [ ] **Step 2: Verify red**

Run:

```powershell
.\gradlew.bat :android-app:connectedDebugAndroidTest
```

Expected: FAIL because `TrailMateApp` is not defined.

- [ ] **Step 3: Add app shell**

Create `TrailMateApp.kt`:

```kotlin
package com.trailmate.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.trailmate.app.feature.home.HomeScreen
import com.trailmate.app.feature.onboarding.OnboardingScreen

enum class TrailMateScreen { ONBOARDING, HOME }

@Composable
fun TrailMateApp() {
    var screen by remember { mutableStateOf(TrailMateScreen.ONBOARDING) }

    when (screen) {
        TrailMateScreen.ONBOARDING -> OnboardingScreen(onComplete = { screen = TrailMateScreen.HOME })
        TrailMateScreen.HOME -> HomeScreen()
    }
}
```

- [ ] **Step 4: Add onboarding UI**

Create `OnboardingScreens.kt`:

```kotlin
package com.trailmate.app.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trailmate.app.core.design.TrailMatePanel

@Composable
fun OnboardingScreen(onComplete: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Text("TrailMate", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text(
            "Personal route assessment, light navigation, and route-ready gear checks.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.72f)
        )
        TrailMatePanel(
            title = "Baseline profile",
            value = "60 sec",
            caption = "Used only as low-confidence defaults until your GPX history is ready."
        )
        Button(onClick = onComplete) {
            Text("Start baseline profile")
        }
        Text(
            "TrailMate does not guarantee safety, provide rescue, or make medical judgments.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f)
        )
    }
}
```

- [ ] **Step 5: Add temporary HomeScreen stub**

Create `HomeScreen.kt`:

```kotlin
package com.trailmate.app.feature.home

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun HomeScreen() {
    Text("Home")
}
```

- [ ] **Step 6: Verify green**

Run:

```powershell
.\gradlew.bat :android-app:connectedDebugAndroidTest
```

Expected: onboarding smoke test PASS on emulator/device.

- [ ] **Step 7: Commit onboarding**

Run:

```powershell
git add android-app/src/main/java/com/trailmate/app android-app/src/androidTest/java/com/trailmate/app
git commit -m "feat(android): add onboarding prototype flow"
```

## Task 6: Build Home, Gear, And Route Detail Screens

**Files:**
- Modify: `android-app/src/main/java/com/trailmate/app/feature/home/HomeScreen.kt`
- Create: `android-app/src/main/java/com/trailmate/app/feature/route/RouteDetailScreen.kt`

- [ ] **Step 1: Add route tab smoke test**

Append to `TrailMateAppSmokeTest.kt`:

```kotlin
@Test
fun routeDetailShowsAssessmentRoutePlanAndGearTabs() {
    compose.setContent {
        TrailMateTheme {
            com.trailmate.app.feature.route.RouteDetailScreen()
        }
    }

    compose.onNodeWithText("Assessment").assertExists()
    compose.onNodeWithText("Route").assertExists()
    compose.onNodeWithText("Plan").assertExists()
    compose.onNodeWithText("Gear").assertExists()
}
```

- [ ] **Step 2: Verify red**

Run:

```powershell
.\gradlew.bat :android-app:connectedDebugAndroidTest --tests "*routeDetailShowsAssessmentRoutePlanAndGearTabs"
```

Expected: FAIL because `RouteDetailScreen` is not defined.

- [ ] **Step 3: Implement route detail UI**

Create `RouteDetailScreen.kt`:

```kotlin
package com.trailmate.app.feature.route

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trailmate.app.core.design.TrailMatePanel
import com.trailmate.app.core.design.TrailMateSegmentedControl
import com.trailmate.app.core.model.TrailMateSampleData

@Composable
fun RouteDetailScreen() {
    var selected by remember { mutableStateOf("Assessment") }
    val assessment = TrailMateSampleData.routeAssessment

    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(assessment.routeName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        TrailMateSegmentedControl(
            labels = listOf("Assessment", "Route", "Plan", "Gear"),
            selected = selected,
            onSelected = { selected = it }
        )
        when (selected) {
            "Assessment" -> TrailMatePanel(
                title = "Cautious attempt",
                value = assessment.estimatedDurationRange,
                caption = "${assessment.distanceKm} km / +${assessment.ascentMeters} m / confidence ${assessment.confidenceLevel}"
            )
            "Route" -> {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.16f))
                )
                TrailMatePanel("Next checkpoint", "1.8 km", "Expected 38-46 min. Fuel check before long climb.")
            }
            "Plan" -> TrailMatePanel("Plan checkpoints", "8:45", "Fuel check, risk start, rest check, offline saved.")
            "Gear" -> GearChecklist()
        }
    }
}

@Composable
private fun GearChecklist() {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        TrailMateSampleData.gearRecommendations.forEach { item ->
            TrailMatePanel(
                title = item.status.name.lowercase().replaceFirstChar { it.titlecase() },
                value = item.category,
                caption = item.rationale
            )
        }
    }
}
```

- [ ] **Step 4: Implement HomeScreen**

Replace `HomeScreen.kt` with:

```kotlin
package com.trailmate.app.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.trailmate.app.core.design.TrailMatePanel
import com.trailmate.app.feature.route.RouteDetailScreen

@Composable
fun HomeScreen() {
    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("山行教练", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        TrailMatePanel("Temporary profile", "LOW", "Import 3 GPX activities to calibrate with real history.")
        RouteDetailScreen()
    }
}
```

- [ ] **Step 5: Verify green**

Run:

```powershell
.\gradlew.bat :android-app:connectedDebugAndroidTest
.\gradlew.bat :android-app:testDebugUnitTest
```

Expected: all tests PASS.

- [ ] **Step 6: Commit route UI**

Run:

```powershell
git add android-app/src/main/java/com/trailmate/app/feature android-app/src/androidTest/java/com/trailmate/app
git commit -m "feat(android): add route tabs and gear checklist prototype"
```

## Task 7: Update Project Docs And OpenSpec Status

**Files:**
- Create: `CHANGELOG.md`
- Modify: `openspec/changes/trailmate-d-light-navigation-gear/tasks.md`

- [ ] **Step 1: Create changelog**

Create `CHANGELOG.md`:

```markdown
# Changelog

## Unreleased

- Added Android Compose prototype plan for TrailMate direction D.
- Planned first client iteration for baseline profile, light navigation, personal gear, and AI gear checklist UI.
```

- [ ] **Step 2: Update OpenSpec tasks**

Modify `openspec/changes/trailmate-d-light-navigation-gear/tasks.md` so the design review and implementation plan items read:

```markdown
- [x] Review this OpenSpec change with the user.
- [x] After review, create a Superpowers implementation plan before coding.
```

- [ ] **Step 3: Final verification**

Run:

```powershell
.\gradlew.bat :android-app:testDebugUnitTest
.\gradlew.bat :android-app:connectedDebugAndroidTest
git status --short
```

Expected:

- unit tests PASS
- connected tests PASS
- only intended doc files are modified before commit

- [ ] **Step 4: Commit docs**

Run:

```powershell
git add CHANGELOG.md openspec/changes/trailmate-d-light-navigation-gear/tasks.md docs/superpowers/plans/2026-06-16-trailmate-android-compose-prototype.md
git commit -m "docs(android): plan compose prototype implementation"
```

## Self-Review

Spec coverage:

- Baseline profile: Task 3 models, Task 5 onboarding UI.
- Light navigation: Task 6 Route tab.
- Personal gear: Task 3 gear models, Task 6 Gear checklist display.
- AI gear advisor boundary: Task 3 boundary test, Task 6 Gear tab UI.
- iOS-like UI polish: Task 4 design system and Task 6 route UI.

Known execution risks:

- Android SDK is not currently detected.
- Global Gradle currently fails on `native-platform.dll`.
- The plan must not proceed to Android code verification until Task 1 succeeds.

Type consistency:

- `BaselineProfile`, `GearItem`, `GearRecommendation`, and `RouteAssessmentSummary` are defined before UI code consumes them.
- Route tab labels are exactly `Assessment`, `Route`, `Plan`, and `Gear`.
- Gear statuses are exactly `COVERED`, `CHECK`, `MISSING`, and `OPTIONAL`.

## Progress Update: 2026-06-17

- Added a sign-in/register prototype step before questionnaire intake.
- Replaced the static baseline profile panel with a real saveable questionnaire for exercise rhythm, session duration, outdoor experience, ascent history, height, weight, and pack weight.
- Fed the completed questionnaire into the Home profile summary while preserving LOW confidence until GPX evidence exists; skipping leaves body and pack fields unset instead of applying sample values.
- Added a target-route import gate so route assessment, light navigation, plan, and gear tabs appear after a GPX import action rather than as the default Home state.
- Added a tested target-route GPX parser for trkpt/rtept points, route name, distance, ascent, and point count, currently wired to the sample import action. The parser rejects DOCTYPE declarations and chooses track points before route points when both are present.
- Added Android `OpenDocument` route import wiring so users can pick a GPX file through the system picker; parse failures are shown as recoverable UI errors while any previous valid route remains available.
- Added a deterministic route assessment engine that uses the temporary questionnaire profile plus parsed route distance/ascent to produce match level, confidence, duration range, and risk text.
- Added a deterministic hike plan engine that converts the imported route assessment into start, energy, rest, risk, and finish checkpoints, and wired Route/Plan tabs to those checkpoints.
- Added an Active Hike route-tab prototype so users can start, pause/resume, advance to the next deterministic checkpoint, and see progress without claiming GPS-grade navigation yet.
- Added a deterministic route-aware gear advisor fallback so route distance, ascent, ETA, and concrete route risks generate the Gear tab checklist while leaving route assessment unchanged.
- Added `GearInventory` rules so available owned gear can satisfy matching route recommendations without changing deterministic route assessment values.
- Added a prototype `SharedPreferences` snapshot store with a tested codec so baseline profile, personal gear, and the last imported target route can be restored after app restart.
- Added a Home-level `Route` / `My Gear` switch, a saveable in-memory My Gear add form, availability switches, delete actions, and route Gear-tab actions that send missing categories into the inventory form.
- Added a My Gear `Inventory` / `Details` split plus per-item route readiness summaries so owned brand gear can show why it does or does not satisfy the current route checklist.
- Added unit tests for gear inventory matching, detail summaries, unavailable/deleted gear, optional brand/model entry, invalid input rejection, and Compose smoke-test coverage for the My Gear screen, details tab, matched gear copy, and route-to-gear add flow.
- Still pending for production: real auth, production import queue/retry state machine, historical GPX capability profile, GPS/location-backed navigation session, Room persistence, profile/gear sync, edit/delete/export behavior, and a real AI gear advisor backend contract.
