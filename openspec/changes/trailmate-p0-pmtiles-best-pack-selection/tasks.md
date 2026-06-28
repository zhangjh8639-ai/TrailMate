## 1. Android PMTiles catalog selection

- [x] Add tests for choosing the smallest known-size full-covering PMTiles pack.
- [x] Keep unknown or non-positive `sizeBytes` packs usable but ranked after known positive sizes.
- [x] Update Android selection policy without changing the catalog API schema.

## 2. Validation

- [x] Run targeted Android catalog selection unit tests.
- [x] Run full Android/server unit tests.
- [x] Run OpenSpec strict validation.
- [x] Run git diff whitespace validation.
