## 1. Android PMTiles storage preflight

- [x] Add tests for known-size storage rejection before downloader invocation.
- [x] Add tests proving partial `.download` bytes reduce the remaining storage requirement.
- [x] Implement Android coordinator preflight without changing server schema or downloader API.

## 2. Validation

- [x] Run targeted Android coordinator unit tests.
- [x] Run full Android/server unit tests.
- [x] Run OpenSpec strict validation.
- [x] Run git diff whitespace validation.
