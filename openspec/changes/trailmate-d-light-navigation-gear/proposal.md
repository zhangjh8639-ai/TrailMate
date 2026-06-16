# Proposal: TrailMate D Light Navigation And Gear

## Summary

Add the confirmed TrailMate D prototype direction:

- registration-after-login baseline profile questionnaire
- light navigation route experience
- personal gear tab
- AI-assisted route equipment checklist

## Motivation

The base specification already defines a strong personal route assessment loop. The user wants TrailMate to preserve navigation usefulness and to help hikers prepare their equipment for a target route.

This change keeps the MVP focused by separating deterministic route assessment from AI-assisted preparation:

- deterministic services decide match level, confidence, risk, time, and checkpoints
- AI only suggests and explains equipment needs from structured route and gear data

## Scope

In scope:

- short skippable baseline profile after registration/login
- low-confidence temporary profile for users without enough GPX history
- personal gear inventory with optional brand/model fields
- assessment screen with Assessment, Route, Plan, and Gear tabs
- AI-generated equipment checklist after route assessment
- fallback deterministic gear checklist when AI is unavailable

Out of scope:

- AI route scoring
- turn-by-turn navigation
- background GPS tracking
- rescue or emergency promises
- ecommerce, affiliate links, price comparison, or shopping recommendations
- medical advice or exact hydration/nutrition dosages

## Impact

Affected product areas:

- onboarding
- user profile
- target route assessment
- hike plan
- Android UI
- privacy/export/delete behavior
- future AI integration boundary

Affected data areas:

- user baseline profile
- user gear items
- route gear checklist outputs

## Acceptance

The change is accepted when:

- users can complete or skip baseline profile intake
- profile defaults are marked low confidence until GPX evidence exists
- users can record owned branded gear
- route assessment exposes a Gear tab after target route import
- gear recommendations cite route facts and saved gear
- AI output cannot modify route score, risk level, estimate, or checkpoints
- the app remains useful when AI generation fails
