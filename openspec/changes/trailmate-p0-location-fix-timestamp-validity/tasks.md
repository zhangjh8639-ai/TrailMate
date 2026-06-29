## 1. Location fix timestamp validity

- [x] Add failing coverage for future-timestamp location fixes.
- [x] Add failing coverage for non-positive location timestamps.
- [x] Add failing coverage for Android provider timestamp preservation.
- [x] Add failing coverage for invalid-timestamp presentation states.
- [x] Add failing coverage for invalid recorded points, route progress, and breadcrumb guidance.
- [x] Reject invalid timestamps in shared field-use reliability.
- [x] Preserve provider timestamps when converting Android fixes.
- [x] Present invalid-timestamp fixes as calibration warnings, not recent updates.
- [x] Reject invalid recorded points without poisoning later track points.
- [x] Block invalid fix timestamps from route progress updates.
- [x] Present invalid breadcrumb timestamps as alert states.
- [x] Run verification before PR.
- [x] Request read-only review before PR.
