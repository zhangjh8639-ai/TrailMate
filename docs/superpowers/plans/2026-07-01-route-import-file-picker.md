# Route Import File Picker Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let the Android `路线` tab import a real GPX/KML document through the Android system document picker and show the parsed result.

**Architecture:** Keep Compose responsible for launching the picker and rendering state. Put document reading in a small Android boundary and keep route tab state reducers pure so JVM tests can verify visible copy and failure behavior.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, AndroidX Activity Result API, existing `RouteImportParser`, JUnit.

---

## File Structure

- Modify `app/src/main/java/com/trailmate/app/feature/routes/RoutesTabState.kt`: import flow states and reducer helpers.
- Modify `app/src/main/java/com/trailmate/app/feature/routes/RoutesScreen.kt`: clickable import action and visible states.
- Modify `app/src/main/java/com/trailmate/app/ui/TrailMateApp.kt`: state holder, document picker launcher, and selected-file handling.
- Create `app/src/main/java/com/trailmate/app/feature/routes/RouteImportFileReader.kt`: Android `ContentResolver`/`Uri` read boundary.
- Modify `app/src/test/java/com/trailmate/app/feature/routes/RoutesTabStateTest.kt`: reducer and copy coverage.
- Create `app/src/test/java/com/trailmate/app/feature/routes/RouteImportFileReaderTest.kt`: pure helper coverage for supported file and read-result mapping.
- Add `docs/route-import-file-picker.md`: user/product-facing implementation note.

## Task 1: Route Tab Import State

**Files:**
- Modify: `app/src/main/java/com/trailmate/app/feature/routes/RoutesTabState.kt`
- Modify: `app/src/test/java/com/trailmate/app/feature/routes/RoutesTabStateTest.kt`

- [ ] **Step 1: Write failing tests for default idle state**

```kotlin
@Test
fun defaultRouteTabDoesNotShowFakeSuccessfulImport() {
    val state = RoutesTabSampleState.build()

    assertNull(state.importPreview)
    assertTrue(state.visibleText().contains("选择 GPX/KML 文件后显示解析结果"))
    assertFalse(state.visibleText().joinToString("\n").contains("longjing-loop.gpx"))
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\\gradlew.bat :app:testDebugUnitTest --tests "com.trailmate.app.feature.routes.RoutesTabStateTest.defaultRouteTabDoesNotShowFakeSuccessfulImport" --console=plain`

Expected: FAIL because the current sample state still shows `longjing-loop.gpx`.

- [ ] **Step 3: Write failing tests for selected-file success and failure**

```kotlin
@Test
fun selectedImportResultShowsRealFileMetrics() {
    val result = RouteImportParser.parse("my-route.gpx", successfulGpx())

    val state = RoutesTabSampleState.build().withImportResult(result)

    val preview = requireNotNull(state.importPreview)
    assertEquals("my-route.gpx", preview.fileName)
    assertEquals("解析完成", preview.statusLabel)
    assertTrue(preview.routeOnlyCopy.contains("不包含商业地图底图"))
}

@Test
fun failedReadShowsFailureWithoutCreatingImportedAsset() {
    val state = RoutesTabSampleState.build().withImportReadFailure(
        fileName = "large-route.gpx",
        reason = "文件过大，暂不支持直接导入"
    )

    val preview = requireNotNull(state.importPreview)
    assertEquals("large-route.gpx", preview.fileName)
    assertEquals("导入失败", preview.statusLabel)
    assertEquals("不可用", preview.distanceLabel)
    assertFalse(state.assets.any { it.name == preview.routeName && it.sourceLabel == "GPX 导入" })
}
```

- [ ] **Step 4: Run tests to verify they fail**

Run: `.\\gradlew.bat :app:testDebugUnitTest --tests "com.trailmate.app.feature.routes.RoutesTabStateTest" --console=plain`

Expected: FAIL because reducer APIs and idle state do not exist yet.

- [ ] **Step 5: Implement minimal state model**

Add these concepts in `RoutesTabState.kt`:

```kotlin
data class RouteImportEmptyState(
    val title: String = "导入 GPX / KML",
    val body: String = "选择 GPX/KML 文件后显示解析结果",
)

enum class RouteImportFlowStatus {
    Idle,
    Importing,
    Cancelled,
    PreviewReady,
    Failed,
}
```

Then add reducer helpers:

```kotlin
fun RoutesTabState.withImporting(): RoutesTabState = copy(
    importFlowStatus = RouteImportFlowStatus.Importing,
    importPreview = null,
)

fun RoutesTabState.withImportCancelled(): RoutesTabState = copy(
    importFlowStatus = RouteImportFlowStatus.Cancelled,
    importPreview = null,
)

fun RoutesTabState.withImportResult(result: RouteImportResult): RoutesTabState {
    val preview = RoutesTabSampleState.previewFromImportResult(result)
    return copy(
        importFlowStatus = if (result.geometry == null) RouteImportFlowStatus.Failed else RouteImportFlowStatus.PreviewReady,
        importPreview = preview,
    )
}
```

- [ ] **Step 6: Run tests to verify green**

Run: `.\\gradlew.bat :app:testDebugUnitTest --tests "com.trailmate.app.feature.routes.RoutesTabStateTest" --console=plain`

Expected: PASS.

## Task 2: Route Import File Reader

**Files:**
- Create: `app/src/main/java/com/trailmate/app/feature/routes/RouteImportFileReader.kt`
- Create: `app/src/test/java/com/trailmate/app/feature/routes/RouteImportFileReaderTest.kt`

- [ ] **Step 1: Write failing pure helper tests**

```kotlin
@Test
fun supportedImportFileRecognizesGpxKmlAndGenericMimeFallback() {
    assertTrue(RouteImportFileReader.isSupportedFile("track.gpx", null))
    assertTrue(RouteImportFileReader.isSupportedFile("track.kml", "application/octet-stream"))
    assertTrue(RouteImportFileReader.isSupportedFile("route", "application/gpx+xml"))
    assertFalse(RouteImportFileReader.isSupportedFile("notes.txt", "text/plain"))
}

@Test
fun sizeGuardRejectsOversizedText() {
    val result = RouteImportFileReader.textFromBytes(
        fileName = "huge.gpx",
        bytes = ByteArray(RouteImportFileReader.MaxImportBytes + 1) { '<'.code.toByte() },
    )

    assertTrue(result is RouteImportFileReadResult.Failed)
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `.\\gradlew.bat :app:testDebugUnitTest --tests "com.trailmate.app.feature.routes.RouteImportFileReaderTest" --console=plain`

Expected: FAIL because the reader does not exist.

- [ ] **Step 3: Implement minimal reader**

Create `RouteImportFileReader.kt` with:

```kotlin
sealed interface RouteImportFileReadResult {
    data class Success(val fileName: String, val content: String) : RouteImportFileReadResult
    data class Failed(val fileName: String, val reason: String) : RouteImportFileReadResult
}

object RouteImportFileReader {
    const val MaxImportBytes: Int = 2 * 1024 * 1024

    fun isSupportedFile(fileName: String, mimeType: String?): Boolean { /* extension or MIME check */ }

    fun textFromBytes(fileName: String, bytes: ByteArray): RouteImportFileReadResult { /* size guard + UTF-8 text */ }
}
```

Add an Android read method that uses `ContentResolver.openInputStream(uri)` and returns `RouteImportFileReadResult`.

- [ ] **Step 4: Run reader tests**

Run: `.\\gradlew.bat :app:testDebugUnitTest --tests "com.trailmate.app.feature.routes.RouteImportFileReaderTest" --console=plain`

Expected: PASS.

## Task 3: Compose And App Wiring

**Files:**
- Modify: `app/src/main/java/com/trailmate/app/feature/routes/RoutesScreen.kt`
- Modify: `app/src/main/java/com/trailmate/app/ui/TrailMateApp.kt`

- [ ] **Step 1: Add screen callback API**

Change `RoutesScreen` signature:

```kotlin
fun RoutesScreen(
    modifier: Modifier = Modifier,
    state: RoutesTabState,
    onImportClick: () -> Unit,
)
```

Make `ImportActionButton` clickable via `Modifier.clickable(onClick = onImportClick)`.

- [ ] **Step 2: Render import states**

Display:
- Idle: `选择 GPX/KML 文件后显示解析结果`
- Importing: `正在解析路线文件`
- Cancelled: `已取消导入`
- Failed: failure preview from reducer
- Success: existing preview card

- [ ] **Step 3: Wire app picker**

In `TrailMateApp`, remember `RoutesTabState`, register:

```kotlin
val importLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.OpenDocument(),
) { uri ->
    if (uri == null) {
        routesState = routesState.withImportCancelled()
    } else {
        routesState = routesState.withImporting()
        val read = RouteImportFileReader.read(context.contentResolver, uri)
        routesState = when (read) {
            is RouteImportFileReadResult.Success -> routesState.withImportResult(
                RouteImportParser.parse(read.fileName, read.content)
            )
            is RouteImportFileReadResult.Failed -> routesState.withImportReadFailure(read.fileName, read.reason)
        }
    }
}
```

Launch with MIME types:

```kotlin
importLauncher.launch(arrayOf("application/gpx+xml", "application/vnd.google-earth.kml+xml", "application/xml", "text/xml", "*/*"))
```

- [ ] **Step 4: Run full unit tests**

Run: `.\\gradlew.bat :app:testDebugUnitTest --console=plain`

Expected: PASS.

## Task 4: Docs And Verification

**Files:**
- Create: `docs/route-import-file-picker.md`
- Modify: `openspec/changes/add-route-import-file-picker/tasks.md`

- [ ] **Step 1: Add docs**

Document:
- what this slice adds;
- what it does not add;
- supported file behavior;
- privacy/storage implication: no broad storage permission;
- manual verification steps.

- [ ] **Step 2: Validate OpenSpec**

Run: `openspec validate add-route-import-file-picker`

Expected: `Change 'add-route-import-file-picker' is valid`.

- [ ] **Step 3: Build debug APK**

Run: `.\\gradlew.bat :app:assembleDebug --console=plain`

Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Real-device smoke**

Run:

```powershell
D:\software\Android\Sdk\platform-tools\adb.exe devices
D:\software\Android\Sdk\platform-tools\adb.exe install -r app\build\outputs\apk\debug\app-debug.apk
D:\software\Android\Sdk\platform-tools\adb.exe shell am start -n com.trailmate.app/.MainActivity
```

Expected: app launches on the connected test phone and route tab import control opens the system picker.
