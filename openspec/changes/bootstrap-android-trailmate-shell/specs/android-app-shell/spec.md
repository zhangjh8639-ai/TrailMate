## ADDED Requirements

### Requirement: Android project foundation
The system SHALL provide a native Android project foundation using Kotlin, Gradle Kotlin DSL, Jetpack Compose, and Material 3.

#### Scenario: Debug build can be assembled
- **WHEN** a developer runs the documented debug build command
- **THEN** Gradle assembles a debug APK without requiring React Native, Flutter, WebView, or iOS tooling

#### Scenario: Unit tests can be executed
- **WHEN** a developer runs the documented unit test command
- **THEN** the Android unit test task completes and reports test results

### Requirement: Fixed five-tab information architecture
The app shell SHALL expose exactly five bottom navigation tabs named `发现`, `路线`, `导航`, `记录`, and `我的`.

#### Scenario: App starts with production IA
- **WHEN** the app launches
- **THEN** the bottom navigation shows `发现`, `路线`, `导航`, `记录`, and `我的`

#### Scenario: Legacy planning tab is absent
- **WHEN** the app shell is inspected
- **THEN** there is no bottom navigation tab named `规划`

#### Scenario: Legacy equipment and community surfaces are absent
- **WHEN** the app shell is inspected
- **THEN** there are no primary navigation entries for equipment, marketplace, community feed, likes, comments, ranking, or achievements

### Requirement: IA-aligned feature boundaries
The codebase SHALL use Android package or module boundaries that align with TrailMate's current information architecture and future navigation stack.

#### Scenario: Allowed feature boundaries exist
- **WHEN** the Android source tree is inspected
- **THEN** it contains or reserves boundaries for discover, routes, navigation, records, profile, safety, core model, core geo, core offline, core location, and tracking services

#### Scenario: Deprecated feature boundaries are not introduced
- **WHEN** the Android source tree is inspected
- **THEN** it does not introduce `planner`, `equipment`, `community`, or `pretrip_check` feature packages

### Requirement: Lovable prototype reference contract
The implementation SHALL document that the Lovable prototype at `D:\workSpace\trailguide-pro` is a design reference source, not production source code.

#### Scenario: Design source is documented
- **WHEN** a developer reads the project documentation for the Android shell
- **THEN** it explains how to use the Lovable prototype for visual and interaction reference

#### Scenario: Product rules override prototype conflicts
- **WHEN** the Lovable prototype conflicts with `AGENTS.md`
- **THEN** implementation follows `AGENTS.md` and records the conflict in review notes when relevant

### Requirement: Real-device smoke-test readiness
The Android shell SHALL provide a documented path for installing or launching the debug build on a connected Android device.

#### Scenario: Connected device can be detected
- **WHEN** a developer runs `adb devices`
- **THEN** the connected test device is visible before real-device smoke testing proceeds

#### Scenario: Smoke test path is documented
- **WHEN** a developer prepares to verify the Android shell
- **THEN** documentation lists the build, install, launch, and screenshot or UI inspection steps
