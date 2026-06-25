# Off-Route Alert Delivery Design

## Product Behavior

When the off-route policy emits an urgent or escalated decision, TrailMate should deliver a short Android notification when notification permission is available and vibrate when the device supports vibration. If notification permission is missing, the app still keeps the in-app alert banner visible and may vibrate for the foreground Route tab event. Silent same-episode decisions and low-accuracy waiting states must not create notification or vibration noise.

Rejoined-route decisions may post a lower-priority confirmation notification when notification permission is available, but they do not vibrate. The in-app "continue navigation" confirmation remains the primary acknowledgement path.

## Technical Approach

Add `RouteDeviationAlertDeliveryEngine` as a pure Kotlin model in `android-app/src/main/java/com/trailmate/app/core/model/`. It accepts:

- the latest `RouteDeviationAlertDecision`
- whether Android notification permission is granted
- whether the current device can vibrate

It returns a `RouteDeviationAlertDeliveryPlan` with:

- whether to post a notification
- whether to vibrate
- localized notification title/body
- an in-app-only fallback reason when notification permission blocks posting

Add a small Android adapter in `android-app/src/main/java/com/trailmate/app/core/location/` that consumes the delivery plan. The adapter owns notification channel creation, notification posting, and vibration API differences. Urgent and escalated off-route decisions use a high-importance route-alert channel; rejoined-route confirmations use a separate default-importance route-status channel. `RouteDetailScreen` calls this adapter only from the GPS snapshot handling path immediately after a new policy decision is evaluated. Compose rendering continues to derive UI from state only and does not deliver notifications or vibration.

## Safety Boundaries

This change does not create background off-route detection. The foreground service currently records track points but does not receive target route geometry, so lock-screen/background off-route alerts remain a later route-service integration. Delivery text must avoid claims of rerouting, rescue, or guaranteed safety.

## Android Notes

- `POST_NOTIFICATIONS` remains the runtime gate for notification posting on Android 13+.
- `VIBRATE` is a normal manifest permission and is required before using vibrator APIs.
- The route-alert notification channel is separate from the low-importance track-recording foreground-service channel so safety alerts can be more prominent.
- The route-status notification channel is separate from urgent route alerts so rejoined confirmations can stay quieter.
