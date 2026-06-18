# capability-profile Specification

## ADDED Requirements

### Requirement: Historical GPX evidence shall produce a production capability profile

The app shall derive route assessment capacity from a single historical GPX capability profile once enough historical activities are available.

#### Scenario: User has insufficient historical GPX evidence

- GIVEN the user has fewer than 3 historical GPX activities
- WHEN the app builds the capability profile
- THEN no historical GPX capability profile is produced
- AND the route assessment uses the conservative questionnaire fallback with LOW confidence

#### Scenario: User has enough historical GPX evidence

- GIVEN the user has at least 3 historical GPX activities
- WHEN the app builds the capability profile
- THEN the profile records activity count, stable distance, stable ascent, average distance, average ascent, pace, effective speed, and confidence
- AND route assessment uses that historical profile instead of questionnaire capacity

#### Scenario: Historical GPX durations are malformed

- GIVEN the historical activities do not contain usable positive duration and distance values
- WHEN the app builds the capability profile
- THEN stable distance and ascent are clamped to finite minimums
- AND pace and effective speed are omitted instead of producing NaN or Infinity
