# Change: Add daylight return watch

## Why

TrailMate's route tab should help hikers notice when daylight is becoming a constraint. A route that is safe at noon can become risky near sunset, especially with low signal, low battery, slower pace, or uncertain exits.

## What Changes

- Add a local daylight return watch that estimates the route day's sunset window from route coordinates.
- Compare the active expected finish time with sunset and civil dusk.
- Surface caution or alert guidance on the route tab when the daylight window is tight.
- Offer manual safety sharing for alert states.
- Keep copy conservative: TrailMate estimates daylight only and does not guarantee visibility, dispatch rescue, or contact anyone automatically.

## Non-Goals

- Weather-aware visibility prediction.
- Headlamp or gear certification.
- Background alarm scheduling.
- Automatic emergency contact, SMS, or rescue dispatch.
