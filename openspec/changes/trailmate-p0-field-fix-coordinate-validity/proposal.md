# Change: Reject malformed field-fix coordinates

## Why

TrailMate uses shared field-location reliability to decide whether route readiness, cockpit state, map location, and recording gates can trust the current GPS fix. A fix with missing or non-finite latitude/longitude cannot represent a real field position, even if its status, accuracy, and timestamp look usable.

## What Changes

- Require latitude and longitude to be present and finite before a snapshot can be reliable for field use.
- Preserve the existing status, timestamp freshness, and accuracy validity behavior.
- Keep the change in the shared reliability predicate so every field-critical caller gets the same protection.

## Non-Goals

- New GPS provider behavior.
- UI redesign.
- Route matching or deviation threshold changes.
- Emergency dispatch or rescue promises.
