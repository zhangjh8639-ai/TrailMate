# gear-advisor Specification

## ADDED Requirements

### Requirement: Users shall be able to manage a private gear inventory

The app shall allow users to add, edit, delete, and mark personal gear items as available or unavailable.

#### Scenario: User adds branded gear

- GIVEN a user opens the My Gear tab
- WHEN the user enters category, brand, model, and optional weight or tags
- THEN the app stores the gear item as private user data
- AND future gear recommendations can match the item by category and availability

#### Scenario: User deletes gear

- GIVEN a user has saved gear items
- WHEN the user deletes one item
- THEN the item is no longer used for future gear recommendations
- AND exported user data no longer includes that deleted item

### Requirement: Gear advisor shall generate route preparation checklist after route assessment

After a target route assessment exists, the Gear tab shall provide an equipment checklist based on route facts, plan context, terrain tags, risk factors, and the user's gear inventory.

#### Scenario: User opens Gear tab for assessed route

- GIVEN a target route has a saved assessment
- WHEN the user opens the Gear tab
- THEN the app can request an AI-generated gear checklist using only structured route, plan, risk, and gear data
- AND the checklist labels items as covered, check, missing, or optional
- AND each item includes a route-based rationale
- AND current gear inventory availability is applied before the checklist is displayed

### Requirement: AI gear advisor shall not affect deterministic route assessment

AI-generated gear output shall not modify match score, match level, confidence level, estimated duration, risk severity, risk segment geometry, or plan checkpoints.

#### Scenario: AI returns gear checklist

- GIVEN the deterministic route assessment has already been saved
- WHEN the AI gear advisor returns checklist output
- THEN the saved route assessment values remain unchanged
- AND the gear checklist is stored as a separate preparation artifact

### Requirement: Gear advisor shall degrade gracefully when AI is unavailable

If AI generation fails, the app shall keep assessment and plan workflows usable.

#### Scenario: AI service fails

- GIVEN a user opens the Gear tab for an assessed route
- WHEN AI generation fails or times out
- THEN the app displays a deterministic essentials checklist
- AND the app offers retry
- AND the app does not block viewing assessment, route, or plan tabs

#### Scenario: AI response belongs to a stale route assessment

- GIVEN a user has opened the Gear tab for the current assessed route
- WHEN an AI checklist response references a different route assessment fingerprint
- THEN the app labels the AI advisor state as stale
- AND the app displays the deterministic essentials checklist for the current route
- AND stale AI checklist categories are not displayed as current recommendations

### Requirement: Gear advice shall avoid medical and shopping claims

Gear advice shall use preparation language and avoid medical, safety guarantee, affiliate, or purchase-command language.

#### Scenario: Gear checklist includes missing items

- GIVEN the AI advisor marks an item as missing
- WHEN the user views the checklist
- THEN the app explains why the category may help for this route
- AND the app may let the user add owned gear with brand and model
- AND the app does not require buying a specific brand or claim the route becomes safe
