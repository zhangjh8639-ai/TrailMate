# Change: Route safety-share shortcut action label

## Why

The route cockpit and fullscreen navigation expose a compact safety-share shortcut. The shortcut previously always showed "安全分享", even when current location was missing, stale, or too inaccurate and the click would actually request location. In the field, that mismatch can make hikers believe a safety contact can be notified when TrailMate first needs a fresh GPS fix.

## What Changes

- Add a route safety-share shortcut presentation policy.
- Use a compact "安全分享" label only when share text is available.
- Use the existing safety-share repair label, such as "授权定位" or "重新定位", when the shortcut must request location first.
- Dispatch the shortcut through an explicit action kind.
- Keep the route location presentation clock refreshing while a located or low-accuracy fix can age into a repair state.

## Non-Goals

- Changing the generated safety-share message.
- Adding live tracking or server-side sharing.
- Redesigning the route cockpit layout.
