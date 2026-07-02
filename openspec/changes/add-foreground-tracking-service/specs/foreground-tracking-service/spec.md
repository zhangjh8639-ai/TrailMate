## ADDED Requirements

### Requirement: Foreground tracking service is explicitly controlled
The system SHALL expose an Android foreground service boundary that starts only from an explicit start action and stops from an explicit stop action.

#### Scenario: Start action enters foreground mode
- **WHEN** the tracking service receives the start action
- **THEN** it creates the tracking notification channel, calls foreground mode with a persistent notification, and remains started

#### Scenario: Stop action ends the service
- **WHEN** the tracking service receives the stop action
- **THEN** it stops foreground mode and stops itself without emitting fake track points

### Requirement: Tracking notification is clear and safe
The system SHALL define notification metadata for active tracking without claiming rescue, guaranteed safety, or completed navigation.

#### Scenario: Active notification content
- **WHEN** active tracking notification content is built
- **THEN** it contains TrailMate tracking/navigation copy and does not contain rescue or guaranteed safety claims

### Requirement: Manifest declares foreground service capability
The Android manifest SHALL register the tracking service with `foregroundServiceType="location"` and declare the foreground service permissions required by modern Android.

#### Scenario: Service and permissions declared
- **WHEN** the app is built
- **THEN** the manifest contains the tracking service, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_LOCATION`, and `POST_NOTIFICATIONS`

#### Scenario: Background location remains out of scope
- **WHEN** the app is built for this slice
- **THEN** the manifest does not declare `ACCESS_BACKGROUND_LOCATION`

### Requirement: Service shell does not own persistence or route simulation
The foreground service shell MUST NOT write track records, synthesize GPS points, upload location, or claim a route is being safely followed.

#### Scenario: Service start without full tracking stack
- **WHEN** the service is started in this slice
- **THEN** it only maintains foreground lifecycle and notification state, leaving GPS subscription and persistence to later changes
