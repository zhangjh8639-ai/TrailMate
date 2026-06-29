# Change: Route deviation fix reliability guard

## Why

Route-deviation alerts are safety-facing. A stale, future, non-positive, or malformed `HikeLocationFix` must not trigger an off-route alert or clear an active deviation episode as if the user rejoined the route.

## What Changes

- Treat invalid, stale, future, and low-accuracy `HikeLocationFix` values as unreliable inside `RouteDeviationAlertPolicy`.
- Preserve active deviation episodes while waiting for a reliable fix.
- Keep existing off-route thresholds, cooldown timing, and escalation rules unchanged once a fix is reliable.

## Non-Goals

- Changing route geometry projection.
- Changing off-route distance thresholds.
- Changing notification delivery channels.
