# Change: Guard progress safety watch against unreliable fixes

## Why

Progress safety watch compares planned checkpoint timing with current route progress. If that progress comes from a stale, future-dated, malformed, or low-accuracy GPS fix, TrailMate may incorrectly warn that the hiker is behind plan. In the field, false safety guidance is itself risky.

## What Changes

- Require progress safety watch to use only reliable route-progress fixes.
- Treat stale, future, low-accuracy, negative, or non-finite fix values as missing progress evidence.
- Keep the existing caution and alert thresholds unchanged when a fresh reliable fix is available.

## Non-Goals

- New UI layout.
- New route-plan timing rules.
- Medical fatigue diagnosis.
- Automatic emergency contact or rescue behavior.
