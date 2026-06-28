# Change: Prefer WeChat for manual safety sharing

## Why

TrailMate's safety features produce useful static text for departure briefs, current location, and offline emergency information. On Android in China, WeChat is the expected channel for sending this kind of message to a safety contact. The current implementation only opens the generic Android share sheet.

## What Changes

- Add a WeChat text-share policy and Android OpenSDK launcher.
- Try WeChat first for route safety text sharing when AppID is configured and WeChat is installed.
- Fall back to the existing Android system share chooser when WeChat is not configured, not installed, or send request fails.
- Keep sharing manual: TrailMate never selects a contact, sends automatically, monitors delivery, or creates a realtime tracking link.

## Non-Goals

- SMS integration.
- WeChat contact picker or contact storage.
- WeChat Moments sharing.
- Realtime location tracking.
- Automatic emergency contact or rescue dispatch.
