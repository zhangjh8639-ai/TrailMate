# Change: Add departure safety start gate

## Why

TrailMate's route cockpit can currently keep "开始徒步并记录轨迹" as the primary action even when `DepartureReadinessEngine` reports a required offline base map action. That weakens the product's core safety promise: hikers should not be nudged into starting before required offline navigation evidence is ready.

## What Changes

- Make the route cockpit primary action honor required departure readiness repair actions before starting a hike.
- Keep optional/recommended offline base map gaps visible as readiness items without blocking recommended routes.
- Preserve active recording controls: pause, resume, and in-progress recording actions stay primary once the hike or recording is already active.
- Add regression coverage for required offline base map, target-region, tile verification, missing gear, optional base map, and active recording cases.

## Non-Goals

- Changing `DepartureReadinessEngine` scoring.
- Changing PMTiles or AMap offline map download/import behavior.
- Blocking already-active hikes from pausing, resuming, or finishing recording.
- Adding new pages, dialogs, or route detail UI sections.
