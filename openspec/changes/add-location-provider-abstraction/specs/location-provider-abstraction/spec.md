## ADDED Requirements

### Requirement: Platform-independent location provider contract
The system SHALL provide a core location provider contract that can start and stop location updates without depending on Compose, Android services, storage, network, or map rendering.

#### Scenario: Consumer starts location updates
- **WHEN** a consumer starts the provider with a location request and observer
- **THEN** the provider returns a subscription that can stop future updates

#### Scenario: Provider reports availability state
- **WHEN** a provider cannot deliver updates because GPS is disabled or permission is missing
- **THEN** it reports a typed location provider status instead of throwing to the navigation engine

### Requirement: System readings map into navigation samples
The system SHALL convert platform location readings into `LocationSample` values used by the navigation engine.

#### Scenario: Valid reading maps to sample
- **WHEN** a system location reading contains latitude, longitude, accuracy, time, optional altitude, bearing, and speed
- **THEN** the mapper returns a `LocationSample` with WGS84 coordinate, GPS accuracy, timestamp, optional elevation, bearing, and speed

#### Scenario: Invalid reading is rejected
- **WHEN** a system location reading contains an invalid coordinate or negative accuracy
- **THEN** the mapper rejects it before it can feed navigation progress or off-route detection

### Requirement: Android LocationManager implementation
The system SHALL include an Android `LocationManager` implementation behind the provider contract.

#### Scenario: Android provider subscribes to updates
- **WHEN** Android location permission is already granted and GPS/network providers are available
- **THEN** the provider subscribes to system location updates and forwards mapped samples to the observer

#### Scenario: Android provider handles missing permission
- **WHEN** Android throws a location `SecurityException`
- **THEN** the provider reports `PermissionDenied` and returns a stopped subscription

### Requirement: Foreground location permissions declared
The Android manifest SHALL declare foreground fine and coarse location permissions needed for real GPS navigation.

#### Scenario: Manifest declares foreground location capability
- **WHEN** the app is built
- **THEN** the manifest contains `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION` without declaring background location in this slice
