## 1. Build Foundation

- [x] 1.1 Commit the Gradle Wrapper generated for Gradle 9.4.1 and verify `.\gradlew.bat --version` uses JDK 17.
- [x] 1.2 Create root Gradle Kotlin DSL settings, version catalog, and Android build configuration for a Kotlin Android app.
- [x] 1.3 Add a local-only `local.properties` path for the Windows SDK and keep it ignored.

## 2. Android App Shell

- [x] 2.1 Create the Android app module with package `com.trailmate.app`, min/target/compile SDK settings, and debug manifest.
- [x] 2.2 Add Compose + Material 3 app theme using the TrailMate forest-green visual baseline.
- [x] 2.3 Implement the five bottom tabs: `发现`, `路线`, `导航`, `记录`, `我的`.
- [x] 2.4 Add minimal Chinese placeholder screens that reflect each tab's responsibility without adding `规划`, equipment, community, marketplace, or complex pre-trip-check surfaces.

## 3. Architecture Boundaries

- [x] 3.1 Document reserved package boundaries for discover, routes, navigation, records, profile, safety, core model, core geo, core offline, core location, and tracking services.
- [x] 3.2 Add a lightweight architecture note explaining which future PR owns GPX/KML parsing, route matching, Foreground Service tracking, offline packages, and emergency card logic.
- [x] 3.3 Document the Lovable reference contract and include `D:\workSpace\trailguide-pro` as the local design source.

## 4. Verification

- [x] 4.1 Add unit or Compose tests that verify the five tabs exist and no `规划` tab is present.
- [x] 4.2 Run `.\gradlew.bat :app:assembleDebug`.
- [x] 4.3 Run `.\gradlew.bat :app:testDebugUnitTest`.
- [x] 4.4 If a connected device is available, install or smoke-launch the debug app with `adb` and record the result.
- [x] 4.5 Run `openspec validate bootstrap-android-trailmate-shell`.
