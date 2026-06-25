# TrailMate Server API

Base path: `/api/v1`

All authenticated requests use:

```http
Authorization: Bearer <accessToken>
```

Times use UTC ISO-8601 strings. JSON fields use `lowerCamelCase`.

## Error Shape

```json
{
  "timestamp": "2026-06-22T10:00:00Z",
  "status": 422,
  "code": "GPX_MISSING_TIME",
  "message": "该历史轨迹缺少时间信息，不能用于个人速度画像。",
  "traceId": "01J2...",
  "details": {
    "missingTimeRatio": 1.0
  }
}
```

Android should map this into `TrailMateApiError` and preserve `code`, `message`, and `traceId`.
Auth validation failures return the same shape with `code=AUTH_INVALID_REQUEST`, so Android can show the server message instead of a generic network failure.

## Endpoint Catalog

| Method | Path | Android Operation | Notes |
| --- | --- | --- | --- |
| `POST` | `/auth/register` | `register` | Optional legacy email/password account creation |
| `POST` | `/auth/login` | `login` | Optional legacy email/password login |
| `POST` | `/auth/phone/code` | `requestPhoneCode` | Sends an SMS code for login or registration |
| `POST` | `/auth/phone/login` | `loginWithPhone` | Verifies SMS code, creates account if needed, and returns session |
| `POST` | `/auth/wechat/login` | `loginWithWechat` | Exchanges WeChat auth code for a TrailMate session |
| `POST` | `/auth/refresh` | `refreshSession` | Rotates refresh token and returns a new session |
| `POST` | `/auth/logout` | `logout` | Revokes the provided refresh token |
| `GET` | `/users/me` | `getCurrentUser` | Reads account and profile summary |
| `GET` | `/users/me/profile` | `getOnboardingProfile` | Reads saved baseline exercise/outdoor/body profile |
| `PUT` | `/users/me/profile` | `saveOnboardingProfile` | Saves baseline exercise/outdoor/body profile |
| `POST` | `/imports/gpx` | `startGpxImport` | Multipart upload, `purpose=HISTORY` or `TARGET` |
| `GET` | `/imports/{jobId}` | `getImportJob` | Polls parse/quality/import status |
| `POST` | `/imports/{jobId}/confirm` | `confirmImport` | Converts parsed import into activity or target route |
| `GET` | `/activities` | `listActivities` | History activity summaries |
| `GET` | `/activities/{activityId}` | `getActivity` | Activity detail and derived features |
| `DELETE` | `/activities/{activityId}` | `deleteActivity` | Deletes one history activity and schedules profile recalculation |
| `POST` | `/profiles/recalculate` | `recalculateProfile` | Creates a new capability profile version |
| `GET` | `/profiles/current` | `getCurrentCapabilityProfile` | Latest profile with evidence |
| `GET` | `/routes` | `listRoutes` | Target route summaries |
| `GET` | `/routes/{routeId}` | `getRoute` | Target route detail |
| `POST` | `/routes/{routeId}/assessments` | `createAssessment` | Deterministic route assessment |
| `GET` | `/assessments/{assessmentId}` | `getAssessment` | Reads saved assessment |
| `POST` | `/assessments/{assessmentId}/plans` | `createPlan` | Deterministic hike plan |
| `GET` | `/plans/{planId}` | `getPlan` | Reads saved plan |
| `GET` | `/gear/catalog/categories` | `listGearCatalogCategories` | Server-owned gear categories for route checklist matching |
| `GET` | `/gear/catalog/search` | `searchGearCatalog` | Searches server-owned gear catalog by category and query |
| `GET` | `/offline-basemaps/pmtiles/catalog` | `listPmTilesBasemaps` | Lists server-owned PMTiles pack metadata intersecting route bounds |
| `POST` | `/plans/{planId}/gear-advice` | `requestGearAdvice` | Optional AI-backed checklist text |
| `POST` | `/plans/{planId}/feedback` | `submitCompletionFeedback` | Completion result and fatigue |
| `POST` | `/tracks` | `uploadRecordedTrack` | Uploads completed local recording |
| `GET` | `/exports/me` | `requestAccountExport` | Starts or reads export job |
| `DELETE` | `/users/me` | `deleteAccount` | Deletes account and user-owned data |

## Core DTOs

### Auth Session

```json
{
  "userId": "usr_01J2",
  "provider": "PHONE",
  "accessToken": "eyJ...",
  "refreshToken": "rt_...",
  "expiresAt": "2026-06-22T12:00:00Z",
  "phoneNumber": "+8613800138000",
  "wechatOpenId": null,
  "displayName": null
}
```

Refresh session:

```json
{
  "refreshToken": "rt_..."
}
```

Logout:

```json
{
  "refreshToken": "rt_..."
}
```

Server behavior:

- `/auth/refresh` consumes or rotates the provided refresh token and returns a new `AuthSession`.
- `/auth/logout` revokes the provided refresh token and returns `204 No Content`.
- The current local preview implementation stores refresh-token state in memory.
- Production must store only refresh-token hashes in `auth_refresh_token` and revoke the token family when rotated tokens are replayed.

### Phone Auth

Request SMS code:

```json
{
  "phoneNumber": "+8613800138000",
  "scene": "LOGIN_OR_REGISTER"
}
```

SMS response:

```json
{
  "phoneNumber": "+8613800138000",
  "expiresInSeconds": 300,
  "retryAfterSeconds": 60
}
```

Login/register with code:

```json
{
  "phoneNumber": "+8613800138000",
  "smsCode": "123456"
}
```

Server behavior:

- `requestPhoneCode` validates the normalized `+86` mainland mobile number.
- The server rate-limits requests by phone number and client IP.
- The server rejects repeated requests for the same phone number during the 60-second retry window.
- The server stores a short-lived SMS code and sends it through `SmsCodeSender`.
- JDBC mode records the SMS delivery attempt in `auth_sms_code_attempt` as `sent` or `failed`, with the client address stored only as a hash.
- `loginWithPhone` only succeeds when the code matches an unexpired stored code.
- A successful phone login consumes the code so it cannot be reused.

Production configuration still needed:

- Replace `NoopSmsCodeSender` with a real SMS provider adapter.
- Use `trailmate.auth.sms-code-store.mode=redis` for deployed environments. The Docker Compose deployment already enables this mode.
- Use `trailmate.auth.persistence.mode=jdbc` in deployed environments so account, session, audit, and SMS delivery-attempt evidence are durable.
- `trailmate.auth.sms-code.fixed-code` is only for internal smoke tests before a real SMS provider exists; keep it blank in production.
- Keep the code expiry at 300 seconds unless product/legal review requires a different window.
- See [TrailMate Auth Architecture](../architecture/trailmate-auth-architecture.md) and [TrailMate Auth Database Schema](../database/trailmate-auth-schema.md) for Redis keys and account tables.

### WeChat Auth

Android obtains the WeChat authorization code from the WeChat SDK, then sends it to TrailMate:

```json
{
  "authCode": "wx_auth_code",
  "state": "client_nonce"
}
```

Server behavior:

- `loginWithWechat` delegates auth-code exchange to `WechatAuthClient`.
- The current implementation provides `PreviewWechatAuthClient` for local development.
- HTTP mode calls WeChat Open Platform using the mobile app `appid`, `secret`, received `authCode`, and `grant_type=authorization_code`.
- When WeChat returns an `access_token`, the server calls `/sns/userinfo` and maps `nickname` to `displayName`; if userinfo is unavailable, `displayName` falls back to `微信用户`.
- Persist `wechatOpenId` and, if available, `unionId` as account identity records.
- Set `trailmate.auth.wechat.mode=http`, `trailmate.auth.wechat.app-id`, and `trailmate.auth.wechat.app-secret` in the server runtime environment.

Android integration:

- Set `TRAILMATE_SERVER_BASE_URL` at build time to switch onboarding auth from local preview to HTTP backend calls.
- The Android HTTP client currently calls `/auth/phone/code`, `/auth/phone/login`, `/auth/wechat/login`, `/auth/refresh`, and `/auth/logout`.
- The Android session manager keeps sessions that expire outside the refresh window, rotates tokens through `/auth/refresh` when a session is expired or close to expiry, and clears only the local auth session if refresh is rejected.
- A backend logout success through `/auth/logout` clears only the local auth session; local GPX, route, gear, and activity data remain separate from account sign-out.
- Set `TRAILMATE_WECHAT_APP_ID` at build time to enable the Android WeChat SDK launcher.
- The Android launcher generates a per-request `state`, sends it through the WeChat SDK, and only consumes callbacks whose returned `state` matches the pending request.
- `com.trailmate.app.wxapi.WXEntryActivity` receives WeChat auth callbacks and stores the returned `authCode` for the onboarding auth action to exchange with the backend when the app returns to the foreground.
- After account login and baseline profile collection, Android saves the profile through `PUT /users/me/profile` with the current session `userId` in `X-TrailMate-User-Id`.

### Onboarding Profile

Save request:

```json
{
  "exerciseFrequency": "ONE_TO_TWO_PER_WEEK",
  "typicalDuration": "OVER_60",
  "experienceLevel": "REGULAR",
  "ascentExperience": "M300_TO_800",
  "heightCm": 178,
  "weightKg": 70,
  "commonPackWeightKg": 6
}
```

Response:

```json
{
  "userId": "usr_01J2",
  "exerciseFrequency": "ONE_TO_TWO_PER_WEEK",
  "typicalDuration": "OVER_60",
  "experienceLevel": "REGULAR",
  "ascentExperience": "M300_TO_800",
  "heightCm": 178,
  "weightKg": 70,
  "commonPackWeightKg": 6,
  "updatedAt": "2026-06-23T08:00:00Z"
}
```

Current MVP user resolution accepts `X-TrailMate-User-Id` and falls back to
`local-preview-user` for smoke tests. Production should replace this with the
shared access-token user resolver before account data is treated as secure.

### GPX Import Job

```json
{
  "jobId": "imp_01J2",
  "purpose": "TARGET",
  "status": "PROCESSING",
  "qualityStatus": "PENDING",
  "message": "正在解析 GPX 文件"
}
```

### Assessment

```json
{
  "assessmentId": "asm_01J2",
  "routeId": "rte_01J2",
  "profileId": "pro_01J2",
  "assessmentFingerprint": "rte_01J2#pro_7#assessment-v1",
  "matchScore": 68,
  "matchLevel": "CAUTION",
  "confidenceLevel": "MEDIUM",
  "estimatedMinSeconds": 24000,
  "estimatedMaxSeconds": 28200,
  "riskFactors": [
    {
      "code": "LATE_STAGE_ASCENT",
      "severity": "HIGH",
      "routeStartMeters": 11200,
      "routeEndMeters": 14600,
      "message": "最后阶段仍有连续爬升，可能放大后程速度下降"
    }
  ],
  "algorithmVersion": "assessment-v1"
}
```

### Plan

```json
{
  "planId": "plan_01J2",
  "assessmentId": "asm_01J2",
  "assessmentFingerprint": "rte_01J2#pro_7#assessment-v1",
  "checkpoints": [
    {
      "type": "REST_CHECK",
      "title": "长爬升前检查",
      "distanceMeters": 5200,
      "timeFromStartMinSeconds": 7800,
      "timeFromStartMaxSeconds": 9000,
      "note": "进入连续爬升前检查体力、饮水和能量。"
    }
  ],
  "algorithmVersion": "plan-v1"
}
```

### Gear Catalog

Catalog search:

```json
[
  {
    "catalogItemId": "cat_rain_arcteryx_beta_lt",
    "category": "雨衣（防水透气）",
    "brand": "Arc'teryx",
    "model": "Beta LT Jacket",
    "displayName": "Arc'teryx Beta LT Jacket",
    "weightGrams": 395,
    "tags": ["防水", "硬壳", "暴露路段"],
    "imageUrl": "https://cdn.trailmate.local/gear/arcteryx-beta-lt.png",
    "imageAttribution": "TrailMate catalog seed image",
    "source": "seed"
  }
]
```

Catalog data is server-owned. In JDBC mode it is stored in `gear_catalog_item`.
Product images are not stored as database blobs; the catalog stores `imageUrl`
and optional `imageAttribution`, pointing to object storage, CDN, or a future
image hosting provider.

Route checklist matching reads from the same server-owned catalog. Android does
not expose a user gear inventory API or create brand equipment records from
free-form input. If a brand/model is missing, the catalog must be expanded
server-side first, then the client can show it as a matched candidate.

Matched route checklist candidate:

```json
{
  "requirementId": "req_rain_shell",
  "requirementName": "雨衣（防水透气）",
  "importance": "required",
  "matchStatus": "matched",
  "candidate": {
    "catalogItemId": "cat_rain_arcteryx_beta_lt",
    "brand": "Arc'teryx",
    "model": "Beta LT Jacket",
    "displayName": "Arc'teryx Beta LT Jacket",
    "weightGrams": 395,
    "imageUrl": "https://cdn.trailmate.local/gear/arcteryx-beta-lt.png"
  }
}
```

### Offline Basemap Catalog

PMTiles catalog lookup:

```http
GET /api/v1/offline-basemaps/pmtiles/catalog?minLongitude=120.05&minLatitude=30.10&maxLongitude=120.25&maxLatitude=30.35
```

Response:

```json
[
  {
    "packId": "pmtiles_hangzhou_westlake_osm_v1",
    "regionName": "杭州市 · 西湖区",
    "downloadUrl": "/offline-basemaps/pmtiles/hangzhou-westlake.pmtiles",
    "sizeBytes": 120000000,
    "sha256": null,
    "tileType": "MVT",
    "minZoom": 10,
    "maxZoom": 14,
    "minLongitude": 120.0,
    "minLatitude": 30.05,
    "maxLongitude": 120.3,
    "maxLatitude": 30.4,
    "attribution": "OpenStreetMap contributors",
    "source": "OSM / Protomaps"
  }
]
```

The catalog is server-owned metadata for choosing a suitable PMTiles package.
Android must still download or import the file, validate the PMTiles v3 header,
vector tile type, and route-bounds coverage locally before showing PMTiles as
ready. Invalid bounds return `400` with `code=OFFLINE_BASEMAP_INVALID_BOUNDS`.
If the catalog is unavailable, returns no suitable `MVT` item, or the file at
`downloadUrl` cannot be downloaded or validated, Android falls back to the local
`.pmtiles` document picker.

Current MVP storage is an in-memory seed list. Production should move this
metadata to database-backed catalog rows or managed configuration. PMTiles
binaries should not be stored as PostgreSQL blobs; serve them from object
storage, CDN, or explicit static hosting and keep `downloadUrl` stable.

### Gear Advice

```json
{
  "assessmentFingerprint": "rte_01J2#pro_7#assessment-v1",
  "recommendations": [
    {
      "category": "雨衣（防水透气）",
      "status": "MISSING",
      "rationale": "路线海拔和天气变化可能导致降雨或低温，建议携带可压缩雨衣。"
    }
  ]
}
```

The server must not alter route assessment values in this response. Android must reject responses where `assessmentFingerprint` differs from the current assessment.

## Offline And Retry Rules

Android may show cached profile, assessment, plan, route, and gear checklist while offline. It may also run local fallback rules for route assessment and gear checklist. Android must not mark a server operation as synced, exported, deleted, or AI-complete until the server returns success.
