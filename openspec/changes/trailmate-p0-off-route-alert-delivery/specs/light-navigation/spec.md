# light-navigation Specification Delta

## ADDED Requirements

### Requirement: Route off-route alerts shall deliver notification and vibration intents

TrailMate SHALL convert new off-route alert decisions into Android notification and vibration delivery plans without replaying those effects during Compose recomposition.

#### Scenario: Urgent off-route decision posts and vibrates when allowed

- GIVEN the off-route policy emits a reliable urgent off-route decision
- AND Android notification permission is granted
- AND the device can vibrate
- WHEN TrailMate resolves route alert delivery
- THEN it posts a Chinese notification telling the user to stop and check the route
- AND it requests vibration
- AND it does not claim rerouting, rescue, or guaranteed safety

#### Scenario: Same-episode silent decision does not interrupt again

- GIVEN the off-route policy suppresses repeated notification and vibration for the same episode
- WHEN TrailMate resolves route alert delivery
- THEN it does not post a notification
- AND it does not vibrate
- AND the in-app recovery guidance remains responsible for visible status

#### Scenario: Missing notification permission degrades to in-app alert and vibration

- GIVEN the off-route policy emits an urgent off-route decision
- AND Android notification permission is not granted
- AND the device can vibrate
- WHEN TrailMate resolves route alert delivery
- THEN it does not attempt to post a notification
- AND it still requests vibration for the foreground route alert
- AND it returns a Chinese fallback reason explaining that notification permission is missing

#### Scenario: Rejoined confirmation can notify without vibration

- GIVEN the off-route policy emits a rejoined-route confirmation
- AND Android notification permission is granted
- WHEN TrailMate resolves route alert delivery
- THEN it may post a Chinese route-rejoined notification
- AND it does not request vibration

#### Scenario: Rendering the route GPS panel does not replay delivery

- GIVEN an off-route alert decision is already stored for display
- WHEN the route GPS panel recomposes
- THEN TrailMate renders the alert banner from state
- AND it does not post another notification or vibrate from the render path
