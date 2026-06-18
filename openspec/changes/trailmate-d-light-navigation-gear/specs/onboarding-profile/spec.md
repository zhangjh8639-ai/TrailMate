# onboarding-profile Specification

## ADDED Requirements

### Requirement: Users shall be able to provide a skippable baseline profile after authentication

After registration or first login, the app shall offer a short baseline profile questionnaire before prompting for historical GPX import.

#### Scenario: User completes baseline profile

- GIVEN a newly authenticated user
- WHEN the user enters exercise frequency, outdoor experience, ascent experience, and optional body/load fields
- THEN the app stores a baseline profile for that user
- AND the app labels the resulting capability estimate as low confidence until enough GPX evidence exists

#### Scenario: User skips baseline profile

- GIVEN a newly authenticated user
- WHEN the user chooses to skip the questionnaire
- THEN the app continues to the GPX import prompt
- AND route assessment uses existing experience-level defaults until better evidence exists

### Requirement: Baseline profile shall not override GPX-derived capability evidence

The baseline profile shall provide conservative defaults only when GPX-derived evidence is missing or insufficient.

#### Scenario: User has three usable historical GPX activities

- GIVEN a user has at least three usable historical GPX activities
- WHEN capability profile recalculation runs
- THEN GPX-derived stable distance, stable ascent, stable duration, grade speeds, and fatigue curve take precedence over questionnaire defaults
- AND target route assessment uses GPX-derived stable distance and ascent evidence instead of questionnaire-only capacity

### Requirement: Body and pack fields shall be optional and non-medical

Height, weight, and pack weight shall be optional and used only for conservative load context.

#### Scenario: User enters height and weight

- GIVEN a user enters optional body fields
- WHEN the app displays an assessment or profile explanation
- THEN the app does not display medical advice, body judgment, calorie claims, or health diagnosis
