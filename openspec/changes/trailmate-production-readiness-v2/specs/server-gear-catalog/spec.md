## ADDED Requirements

### Requirement: Server-Owned Gear Catalog

TrailMate SHALL expose a server-owned gear catalog for Android gear selection.

#### Scenario: Android lists gear categories

- **GIVEN** the Android app needs a user to fill a missing route gear category
- **WHEN** it requests `GET /api/v1/gear/catalog/categories`
- **THEN** the server returns stable gear categories suitable for route checklist matching

#### Scenario: Android searches catalog items

- **GIVEN** the user is choosing gear for a missing category
- **WHEN** Android requests `GET /api/v1/gear/catalog/search` with a category and optional query
- **THEN** the server returns matching catalog items with category, brand, model, display name, weight, tags, image URL, and stable catalog item ID
- **AND** Android can display those items without requiring free-form brand or model input
- **AND** Android can use the image URL as the item's route checklist thumbnail when the URL is available

#### Scenario: Core route checklist categories have selectable brand gear

- **GIVEN** a route checklist asks the user to prepare rain shell, headlamp, trekking poles, insulation, water, hiking shoes, first aid, power bank, navigation device, and pack categories
- **WHEN** Android searches the server catalog for each matching Chinese checklist category
- **THEN** every category returns at least one active catalog item
- **AND** every returned catalog item has a non-empty brand and model
- **AND** Android can show those catalog items as route matches without asking the user to maintain a personal gear inventory

#### Scenario: Catalog stores image hosting references

- **GIVEN** TrailMate maintains a brand gear catalog
- **WHEN** a catalog item has product imagery
- **THEN** the database stores the image URL and optional attribution
- **AND** the product image binary is hosted in object storage, CDN, image hosting, or a server static asset outside PostgreSQL
- **AND** Android resolves relative hosted image paths against the configured TrailMate server base URL before loading the thumbnail

#### Scenario: Seed catalog thumbnail paths are loadable

- **GIVEN** TrailMate ships seed catalog items for core route checklist categories
- **WHEN** a seed item exposes an image URL under `/gear-thumbnails/`
- **THEN** the server package includes a matching PNG static asset
- **AND** Android can use the resolved URL as a visible route checklist or catalog candidate thumbnail

### Requirement: Route Gear Matching Uses Catalog Items Directly

TrailMate SHALL match route gear recommendations directly against server catalog items on Android.

#### Scenario: Route checklist displays server catalog matches

- **GIVEN** the Android route checklist contains a required gear category
- **WHEN** the server catalog has a matching category or category alias
- **THEN** Android shows the matched brand/model and thumbnail in the route checklist row
- **AND** the row action opens server catalog matches for that category
- **AND** the route checklist does not require a "my gear" tab, save-to-inventory form, availability toggle, or remove action

#### Scenario: Catalog item details are read-only for route preparation

- **GIVEN** the user opens a matched catalog item
- **WHEN** Android shows the item detail
- **THEN** the screen displays catalog-derived category, brand, model, weight, tags, image URL state, and image attribution
- **AND** it does not create or mutate a personal inventory item

### Requirement: Gear Advice Does Not Create Inventory

TrailMate SHALL keep AI gear advice separate from user inventory mutation.

#### Scenario: Gear advice recommends categories only

- **GIVEN** the server returns gear advice for a plan
- **WHEN** the advice contains required or suggested equipment
- **THEN** it returns categories, status, rationale, and optional matching hints
- **AND** it does not create user inventory items or invent owned brand/model data
