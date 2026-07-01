## 1. Build Foundation

- [ ] 1.1 Commit the Gradle Wrapper generated for Gradle 9.4.1 and verify `.\gradlew.bat --version` uses JDK 17.
- [ ] 1.2 Create root Gradle Kotlin DSL settings, version catalog, and Android build configuration for a Kotlin Android app.
- [ ] 1.3 Add a local-only `local.properties` path for the Windows SDK and keep it ignored.

## 2. Android App Shell

- [ ] 2.1 Create the Android app module with package `com.trailmate.app`, min/target/compile SDK settings, and debug manifest.
- [ ] 2.2 Add Compose + Material 3 app theme using the TrailMate forest-green visual baseline.
- [ ] 2.3 Implement the five bottom tabs: `发现`, `路线`, `导航`, `记录`, `我的`.
- [ ] 2.4 Add minimal Chinese placeholder screens that reflect each tab's responsibility without adding `规划`, equipment, community, marketplace, or complex pre-trip-check surfaces.

## 3. Architecture Boundaries

- [ ] 3.1 Create source package boundaries for discover, routes, navigation, records, profile, safety, core model, core geo, core offline, core location, and tracking services.
- [ ] 3.2 Add a lightweight architecture note explaining which future PR owns GPX/KML parsing, route matching, Foreground Service tracking, offline packages, and emergency card logic.
- [ ] 3.3 Document the Lovable reference contract and include `D:\workSpace\trailguide-pro` as the local design source.

## 4. Verification

- [ ] 4.1 Add unit or Compose tests that verify the five tabs exist and no `规划` tab is present.
- [ ] 4.2 Run `.\gradlew.bat :app:assembleDebug`.
- [ ] 4.3 Run `.\gradlew.bat :app:testDebugUnitTest`.
- [ ] 4.4 If a connected device is available, install or smoke-launch the debug app with `adb` and record the result.
- [ ] 4.5 Run `openspec validate bootstrap-android-trailmate-shell`.
