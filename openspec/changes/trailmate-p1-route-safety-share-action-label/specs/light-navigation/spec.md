## ADDED Requirements

### Requirement: Route Safety-Share Shortcut Action Label

TrailMate SHALL label route safety-share shortcuts according to the executable action currently available, and SHALL dispatch the shortcut by explicit action kind rather than fixed display copy.

#### Scenario: Fresh share text keeps a compact safety-share label

- **GIVEN** the route page has a fresh, accurate location that can produce safety-share text
- **WHEN** TrailMate presents a route safety-share shortcut
- **THEN** TrailMate MUST show a compact "安全分享" label
- **AND** the shortcut MUST run the safety-share action

#### Scenario: Missing location asks for authorization first

- **GIVEN** the route page cannot produce safety-share text because location is missing
- **WHEN** TrailMate presents a route safety-share shortcut
- **THEN** TrailMate MUST label the shortcut with the safety-share location-request label
- **AND** the shortcut MUST request location instead of attempting to share

#### Scenario: Stale or unreliable location asks for refresh first

- **GIVEN** the route page cannot produce safety-share text because the current fix is stale or unreliable
- **WHEN** TrailMate presents a route safety-share shortcut
- **THEN** TrailMate MUST label the shortcut with a location-refresh label
- **AND** the shortcut MUST request a fresh location

#### Scenario: Located fixes age out while the route page remains open

- **GIVEN** GPS is enabled and the route page has a located or low-accuracy fix
- **WHEN** TrailMate keeps the route page open without a new location callback
- **THEN** TrailMate MUST refresh the location presentation clock
- **AND** stale safety-share shortcuts MUST age into a location-refresh action instead of keeping a share label
