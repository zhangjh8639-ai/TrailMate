# Change: Safety watch share availability

## Why

Progress, daylight-return, and return-ETA safety watches can ask the hiker to share their current location. The route page already rechecks safety-share text at click time, but those panels still presented the original share label even when current location could not produce share text. In the field, the button must say whether TrailMate will share now or first repair location.

## What Changes

- Add explicit action kinds to the field safety-watch panel button policy.
- Add explicit action kinds to the return-ETA watch panel button policy.
- Use the shared safety-share shortcut availability to label safety-watch buttons.
- Dispatch safety-watch panel buttons by action kind instead of implicit callback assumptions.

## Non-Goals

- Changing progress, daylight, or return-ETA risk thresholds.
- Changing generated safety-share text.
- Redesigning safety-watch panel layouts.
