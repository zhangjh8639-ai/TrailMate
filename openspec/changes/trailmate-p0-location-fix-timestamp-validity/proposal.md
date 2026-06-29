# Change: Location fix timestamp validity

## Why

TrailMate uses a shared location reliability policy to decide whether a GPS fix can unlock departure, track recording, map confidence, and field guidance. A future or non-positive location timestamp can come from device clock drift, provider anomalies, or replayed samples. Such fixes must not be treated as current field evidence.

## What Changes

- Require location fix timestamps to be positive and not later than the current evaluation time.
- Make invalid timestamps age outside the reliable fix window.
- Keep existing accuracy and status checks unchanged.

## Non-Goals

- Changing the 60-second reliable-fix window.
- Changing accuracy thresholds.
- Changing Android location provider collection.
