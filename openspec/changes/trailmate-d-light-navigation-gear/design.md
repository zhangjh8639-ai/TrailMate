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

## Auth And Profile Sync

Production auth is a boundary around account identity, not a source of route scoring.

After real authentication succeeds, account-bound data includes:

- baseline profile
- historical GPX capability evidence
- personal gear inventory
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

Foreground current-location display can be added if permission is granted and the route screen is open. Background GPS, turn-by-turn navigation, and deviation alerts remain later-stage work.

## Gear Inventory

Personal gear inventory is a private user profile feature.

Gear item:

- id
- userId
- category
- brand
- model
- weightGrams
- conditionTags
- seasonTags
- ownershipStatus
- notes
- available
- createdAt
- updatedAt

Brand and model are optional because the useful product behavior is category coverage, not shopping.

## Gear Advisor

Gear advisor generation runs after a route assessment exists.

Structured AI input:

- route metrics
- terrain tags
- estimated duration range
- checkpoint list
- risk factor summaries
- match and confidence levels
- saved gear categories and metadata

AI output:

- checklist item category
- status: covered, check, missing, optional
- matchedGearItemId when covered
- route-based rationale
- suggested attributes
- confidence note

The backend must validate and normalize AI output before storing or returning it. Invalid AI output is discarded and the client falls back to deterministic essentials.

## Safety And Privacy

Safety:

- AI must not say a route is safe, guaranteed, or medically appropriate.
- AI must not prescribe exact water, food, medicine, or emergency actions.
- Gear advice uses "check", "consider", and "prepare" language.

Privacy:

- Gear and body metrics are private.
- Prompts should use the minimum fields required.
- Do not log AI prompts containing personal profile or exact route detail.
- Export and delete workflows must include baseline profile, gear inventory, and gear checklist records.

## Failure Modes

- AI unavailable: show deterministic essentials and retry.
- User has no gear saved: show missing categories and "add owned gear" action.
- Route changed after checklist generated: mark checklist stale.
- User deletes a gear item: previously generated checklist keeps category rationale but removes the matched item link.
