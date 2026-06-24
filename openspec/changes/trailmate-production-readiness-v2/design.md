# Design: TrailMate Production Readiness V2

## Direction

Use direction A:行前准备闭环 + 现场驾驶舱 + 服务端品牌装备库.

TrailMate should feel like a Chinese Android hiking app whose core loop is:

login -> baseline profile -> import GPX -> assess route -> prepare gear -> navigate and record -> review.

## Mobile UX Contracts

### Login And Onboarding

The login screen prioritizes WeChat and keeps phone login as a secondary route. The screen states the private-account value and safety boundary without explaining every product feature.

After login, baseline profile collection captures only the inputs required for route assessment, plan advice, and gear advice. These inputs remain behind the scenes.

### Home

Home is the route-preparation entry point. It shows:

- TrailMate identity, weather, and location;
- "准备走哪条线？";
- import GPX;
- current route assessment card;
- three quick actions at most.

Home does not show fake daily health data when no real sensor or Health Connect source exists.

### Route Workspace

The bottom route tab manages target routes and import state. It does not show field navigation controls or full diagnostics.

### Route Detail

Route detail owns four preparation tabs:

- Assessment: suitability and next action.
- Route: normal route preview and checkpoints.
- Plan: supply, rest, and risk timeline.
- Gear: route-specific gear checklist.

Full-screen navigation is launched from route detail but is not treated as another dense card inside the route tab.

### Full-Screen Navigation

Full-screen navigation is a field mode. It keeps only:

- map;
- current location;
- route polyline;
- checkpoints;
- recorded track;
- GPS, recording, base map, and gear status;
- primary recording action;
- safety share.

Diagnostics and setup explanations are secondary disclosures outside the first viewport.

## Server Gear Catalog

Add a server-owned gear catalog with stable item IDs. The Android app searches this catalog and uses catalog items directly as route gear matches; the primary mobile path does not create or maintain a personal gear inventory.

Catalog item minimum fields:

- `catalogItemId`
- `category`
- `brand`
- `model`
- `displayName`
- `weightGrams`
- `tags`
- `imageUrl`
- `imageAttribution`
- `source`

Catalog items are read-only in the Android route-preparation UI. Brand, model,
weight, tags, image URL, and attribution remain owned by the server catalog.
Missing brands or models must be added to the catalog before they can appear as
route candidates.

## Android Data Flow

1. Gear advice returns categories, reasons, and optional matching hints.
2. Android displays route checklist.
3. Android matches each route checklist category against the server catalog.
4. The checklist row shows the matched brand, model, and thumbnail when present.
5. The row action opens read-only catalog candidates for that category.
6. The checklist recomputes matched and missing states from catalog availability, not personal inventory mutation.

## Testing And Verification

Automated checks:

- Server unit tests for catalog search and hosted thumbnail references.
- Android unit tests for catalog search state and gear matching.
- Compose smoke tests for the route gear catalog matching path.
- Debug build.

Manual true-device checks:

- Login screen readable.
- GPX route still imports.
- Full-screen navigation remains field-focused.
- Gear checklist shows server catalog matches and thumbnails on SM_S9260.

Deployed-server checks:

- `ssh remote-linux` can reach the deployed TrailMate service.
- Catalog API returns seeded data.
- Android can call the deployed server base URL.
