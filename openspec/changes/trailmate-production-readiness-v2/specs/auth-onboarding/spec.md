## ADDED Requirements

### Requirement: Mature Private Account Entry

TrailMate SHALL present login and registration as a private account entry flow, not as a prototype setup panel.

#### Scenario: WeChat is primary when configured

- **GIVEN** the Android app has a WeChat App ID and backend base URL configured
- **WHEN** the user opens the account step
- **THEN** TrailMate presents WeChat login/register as the only primary account action
- **AND** it does not show phone number, SMS code, or SMS-code request controls on that step
- **AND** phone login is reserved for builds where WeChat login is unavailable or not configured

#### Scenario: Phone login is fallback only when WeChat is unavailable

- **GIVEN** the Android app cannot offer WeChat login because the build or runtime configuration is unavailable
- **WHEN** the user opens the account step
- **THEN** TrailMate may show phone login as the fallback account action
- **AND** the screen explains in Chinese that WeChat login is not configured for the current build

#### Scenario: Auth failure is user-readable

- **GIVEN** a login attempt fails because WeChat is missing, backend configuration is absent, network is unavailable, or the user cancels authorization
- **WHEN** TrailMate returns to the account step
- **THEN** the screen shows a concise Chinese error or recovery message
- **AND** it does not expose raw exceptions, stack traces, or internal endpoint names

#### Scenario: WeChat authorization has a recoverable waiting state

- **GIVEN** the user taps WeChat login
- **WHEN** TrailMate opens WeChat authorization and waits for the callback
- **THEN** the primary action shows a waiting state and is not repeatedly tappable
- **AND** if the user returns without an authorization result, TrailMate restores the primary action and shows a retry message
- **AND** account method switching is ignored while authorization is processing or waiting for the callback

### Requirement: Baseline Profile Is Evidence Not Content

TrailMate SHALL collect baseline profile values only as assessment evidence.

#### Scenario: Profile collection explains purpose

- **GIVEN** the user is filling baseline exercise, outdoor, body, and pack-weight inputs
- **WHEN** the user reviews the step
- **THEN** each group explains that it affects route assessment, plan, or gear advice
- **AND** the app does not present the profile as a public score or main content surface

#### Scenario: Profile can be saved as account-backed evidence

- **GIVEN** the user has completed the baseline profile step
- **WHEN** Android saves the profile to `/api/v1/users/me/profile`
- **THEN** the server stores the exercise, outdoor, body, and pack-weight values for the current user
- **AND** future reads return those values without exposing them on the main route or home surfaces
- **AND** Android preserves the local profile if a transient sync failure occurs
