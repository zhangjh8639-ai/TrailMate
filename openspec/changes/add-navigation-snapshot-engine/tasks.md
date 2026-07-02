## 1. Tests First

- [x] 1.1 Add failing unit test for on-route snapshot progress and route facts.
- [x] 1.2 Add failing unit test that poor GPS accuracy does not trigger off-route guidance.
- [x] 1.3 Add failing unit test for sustained confirmed off-route guidance.
- [x] 1.4 Add failing unit test for reverse-direction progress.
- [x] 1.5 Add failing unit test that guidance text does not expose safe-route commands.

## 2. Core Engine

- [x] 2.1 Add navigation snapshot engine input/output models.
- [x] 2.2 Compose projection, progress, off-route evidence, and session state into `NavigationSnapshot`.
- [x] 2.3 Add eight-way compass direction guidance to nearest route point.
- [x] 2.4 Keep implementation side-effect free and independent from Android services.

## 3. Verification

- [x] 3.1 Run OpenSpec validation.
- [x] 3.2 Run focused navigation snapshot tests.
- [x] 3.3 Run full unit tests and debug build.
- [x] 3.4 Request code review before PR.
