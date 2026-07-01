## ADDED Requirements

### Requirement: Route tab launches Android document picker
The Android route tab SHALL provide a real import control that launches the system document picker for GPX/KML route files without requesting broad storage permissions.

#### Scenario: User taps import
- **WHEN** the user activates `导入 GPX / KML` on the `路线` tab
- **THEN** the app launches an Android document picker for route files
- **AND** the app does not request all-files or broad external storage permission

#### Scenario: User cancels picker
- **WHEN** the document picker returns no file
- **THEN** the route tab remains usable
- **AND** the import preview shows a concise cancelled or idle state instead of a fake successful import

### Requirement: Selected document is read through importer boundary
The app SHALL read the selected document through a route import file reader boundary that derives a display filename, enforces a text size guard, and returns a structured success or failure result.

#### Scenario: Read succeeds
- **WHEN** the selected URI can be opened and is within the supported text size limit
- **THEN** the importer returns the filename and text content to the route import parser

#### Scenario: Read fails
- **WHEN** the selected URI cannot be opened, has unsupported content, or exceeds the supported text size limit
- **THEN** the route tab shows a Chinese failure state
- **AND** no route asset is silently created

### Requirement: Parsed import result updates route tab preview
The route tab SHALL update its import preview from the selected file's parser result rather than from a deterministic sample.

#### Scenario: GPX/KML parses successfully
- **WHEN** the selected file contains supported route geometry
- **THEN** the preview shows the real filename, route name, distance, elevation gain, waypoint count, track point count, elevation availability, and quality notes
- **AND** it preserves the copy `导入文件只包含路线轨迹和航点，用于轨迹导航、偏航判断和进度计算，不包含商业地图底图。`
- **AND** no route asset is silently created before the user explicitly saves it

#### Scenario: Parser rejects file
- **WHEN** the selected file is invalid XML, unsupported, or missing navigable track geometry
- **THEN** the preview shows failure labels such as `文件解析失败`, `格式不支持`, or `缺少可导航轨迹`
- **AND** distance and elevation are not shown as successful route metrics
- **AND** save or start-navigation actions are not shown for the failed file

### Requirement: Route import flow stays within route asset scope
The import flow SHALL stay inside the `路线` tab route asset center and MUST NOT introduce planner, equipment, community, marketplace, or complex pre-trip-check surfaces.

#### Scenario: Import UI is rendered
- **WHEN** the user views the route import states
- **THEN** the visible text does not include `规划`, `装备`, `社区`, `商城`, `出发前检查`, or `完成检查后开始`
