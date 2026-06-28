## ADDED Requirements

### Requirement: WeChat Manual Safety Sharing

TrailMate SHALL prefer WeChat for manual safety text sharing when WeChat is available, while preserving a system-share fallback.

#### Scenario: WeChat is configured and installed

- **GIVEN** TrailMate has a configured WeChat AppID
- **AND** WeChat is installed on the device
- **AND** safety share text is non-empty
- **WHEN** the hiker shares departure, current-location, or emergency safety text
- **THEN** TrailMate MUST attempt to send the text to a WeChat chat session first

#### Scenario: WeChat is unavailable

- **GIVEN** TrailMate does not have a configured WeChat AppID or WeChat is not installed
- **WHEN** the hiker shares safety text
- **THEN** TrailMate MUST use the Android system share chooser
- **AND** the share action MUST NOT become disabled only because WeChat is unavailable

#### Scenario: WeChat send request fails

- **GIVEN** WeChat is configured and installed
- **AND** the WeChat SDK does not accept the send request
- **WHEN** the hiker shares safety text
- **THEN** TrailMate MUST fall back to the Android system share chooser

#### Scenario: Safety boundary remains manual

- **GIVEN** TrailMate shares safety text through WeChat or the system chooser
- **WHEN** the hiker uses the share action
- **THEN** TrailMate MUST NOT choose a recipient automatically
- **AND** it MUST NOT imply the message was delivered
- **AND** it MUST NOT start realtime tracking, automatic contact, or rescue dispatch
