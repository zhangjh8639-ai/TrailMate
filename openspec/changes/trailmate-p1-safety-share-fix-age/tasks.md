# Tasks

## 1. Safety share fix age

- [x] Add tests for missing, invalid, stale, and fresh safety-share timestamps.
- [x] Extend safety-share location input with timestamp freshness checks.
- [x] Wire route screen safety-share calls to pass `TrailMateLocationSnapshot.timestampEpochMillis`.
- [x] Re-evaluate safety-share freshness at click time instead of sending precomputed share text.

## 2. Validation

- [x] Run targeted Android model tests.
- [x] Run full Android/server unit tests.
- [x] Run OpenSpec strict validation.
- [x] Run git diff whitespace validation.
