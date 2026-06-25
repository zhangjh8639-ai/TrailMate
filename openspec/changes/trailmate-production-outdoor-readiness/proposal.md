# Proposal: Production Outdoor Readiness

## Why

TrailMate has moved beyond a static prototype. It now needs to behave like a credible outdoor Android app: route import should lead into assessment, map review, full-screen field navigation, foreground GPS track recording, and quiet post-hike review.

Market expectations from outdoor apps are clear: the user must understand route readiness, offline/map readiness, GPS reliability, and recording state before relying on the app outside. A blank map, unclear permission completion, or hidden field controls is not acceptable for production use.

## What Changes

- Formalize the production mobile user flow from onboarding to route import, assessment, full-screen navigation, recording, and review.
- Require AMap map surfaces to show loading/fallback state instead of unexplained gray blanks.
- Require first-use map/location permissions to complete into the main app reliably.
- Require real foreground GPS recording proof and notification controls.
- Move historical evidence and AI-source language out of primary field screens.
- Add release-grade manual QA criteria for physical-device GPS, offline readiness, and power behavior.

## Out Of Scope

- Turn-by-turn navigation.
- Emergency rescue or dispatch.
- Live safety tracking links.
- New backend account/session implementation.
- National offline map coverage.
- Replacing AMap with another map SDK.
