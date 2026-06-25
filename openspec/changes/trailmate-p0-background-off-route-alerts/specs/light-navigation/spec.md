# light-navigation Specification Delta

## ADDED Requirements

### Requirement: Foreground recording shall monitor route deviation while backgrounded

TrailMate SHALL evaluate reliable foreground-service recording fixes against the active imported route geometry and deliver off-route alerts while local track recording is active.

#### Scenario: Recording service alerts on reliable off-route fix

- **GIVEN** a user has imported a target route with route geometry
- **AND** local foreground track recording is active for that route
- **WHEN** the foreground recording service receives a reliable GPS fix farther than the route-deviation threshold
- **THEN** TrailMate SHALL evaluate the same off-route alert policy used by light navigation
- **AND** TrailMate SHALL deliver the resulting Android notification/vibration plan when runtime permissions allow it
- **AND** TrailMate SHALL NOT claim rerouting, rescue, or guaranteed safety

#### Scenario: Recording service confirms rejoin without vibration

- **GIVEN** foreground recording previously detected a reliable off-route episode
- **WHEN** a later reliable foreground-service fix returns near the planned route
- **THEN** TrailMate SHALL emit a rejoined-route decision
- **AND** the Android delivery plan SHALL NOT vibrate

#### Scenario: Missing route geometry does not produce precise deviation alerts

- **GIVEN** foreground track recording is active
- **AND** the local session has no imported route, a different imported route, or fewer than two route points
- **WHEN** the foreground recording service receives a GPS fix
- **THEN** TrailMate SHALL keep recording usable track points
- **AND** TrailMate SHALL NOT post a precise off-route notification from service-side route monitoring

#### Scenario: Stale service fixes do not produce precise deviation alerts

- **GIVEN** foreground track recording is active for a route with geometry
- **WHEN** the foreground recording service receives a GPS fix older than the shared reliable-fix age window
- **THEN** TrailMate SHALL keep recording only if the track-recording engine accepts the point
- **AND** TrailMate SHALL NOT post a precise off-route or rejoined-route notification from that stale fix

#### Scenario: Same-name route replacement does not reuse the wrong geometry

- **GIVEN** foreground track recording is active for a route key
- **AND** the local imported route is replaced by another route with the same display name and summary metadata but different coordinates
- **WHEN** the foreground recording service evaluates a GPS fix
- **THEN** TrailMate SHALL NOT evaluate the fix against the replacement route geometry
- **AND** TrailMate SHALL either keep using the route geometry captured for the active recording or suppress precise service-side route-deviation alerts

#### Scenario: Route screen avoids duplicate interruptive delivery during active recording

- **GIVEN** local foreground track recording is active
- **AND** the visible route screen also computes route-deviation state for display
- **WHEN** a route-deviation decision is produced on the route screen
- **THEN** the route screen SHALL keep the in-app alert and recovery UI available
- **AND** the foreground recording service SHALL own notification/vibration delivery to avoid duplicate alerts
