# gear-advisor Specification

## ADDED Requirements

### Requirement: Gear catalog shall be server-owned and read-only on mobile

The app SHALL source brand equipment options from a server-managed gear catalog and SHALL NOT expose personal gear inventory creation, editing, deletion, or "save to my gear" flows in the mobile Gear tab.

#### Scenario: User opens server catalog matches

- GIVEN a target route has route gear recommendations
- WHEN the user opens the Gear tab
- THEN the app shows route needs, brand candidates, and read-only equipment details
- AND brand candidates come from the server-managed catalog with category, brand, model, tags, and thumbnail metadata
- AND the app does not show "My Gear", add-owned-gear, inventory edit, inventory delete, or save-to-my-gear actions

#### Scenario: Catalog has no matching item

- GIVEN the server-managed gear catalog has no item for a route need
- WHEN the user views brand candidates
- THEN the app explains that the model is not yet in the backend catalog
- AND the app offers a clear path back to route needs so the user can review another category
- AND the mobile app does not offer custom gear creation as a fallback

#### Scenario: Server catalog sync fails on mobile

- GIVEN the mobile app cannot load the server-managed gear catalog
- WHEN the user opens the Gear tab
- THEN the app labels the catalog source as a local cache fallback
- AND the app offers retry for syncing the server-managed catalog
- AND the app continues to show route needs and read-only brand candidate matches from cached catalog data
- AND the app does not expose personal gear creation, add-owned-gear, or save-to-my-gear actions as a fallback

### Requirement: Gear advisor shall generate route preparation checklist after route assessment

After a target route assessment exists, the Gear tab SHALL provide an equipment checklist based on route facts, plan context, terrain tags, risk factors, and the server-managed gear catalog.

#### Scenario: User opens Gear tab for assessed route

- GIVEN a target route has a saved assessment
- WHEN the user opens the Gear tab
- THEN the app can request an AI-generated gear checklist using only structured route, plan, risk, catalog, and checklist data
- AND the checklist labels items as covered, check, missing, or optional
- AND each item includes a route-based rationale
- AND server catalog candidate matches are applied before the checklist is displayed

### Requirement: AI gear advisor shall not affect deterministic route assessment

AI-generated gear output SHALL NOT modify match score, match level, confidence level, estimated duration, risk severity, risk segment geometry, or plan checkpoints.

#### Scenario: AI returns gear checklist

- GIVEN the deterministic route assessment has already been saved
- WHEN the AI gear advisor returns checklist output
- THEN the saved route assessment values remain unchanged
- AND the gear checklist is stored as a separate preparation artifact

### Requirement: Gear advisor shall degrade gracefully when AI is unavailable

If AI generation fails, the app SHALL keep assessment and plan workflows usable.

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

Gear advice SHALL use preparation language and avoid medical, safety guarantee, affiliate, or purchase-command language.

#### Scenario: Gear checklist includes missing items

- GIVEN the AI advisor marks an item as missing
- WHEN the user views the checklist
- THEN the app explains why the category may help for this route
- AND the app may show read-only server catalog candidates for that category
- AND the app does not require buying a specific brand or claim the route becomes safe

### Requirement: AI gear advisor backend service SHALL be connected behind a validation boundary

The app SHALL call the production AI gear advisor through a backend service boundary that validates response fingerprints and checklist fields before any recommendation is displayed.

#### Scenario: Backend returns validated checklist

- GIVEN a target route has a deterministic assessment and fallback checklist
- WHEN the backend returns a checklist with the current assessment fingerprint
- THEN the app validates the response through the gear advisor contract
- AND refreshes covered or missing status against server catalog candidate matches
- AND displays the checklist as AI-ready without changing route assessment values

#### Scenario: Backend fails or times out

- GIVEN a user opens the Gear tab for an assessed route
- WHEN the backend is unavailable, times out, or throws an error
- THEN the app displays the deterministic fallback checklist
- AND labels retry as available
- AND does not treat the fallback as a successful AI response

#### Scenario: Backend returns stale checklist

- GIVEN the user has changed or re-imported the target route
- WHEN a backend response references a previous assessment fingerprint
- THEN the app labels the response as stale
- AND displays the deterministic fallback checklist for the current route
- AND stale checklist categories are not shown as current recommendations
