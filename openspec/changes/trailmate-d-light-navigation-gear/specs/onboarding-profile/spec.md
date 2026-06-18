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

### Requirement: Production auth shall bind profile data to the authenticated account

The production app shall treat baseline profile, capability evidence, gear inventory, imported routes, and data controls as account-bound data after login.

#### Scenario: First login has a completed local profile draft

- GIVEN a user has completed local profile intake before server sync
- WHEN the user completes real authentication
- THEN the app associates the local profile draft with the authenticated user id
- AND uploads the draft through the profile sync boundary before using it as synced account data
- AND records the sync state as pending, synced, or failed

#### Scenario: First login has skipped local profile intake

- GIVEN a user skipped local profile intake before server sync
- WHEN the user completes real authentication
- THEN the app records that no baseline profile has been provided
- AND does not upload questionnaire defaults as account profile data
- AND continues to historical GPX import or route import prompts using conservative defaults

#### Scenario: Returning login has a server profile

- GIVEN a user signs in on a device with no newer local profile edits
- WHEN server profile sync returns an existing baseline profile and capability summary
- THEN the app restores those account-bound values into the local session
- AND route assessment uses the synced account profile instead of sample defaults

#### Scenario: User switches accounts on the same device

- GIVEN account-bound local profile or gear data exists for one user
- WHEN a different authenticated user signs in
- THEN the app must not show the previous user's profile, routes, history, or gear inventory
- AND the app starts from the new user's synced profile or a fresh profile intake state

#### Scenario: Account-bound activity, route, gear, checklist, and data-control records are synced

- GIVEN a user is authenticated
- WHEN historical GPX evidence, target routes, gear inventory, gear checklist artifacts, export jobs, or delete jobs are created or synced
- THEN each record is associated with the authenticated user id
- AND local queries for those records are scoped to the active authenticated user
- AND server APIs reject cross-user access to those records

### Requirement: Profile sync shall avoid silent overwrite of newer local or remote edits

Profile sync shall use server revision metadata and local dirty state to decide whether to upload, download, or ask the user to resolve a conflict.

#### Scenario: Offline profile edit waits for connectivity

- GIVEN a signed-in user edits baseline profile fields while offline
- WHEN the app cannot reach the profile sync service
- THEN the app marks the profile sync state as pending
- AND preserves the local dirty profile fields for retry

#### Scenario: Local profile changed while offline

- GIVEN a signed-in user edits baseline profile fields while offline
- WHEN connectivity returns and the server profile revision has not changed
- THEN the app uploads the local edit and marks the profile synced after success

#### Scenario: Profile upload fails

- GIVEN a signed-in user has local profile edits to upload
- WHEN the profile sync service rejects or fails the upload
- THEN the app marks the profile sync state as failed
- AND preserves the local dirty profile fields
- AND allows the user or background sync worker to retry without losing the local edit

#### Scenario: Local and remote profile both changed

- GIVEN a signed-in user has unsynced local profile edits
- AND the server profile revision changed since the last successful sync
- WHEN profile sync runs
- THEN the app preserves both versions
- AND asks the user which version to keep before overwriting account profile data

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
