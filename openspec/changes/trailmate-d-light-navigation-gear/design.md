# Design: TrailMate D Light Navigation And Gear

## Overview

The design adds preparation intelligence around the existing route assessment loop. It does not replace the deterministic algorithm model in `TRAILMATE_CODEX_SPEC.md`.

The route experience becomes:

1. Assessment
2. Route
3. Plan
4. Gear

## Baseline Profile

The baseline profile is collected after auth and before the first GPX import prompt. It is skippable.

Fields:

- exercise frequency
- typical exercise duration
- recent longest hike distance and duration
- outdoor experience level
- ascent experience range
- height in cm, optional
- weight in kg, optional
- common pack weight in kg, optional

The baseline profile creates conservative defaults for missing denominators in profile and assessment logic. It does not replace GPX evidence.

## Historical GPX Capability Profile

Historical GPX activities become the production capability profile once at least three activities are available.

The derived profile stores:

- activity count
- stable distance
- stable ascent
- average distance
- average ascent
- average pace when durations are usable
- effective speed when durations and effective distance are usable
- confidence level

Route assessment and the Home capability summary read from this single derived profile. When fewer than three historical activities exist, the app uses the questionnaire fallback with LOW confidence. Malformed historical durations must not produce NaN, Infinity, or zero-duration ETA output.

## Auth And Profile Sync

Production auth is a boundary around account identity, not a source of route scoring.

After real authentication succeeds, account-bound data includes:

- baseline profile
- historical GPX capability evidence
- imported target routes
- gear checklist artifacts
- data export and delete state

Local profile intake may create a draft before server sync. The app can use that draft for prototype flow, but production sync must associate it with the authenticated user id and track pending, synced, or failed state before treating it as account data.

When a returning user signs in, the local session should restore the server profile and capability summary unless the device has newer unsynced edits. If both local and remote profile revisions changed, the app preserves both versions and asks the user to resolve the conflict.

When a different user signs in on the same device, account-bound local data from the previous user must not be displayed. The new session starts from the new user's synced data or a fresh profile intake state.

## Light Navigation

Light navigation means route-following context, not full navigation.

The Route tab shows:

- simplified route line
- elevation profile
- highlighted risk segments
- next checkpoint summary
- saved offline plan access

Foreground current-location display can be added if permission is granted and the route screen is open. Location-backed hike sessions may use accurate foreground fixes to advance checkpoints, ignore low-accuracy fixes, and prompt a route check when the fix is far from the planned line. This remains light route following: background GPS, turn-by-turn navigation, guaranteed deviation detection, voice guidance, and rescue workflows remain later-stage work.

## GPX Import Queue

Production GPX imports use a persisted job queue shared by target-route import and historical-activity import.

Each import job stores:

- id
- kind: target route or historical activity
- source URI
- file name
- status: queued, running, waiting retry, succeeded, or failed
- attempt count and retry budget
- next retry time
- last error
- created and updated timestamps

The import worker is serial: it only marks one queued job, or waiting-retry job whose next retry time has arrived, as running when no other job is running. Parsing is bound to the exact job that was marked running, so a queued user selection cannot accidentally complete a different older retry job. If the app restores a queue that still contains an old running job after process death, startup recovery converts that job back to waiting retry when budget remains, or failed when the budget is exhausted, so later imports cannot be blocked forever. Failed imports must not discard the last valid route or historical capability evidence. Successful imports save parsed route or activity records through their own data boundary and clear retry metadata.

## Gear Catalog Matching

Gear preparation uses a server-owned brand catalog. The mobile app does not create, edit, delete, or save personal gear inventory records.

Catalog item:

- id
- category
- brand
- model
- displayName
- weightGrams
- tags
- imageUrl
- imageAttribution
- active
- createdAt
- updatedAt

Brand, model, and thumbnail are part of the server-managed catalog so Android can show realistic candidates without asking the hiker to maintain their own inventory.

## Gear Advisor

Gear advisor generation runs after a route assessment exists.

Structured AI input:

- route metrics
- terrain tags
- estimated duration range
- checkpoint list
- risk factor summaries
- match and confidence levels
- matched server catalog candidates

AI output:

- checklist item category
- status: covered, check, missing, optional
- matchedCatalogItemId when a catalog item is relevant
- route-based rationale
- suggested attributes
- confidence note

The backend must validate and normalize AI output before storing or returning it. Invalid AI output is discarded and the client falls back to deterministic essentials.

Android connects to the backend through a pure service boundary. The boundary sends the structured `AiGearAdvisorRequest`, converts timeout, unavailable, thrown, stale, and invalid responses into explicit backend statuses, and always resolves display recommendations through the server catalog refresh contract before the Gear tab can present them. The concrete HTTP transport, backend URL, auth token handling, and persistence of generated checklist artifacts remain integration work outside this Android core boundary.

## Safety And Privacy

Safety:

- AI must not say a route is safe, guaranteed, or medically appropriate.
- AI must not prescribe exact water, food, medicine, or emergency actions.
- Gear advice uses "check", "consider", and "prepare" language.

Privacy:

- Gear and body metrics are private.
- Prompts should use the minimum fields required.
- Do not log AI prompts containing personal profile or exact route detail.
- Cloud profile/export includes baseline profile, related gear checklist artifacts, and an audit record. It does not include server-owned gear catalog data, imported target routes, historical GPX activities, or persisted GPX import queue jobs; those require separate route/GPX export scopes.
- Cloud profile/delete removes account profile, related checklist artifacts, local profile/checklist caches, and an audit tombstone. It does not delete server-owned gear catalog data, route library records, historical GPX evidence, or persisted GPX import queue jobs in the same operation.
- Pending sync or conflict state may allow a stale-labeled export snapshot, but must block cloud profile/gear-advisor deletion until sync or conflict resolution completes.

## Failure Modes

- AI unavailable: show deterministic essentials and retry.
- No catalog match exists: show missing route need, suggested attributes, and retry/search actions without an add-owned-gear flow.
- Route changed after checklist generated: mark checklist stale.
- Catalog item is retired: previously generated checklist keeps category rationale but removes the stale catalog item link.
