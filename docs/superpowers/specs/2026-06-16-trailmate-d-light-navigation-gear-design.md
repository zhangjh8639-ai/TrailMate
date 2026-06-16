# TrailMate D Light Navigation And Gear Design

Status: approved for design draft on 2026-06-16
Source: `TRAILMATE_CODEX_SPEC.md`
Prototype: `.superpowers/brainstorm/32932-20260616205357/content/trailmate-prototype-direction-standalone.html`

## Design Brief

TrailMate's first Android prototype should follow direction D: personal route assessment plus light navigation, with onboarding that collects baseline user context and a gear layer that helps the user prepare for a route.

The app remains a personal hiking coach, not a route community or a safety guarantee. The core route score, estimated duration, risk segments, and plan checkpoints remain deterministic and reproducible. AI is allowed only for gear-list explanation and preparation guidance after a target route has already been analyzed.

## Product Shape

The first-run flow is:

1. User registers or logs in and accepts privacy terms.
2. User completes or skips a short baseline profile questionnaire.
3. User can add personal gear, including category, brand, model, weight, season, and notes.
4. User imports historical GPX files to replace questionnaire defaults with real capability data.
5. User imports a target route GPX.
6. TrailMate shows assessment, route, plan, and gear tabs.

The main route experience uses four tabs:

- Assessment: match level, confidence, estimated duration range, evidence, and risks.
- Route: GPX line, simplified route view, elevation preview, current checkpoint context, and route-following affordances.
- Plan: ETA ranges, rest checks, energy checks, hydration checks, risk starts, and turnaround checks.
- Gear: AI-assisted equipment checklist grounded in route features and the user's saved gear.

## Baseline Profile Questionnaire

The questionnaire should take about 60-90 seconds and must be skippable. It creates a low-confidence temporary profile for users without enough GPX history.

Fields:

- exercise frequency: rarely, 1-2 times per week, 3+ times per week
- typical exercise duration: under 30 minutes, 30-60 minutes, 60+ minutes
- recent longest hike: distance and duration, optional
- outdoor experience level: beginner, regular, experienced
- ascent experience: under 300 m, 300-800 m, 800 m+
- height in cm, optional
- weight in kg, optional
- common pack weight in kg, optional

Rules:

- Questionnaire defaults must be visibly labeled as low confidence.
- Height and weight may affect conservative pack/load adjustment only when the user opts in.
- The app must not show medical advice, body judgment, calorie claims, hydration dosage, or health diagnosis.
- After three or more usable historical GPX files are available, GPX-derived capability evidence takes precedence over questionnaire defaults.

## Personal Gear Tab

The user's profile area gains a My Gear tab.

Gear item fields:

- category, such as shoes, shell, insulation, pack, headlamp, poles, water, food, first aid, navigation, emergency
- brand, optional
- model, optional
- weight in grams, optional
- season or condition tags, optional
- ownership status: owned, borrowed, planned, retired
- notes, optional

Rules:

- Brand/model data is user-owned personal data, not a shopping or affiliate feature.
- The user can add, edit, delete, and mark gear as available/unavailable.
- Gear is private by default and is never shared.
- Gear suggestions must be framed as preparation checks, not purchase commands.

## AI Gear Advisor

After a target route is imported and assessed, the Gear tab can generate a checklist.

Inputs available to the AI advisor:

- route distance, ascent, descent, max continuous ascent, final-third ascent, highest elevation
- planned start time, planned finish range, and plan checkpoints
- user-entered terrain tags
- match level, confidence level, and risk factors
- user's saved gear categories and item metadata
- user baseline profile at a coarse level, only when needed for pack/load context

Outputs:

- recommended gear category
- status: already covered, check before leaving, missing, optional
- reason tied to route facts, risk segments, duration, or terrain tags
- matching user gear item when available
- suggested non-brand attributes for missing gear, such as waterproof shell, warm layer, headlamp, or trekking poles

AI boundaries:

- AI must not generate route match scores, estimated time, risk severity, or plan checkpoints.
- AI must not claim a route is safe or guaranteed.
- AI must not diagnose medical risk or prescribe exact water, food, or medicine amounts.
- AI should avoid brand recommendations by default. It may display brands the user already saved.
- If AI is unavailable, the route assessment and plan remain usable; the Gear tab shows a simple deterministic essentials checklist and a retry action.

## Light Navigation Boundary

The prototype keeps navigation feel without turning TrailMate into a full navigation product.

Included:

- GPX route line and simplified map/route preview
- elevation profile with highlighted risk segments
- current plan checkpoint context
- foreground route-following affordance when the app has permission and the screen is open
- offline access to the saved plan and route summary

Excluded from MVP:

- turn-by-turn navigation
- nationwide offline basemap
- background GPS tracking
- rescue or emergency dispatch
- guaranteed deviation detection
- voice guidance

V1.1 can add foreground progress, background tracking, deviation reminders, local TTS, and quick events.

## Data And Privacy

New data introduced by this design:

- baseline profile answers
- optional height, weight, and pack weight
- personal gear items
- generated gear checklist records

Privacy rules:

- All new profile and gear data is private by default.
- Users can skip the questionnaire and still use GPX import.
- Users can delete baseline profile data and gear items.
- Export must include baseline profile and gear data once those features ship.
- Logs must not include exact gear notes, full route coordinates, private body data, or AI prompt payloads containing personal data.

## UX States

Baseline profile:

- empty: explain why the short profile helps when GPX data is missing
- in progress: show step count and skip option
- content: show low-confidence temporary profile and next action to import GPX
- recoverable error: save locally and retry sync

Gear advisor:

- empty: ask user to import or assess a target route first
- loading: show that route facts and gear are being checked
- content: grouped checklist with already covered, check, missing, optional
- AI unavailable: show deterministic essentials checklist and retry
- stale: show when route, terrain tags, or gear changed after the last generated checklist

## Design Approval Notes

User-confirmed changes:

- Keep navigation in the product as light route-following and route/plan context.
- Add a registration-after-login questionnaire covering exercise, outdoor experience, height, weight, and pack context.
- Add a personal gear tab.
- After route import, use AI to assess needed equipment and let users add corresponding branded gear.

Open design decision:

- The first implementation plan should decide whether the initial prototype uses only static mock data or wires the baseline profile and gear advisor into persisted local models immediately. The design recommends persisted local models because privacy, export, and delete behavior are central to the feature.
