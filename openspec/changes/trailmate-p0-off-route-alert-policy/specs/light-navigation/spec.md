# light-navigation Specification Delta

## ADDED Requirements

### Requirement: Light navigation shall issue episode-based off-route alerts

TrailMate SHALL convert reliable off-route route-check states into episode-based field alerts that warn the user without repeating the same warning on every GPS fix.

#### Scenario: Reliable first off-route fix triggers an alert

- GIVEN a hike session is active
- AND GPS route matching reports `CHECK_ROUTE`
- AND the fix has trusted foreground accuracy
- WHEN the off-route alert policy evaluates the fix for an inactive episode
- THEN TrailMate returns an urgent off-route alert decision
- AND the decision asks the user to stop and check the route in Chinese
- AND the decision marks the off-route episode active
- AND the decision requests notification and vibration intent for the first alert

#### Scenario: Same off-route episode is throttled

- GIVEN an off-route episode has already emitted an alert
- AND a later reliable `CHECK_ROUTE` fix remains in the same deviation range
- WHEN the later fix arrives inside the off-route alert cooldown
- THEN TrailMate suppresses repeated notification and vibration
- AND the decision keeps the off-route episode active
- AND the visible copy remains suitable for the existing recovery UI

#### Scenario: Worsening deviation escalates inside cooldown

- GIVEN an off-route episode has already emitted an alert
- AND a later reliable `CHECK_ROUTE` fix is materially farther from the planned route
- WHEN the later fix arrives inside the normal cooldown window
- THEN TrailMate emits an escalated off-route alert decision
- AND the decision requests notification and vibration intent
- AND the decision explains in Chinese that the user appears to be moving farther from the route

#### Scenario: Rejoining the route clears the episode

- GIVEN an off-route episode has emitted an alert
- WHEN a later reliable route check reports `ON_ROUTE`
- THEN TrailMate emits one rejoined-route decision
- AND the decision clears the active off-route episode
- AND later healthy `ON_ROUTE` fixes do not repeat the rejoined decision

#### Scenario: Unreliable fixes do not trigger off-route alerts

- GIVEN GPS route matching reports `CHECK_ROUTE`
- AND the fix is missing or has poor foreground accuracy
- WHEN the off-route alert policy evaluates the fix
- THEN TrailMate does not request notification or vibration
- AND the decision tells the user in Chinese to wait for reliable positioning before trusting route-check alerts

### Requirement: Route GPS panel shall surface off-route alert decisions

TrailMate SHALL show the latest off-route alert decision in the route GPS panel as a compact field alert before longer recovery guidance.

#### Scenario: First off-route alert is visible before recovery details

- GIVEN an active hike has a reliable off-route alert decision
- WHEN the route GPS panel renders
- THEN it shows a compact alert with the decision title, caption, and primary action label
- AND it places the compact alert before the detailed off-route recovery panel
- AND it does not claim rerouting, rescue, or guaranteed safety

#### Scenario: Silent same-episode decisions keep guidance visible without repeated interruption

- GIVEN the off-route alert policy suppresses repeated notification and vibration for the same episode
- WHEN the route GPS panel renders the silent decision
- THEN it shows a lower-priority recovery status in Chinese
- AND it does not label the state as a new urgent alert

#### Scenario: Rejoined decision can be acknowledged

- GIVEN the off-route alert policy emits a rejoined-route decision
- WHEN the route GPS panel renders the alert
- THEN the primary action lets the user continue navigation
- AND acknowledging the decision clears the existing rejoined-route confirmation path
