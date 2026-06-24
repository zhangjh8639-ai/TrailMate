# TrailMate Server

Spring Boot service for TrailMate backend APIs.

## Local Run

```powershell
$env:JAVA_HOME='D:\software\Java\jdk17.0.4'
.\gradlew.bat :trailmate-server:bootRun
```

Android can call this server when the app is built with:

```properties
TRAILMATE_SERVER_BASE_URL=http://10.0.2.2:8080
TRAILMATE_WECHAT_APP_ID=wx_your_android_app_id
```

Use `http://10.0.2.2:8080` for the Android emulator and your computer LAN IP for a physical phone on the same network.
`TRAILMATE_WECHAT_APP_ID` must match the mobile app configured in WeChat Open Platform.
The Android app launches WeChat with a generated `state`, receives the callback in `com.trailmate.app.wxapi.WXEntryActivity`, validates that returned `state`, then calls `/api/v1/auth/wechat/login`.
After a backend login succeeds, the Android auth client can rotate the session through `/api/v1/auth/refresh` and revoke it through `/api/v1/auth/logout`.

## Auth Modes

Local development defaults to preview WeChat auth:

```yaml
trailmate:
  auth:
    persistence:
      mode: memory
    wechat:
      mode: preview
```

For a real WeChat Open Platform exchange, configure:

```yaml
trailmate:
  auth:
    wechat:
      mode: http
      app-id: ${TRAILMATE_WECHAT_APP_ID}
      app-secret: ${TRAILMATE_WECHAT_APP_SECRET}
      api-base-url: https://api.weixin.qq.com
```

Phone login currently uses:

- `SmsCodeGenerator` for six-digit code creation.
- `SmsCodeRepository` for short-lived verification storage.
- `SmsCodeSender` for delivery.

Before production, replace `NoopSmsCodeSender` with a real SMS provider and replace `InMemorySmsCodeRepository` with a shared expiring store such as Redis.

## Design Docs

- `docs/architecture/trailmate-auth-architecture.md`: phone/WeChat login architecture, Redis evaluation, token model, and middleware decisions.
- `docs/database/trailmate-auth-schema.md`: PostgreSQL table design for users, phone identity, WeChat identity, refresh tokens, SMS audit, consent, and auth audit events.
- `docs/api/trailmate-server-api.md`: mobile/server API contract used by the Android onboarding auth client.
- `docs/deployment/ubuntu-docker-compose.md`: Ubuntu 22.04 Docker Compose deployment runbook.
- `trailmate-server/src/main/resources/db/migration/V1__create_auth_schema.sql`: initial PostgreSQL auth schema migration.

`JdbcAuthAccountRepository` is implemented and covered by an H2-backed behavior test, but the app still defaults to the in-memory auth repository until `trailmate.auth.persistence.mode=jdbc` and a datasource are configured.
`JdbcAuthSessionIssuer` is also implemented and tested; it persists hashed refresh tokens, rotates old tokens on refresh, and revokes tokens on logout. When `trailmate.auth.persistence.mode=jdbc` is active, `AuthService` uses the JDBC repository and session issuer beans.

To use the JDBC auth persistence path, configure:

```yaml
trailmate:
  auth:
    persistence:
      mode: jdbc
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/trailmate
    username: trailmate
    password: change-me
```

With the Flyway dependency enabled, Spring Boot applies `V1__create_auth_schema.sql` automatically during startup when a datasource is configured.
