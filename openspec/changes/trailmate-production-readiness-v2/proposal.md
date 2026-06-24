# Proposal: TrailMate Production Readiness V2

## Summary

Upgrade TrailMate from a local Android prototype into a more production-ready hiking workflow. This change focuses on three maturity gaps called out by the user:

- Login and onboarding should feel like a real private account flow.
- Route and full-screen navigation should not mix preparation, diagnostics, and field controls.
- Gear should be selected from a server-backed catalog rather than primarily typed by the user.

## Problem

The current app has strong technical pieces, but the product contract is still immature:

- The auth page still feels like a combined prototype form and setup screen.
- Route detail and full-screen navigation can expose too much preparation or diagnostic context.
- Gear screens let users type category, brand, and model as the main path, which does not match the intended service-backed product.
- The server API has auth endpoints and gear advice, but it does not yet expose a catalog of known gear items for Android selection.

## Goals

- Make account creation, baseline profile, route preparation, gear selection, and field navigation form a coherent user journey.
- Introduce server-owned gear catalog APIs for brand/model/thumbnail data.
- Make Android gear preparation match route needs directly against catalog items instead of prioritizing free-form entry or personal inventory management.
- Keep full-screen navigation field-focused.
- Define true-device and deployed-server acceptance checks.

## Non-Goals

- No marketplace, affiliate shopping, prices, or checkout.
- No community route discovery.
- No full turn-by-turn navigation.
- No rescue, emergency dispatch, or safety guarantee.
- No iOS work.

## Design Source

Use `docs/superpowers/specs/2026-06-23-trailmate-production-readiness-v2-design.md` as the product design source.
