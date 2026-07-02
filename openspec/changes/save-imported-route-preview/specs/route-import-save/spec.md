## ADDED Requirements

### Requirement: Parsed import preview can be saved
The route tab SHALL let a user save a successfully parsed GPX/KML import preview into the route asset list.

#### Scenario: Save successful parsed GPX
- **WHEN** a parsed GPX import preview is visible and the user activates `保存到路线`
- **THEN** the route tab shows an imported route asset for that route
- **AND** the asset source is labeled `GPX 导入`
- **AND** the asset offline status is labeled `仅轨迹可用`
- **AND** the asset is not presented as a verified platform route

#### Scenario: Save successful parsed KML
- **WHEN** a parsed KML import preview is visible and the user activates `保存到路线`
- **THEN** the route tab shows an imported route asset for that route
- **AND** the asset source is labeled `KML 导入`
- **AND** the asset offline status is labeled `仅轨迹可用`

### Requirement: Saved imported routes remain private and unverified
Saved imported route assets MUST preserve the imported route privacy and trust boundary.

#### Scenario: Imported route model uses private track-only defaults
- **WHEN** a parsed GPX/KML result is converted into an imported route
- **THEN** the route visibility is `Private`
- **AND** the route offline status is `TrackOnly`
- **AND** the route confidence is `Unverified`

#### Scenario: Saved asset copy avoids fake readiness
- **WHEN** an imported route asset is shown after save
- **THEN** the estimated duration is shown as `待确认`
- **AND** the difficulty is shown as `未验证`
- **AND** the confidence is shown as `可信度待确认`
- **AND** the risk tags include `导入轨迹`, `未验证`, and `不含地图底图`

### Requirement: Failed imports cannot be saved
The route tab MUST NOT save failed, unsupported, oversized, or missing-geometry imports.

#### Scenario: Failed import preview
- **WHEN** the import preview is in a failed state
- **THEN** `保存到路线` is not shown
- **AND** no imported route asset is created from that failed import

### Requirement: Save action is idempotent in one session
Saving the same parsed import preview more than once SHALL NOT create duplicate imported route cards in the current route asset list.

#### Scenario: User taps save repeatedly
- **WHEN** the same parsed import preview is saved twice in one app session
- **THEN** the route asset list contains only one card for that imported route
- **AND** that imported route card appears before the static sample platform routes

### Requirement: Save scope stays inside route assets
The save flow SHALL stay inside the `路线` tab route asset center and MUST NOT introduce planner, equipment, community, marketplace, or complex pre-trip-check surfaces.

#### Scenario: Saved import UI is rendered
- **WHEN** the user views the route tab after saving an imported route
- **THEN** the visible text does not include `规划`, `装备`, `社区`, `商城`, `出发前检查`, or `完成检查后开始`
