# Proposal: TrailMate P0 Departure Navigation Gate

## Summary

Make the route cockpit enforce a real pre-departure safety gate before any field navigation or track-recording start. The user may still preview the route and repair missing preparation items, but TrailMate must not present `开始徒步并记录轨迹` while required route, offline route, offline base-map, GPS, or critical gear checks are incomplete.

## Problem

TrailMate already computes departure readiness, but the current route cockpit can still surface the start-hike primary action while the departure summary says a required offline base map is missing or unverified. This weakens the product's first responsibility: helping hikers avoid getting lost or starting without field-critical context.

## Goals

- Block the route cockpit start-hike action until `DepartureReadinessSummary` is complete.
- Route the primary action to the first actionable repair item such as saving the offline route, importing/verifying the offline base map, waiting for stable GPS, or completing route-critical gear.
- Keep full-screen navigation and recording controls unavailable from an unready route.
- Clarify home/route entry copy so route preview and field start are not conflated.

## Non-Goals

- No change to the final offline map provider choice.
- No new emergency dispatch, social sharing, or rescue workflow.
- No route-discovery marketplace.
- No broad UI redesign beyond the P0 navigation gate copy and actions.

