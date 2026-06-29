# Change: Reject malformed field-fix accuracy values

## Why

TrailMate uses `TrailMateLocationFixReliability.isReliableForFieldUse` as a shared gate for field-critical states such as route readiness, cockpit status, user-location overlay, and track-recording start actions. The gate must not treat malformed accuracy values as reliable field evidence.

## What Changes

- Require field-use accuracy to be finite and non-negative before comparing it with the configured threshold.
- Reject invalid max-accuracy thresholds so callers cannot accidentally mark every fix as reliable.
- Preserve existing timestamp freshness behavior and the current accuracy threshold semantics for valid values.

## Non-Goals

- UI redesign.
- New GPS sampling behavior.
- New route-progress or deviation thresholds.
- Any change to safety copy or emergency promises.
