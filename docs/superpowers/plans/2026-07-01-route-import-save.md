# Route Import Save Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let users save a successfully parsed GPX/KML import preview into the `路线` tab route asset list as a private, track-only, unverified imported route.

**Architecture:** Keep this slice in the existing in-memory route tab state. Store only the parsed import context needed for save, then use a pure reducer to convert that context into a `RouteAssetCardState`; do not add Room/DataStore or route navigation side effects yet.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, existing route import parser/model, JUnit, OpenSpec.

---

## File Structure

- Modify `app/src/main/java/com/trailmate/app/feature/routes/RoutesTabState.kt`: saveable import context, save reducer, saved asset card mapping.
- Modify `app/src/main/java/com/trailmate/app/feature/routes/RoutesScreen.kt`: save callback from parsed preview action.
- Modify `app/src/main/java/com/trailmate/app/ui/TrailMateApp.kt`: connect save callback to route tab state.
- Modify `app/src/test/java/com/trailmate/app/feature/routes/RoutesTabStateTest.kt`: state, privacy/copy, idempotency, and old-surface regression coverage.
- Modify `docs/route-import-file-picker.md`: describe in-memory save behavior and remaining persistence limitation.

## Task 1: Save State Contract

**Files:**
- Modify: `app/src/test/java/com/trailmate/app/feature/routes/RoutesTabStateTest.kt`
- Modify: `app/src/main/java/com/trailmate/app/feature/routes/RoutesTabState.kt`

- [x] **Step 1: Write failing tests for parsed import save**

Add tests that express the intended API:

```kotlin
@Test
fun parsedImportCanBeSavedAsPrivateTrackOnlyAsset() {
    val parsed = RouteImportParser.parse("saved-route.gpx", successfulGpx())
    val imported = parsed.toImportedRoute(
        id = com.trailmate.app.core.model.RouteId("import-test"),
        region = "导入路线",
        importedAt = java.time.Instant.parse("2026-07-01T00:00:00Z"),
    )

    assertEquals(com.trailmate.app.core.model.PrivacyVisibility.Private, imported.visibility)
    assertEquals(com.trailmate.app.core.model.RouteOfflineStatus.TrackOnly, imported.offlineStatus)
    assertEquals(com.trailmate.app.core.model.RouteConfidence.Unverified, imported.confidence)

    val state = RoutesTabSampleState.build()
        .withImportResult(parsed)
        .withSavedImport()

    val saved = state.assets.first()
    assertEquals("测试路线", saved.name)
    assertEquals("GPX 导入", saved.sourceLabel)
    assertEquals("仅轨迹可用", saved.offlineStatusLabel)
    assertEquals("待确认", saved.estimatedDurationLabel)
    assertEquals("未验证", saved.difficultyLabel)
    assertEquals("可信度待确认", saved.confidenceLabel)
    assertTrue(saved.riskTags.contains("导入轨迹"))
    assertTrue(saved.riskTags.contains("未验证"))
    assertTrue(saved.riskTags.contains("不含地图底图"))
}
```

- [x] **Step 2: Write failing tests for idempotency and failed imports**

Add:

```kotlin
@Test
fun savingSameImportTwiceDoesNotDuplicateAsset() {
    val parsed = RouteImportParser.parse("saved-route.gpx", successfulGpx())
    val state = RoutesTabSampleState.build()
        .withImportResult(parsed)
        .withSavedImport()
        .withSavedImport()

    assertEquals(1, state.assets.count { it.sourceLabel == "GPX 导入" && it.name == "测试路线" })
}

@Test
fun failedImportCannotBeSaved() {
    val state = RoutesTabSampleState.build()
        .withImportReadFailure("broken.gpx", "文件解析失败")
        .withSavedImport()

    assertFalse(state.assets.any { it.sourceLabel == "GPX 导入" || it.sourceLabel == "KML 导入" })
    assertFalse(state.visibleText().contains("保存到路线"))
}
```

- [x] **Step 3: Run tests to verify RED**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.trailmate.app.feature.routes.RoutesTabStateTest" --console=plain
```

Expected: FAIL with unresolved `withSavedImport` and any new state properties.

- [x] **Step 4: Implement minimal state changes**

In `RoutesTabState.kt`, add saveable context:

```kotlin
data class SaveableImportState(
    val key: String,
    val routeName: String,
    val sourceLabel: String,
    val distanceLabel: String,
    val elevationGainLabel: String,
)
```

Add `saveableImport: SaveableImportState?` to `RoutesTabState`.

When `withImportResult(result)` receives parsed geometry, create a saveable state from `result`; for failed imports set it to `null`.

Implement:

```kotlin
fun RoutesTabState.withSavedImport(): RoutesTabState {
    val saveable = saveableImport ?: return this
    val card = RoutesTabSampleState.savedImportedAsset(saveable)
    return copy(
        importPreview = importPreview?.copy(
            qualityNotes = importPreview.qualityNotes
                .filterNot { it == "未保存，仅本次查看" } + "本次已加入路线列表",
        ),
        assets = listOf(card) + assets.filterNot {
            it.sourceLabel == card.sourceLabel && it.name == card.name
        },
    )
}
```

Use route asset copy:

```kotlin
RouteAssetCardState(
    name = saveable.routeName,
    region = "导入路线",
    sourceLabel = saveable.sourceLabel,
    offlineStatusLabel = "仅轨迹可用",
    distanceLabel = saveable.distanceLabel,
    elevationGainLabel = saveable.elevationGainLabel,
    estimatedDurationLabel = "待确认",
    difficultyLabel = "未验证",
    confidenceLabel = "可信度待确认",
    riskTags = listOf("导入轨迹", "未验证", "不含地图底图"),
    lastUsedLabel = "本次已加入路线列表",
    startActionLabel = null,
    detailActionLabel = null,
)
```

- [x] **Step 5: Run tests to verify GREEN**

Run:

```powershell
.\gradlew.bat :app:testDebugUnitTest --tests "com.trailmate.app.feature.routes.RoutesTabStateTest" --console=plain
```

Expected: PASS.

## Task 2: Compose Save Interaction

**Files:**
- Modify: `app/src/main/java/com/trailmate/app/feature/routes/RoutesScreen.kt`
- Modify: `app/src/main/java/com/trailmate/app/ui/TrailMateApp.kt`

- [x] **Step 1: Add save callback API**

Change `RoutesScreen` signature to include:

```kotlin
onSaveImportClick: () -> Unit = {},
```

Thread that callback into `ImportStateContent` and `ImportPreviewCard`.

- [x] **Step 2: Make only parsed preview save clickable**

In `ImportPreviewCard`, replace the static save action with a clickable action only inside `if (preview.canUseRouteActions)`:

```kotlin
StaticSecondaryAction(
    label = preview.saveActionLabel,
    modifier = Modifier.weight(1f),
    onClick = onSaveImportClick,
)
```

Keep failure preview action as:

```kotlin
StaticSecondaryAction(
    label = "重新选择文件",
    modifier = Modifier.fillMaxWidth(),
    onClick = onRetryImport,
)
```

- [x] **Step 3: Wire app state**

In `TrailMateApp`, pass:

```kotlin
onSaveImportClick = {
    routesState = routesState.withSavedImport()
}
```

- [x] **Step 4: Run build**

Run:

```powershell
.\gradlew.bat :app:assembleDebug --console=plain
```

Expected: BUILD SUCCESSFUL.

## Task 3: Documentation And Verification

**Files:**
- Modify: `docs/route-import-file-picker.md`
- Modify: `openspec/changes/save-imported-route-preview/tasks.md`

- [x] **Step 1: Update docs**

Document:

- save creates an in-memory route asset;
- saved imported assets are private/track-only/unverified in model semantics;
- no Room/DataStore persistence yet;
- saved card uses `导入路线`, `待确认`, `未验证`, and `不含地图底图` copy.

- [x] **Step 2: Run final verification**

Run:

```powershell
openspec validate save-imported-route-preview
git diff --check
.\gradlew.bat :app:testDebugUnitTest --console=plain
.\gradlew.bat :app:assembleDebug --console=plain
```

Expected: OpenSpec valid, no whitespace errors, tests pass, debug build succeeds.

- [x] **Step 3: Real-device smoke**

Run:

```powershell
D:\software\Android\Sdk\platform-tools\adb.exe devices
D:\software\Android\Sdk\platform-tools\adb.exe -s R5CX12KKJNJ install -r app\build\outputs\apk\debug\app-debug.apk
D:\software\Android\Sdk\platform-tools\adb.exe -s R5CX12KKJNJ shell am start -n com.trailmate.app/.MainActivity
```

Expected on device: import `trailmate-codex-valid.gpx`, tap `保存到路线`, and see a saved imported route card above the sample route assets with `GPX 导入`, `仅轨迹可用`, `待确认`, `未验证`, `本次已加入路线列表`, and `不含地图底图`; the imported asset card does not expose `开始导航` or `查看详情`.
