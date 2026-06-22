# Design: TrailMate Production Mobile UX System

## Product Model

TrailMate is a personal hiking coach for route preparation and light navigation. The Android client should feel field-ready and focused:

- Home starts today's route preparation.
- Route manages imported routes and opens the active route.
- Route Detail answers assessment, navigation, plan, and gear questions for one route.
- Gear manages route-specific preparation and personal equipment.
- Data reviews recorded activities and ability trends.
- Me manages account, profile, permissions, privacy, and map settings.

## Screen Responsibility Matrix

| Surface | Primary Question | Primary Action | Evidence Policy |
| --- | --- | --- | --- |
| Onboarding | Who is using TrailMate? | Save profile or skip | Collect, do not analyze visibly |
| Home | Which route should I prepare? | Import route / continue route | Hide raw evidence |
| Route | What routes do I have? | Open current route / import route | Hide historical details |
| Route Detail - Assessment | Is this route suitable for me? | Go to cockpit or gear | Show summarized rationale only |
| Route Detail - Route | What should I do in the field now? | GPS/start/pause/continue/recover | Hide diagnostics below fold |
| Route Detail - Plan | When should I rest, eat, and check risk? | Review checkpoint timeline | No raw prompt/evidence |
| Route Detail - Gear | Is my gear ready for this route? | Add/match gear | Show user-owned brand data only |
| Gear | What gear do I own and what is missing? | Add/edit gear | Route-relevant only |
| Data | What happened and what did I learn? | Review activity/track | Evidence summarized as activity history |
| Me | How do I manage account and privacy? | Edit settings / export / clear | Evidence counts, not raw bundles |

## Navigation Rules

- Bottom navigation remains five tabs: 首页, 路线, 装备, 数据, 我的.
- Route Detail may be opened from Home, Route, Gear, or Data, but it owns the four route-specific tabs.
- Home never becomes a data management screen.
- Me never becomes a route evaluation screen.
- Data never becomes a settings screen.

## Visual Rules

- Keep light ivory surfaces, deep moss green primary actions, restrained amber warnings, and blue only for location/track/map progress.
- Use map imagery or real route visuals as the first visual signal on route surfaces.
- Use compact cards with 8-16dp radii; avoid card-in-card section layouts.
- Keep one primary CTA per first viewport.
- Use icon + short label for navigation/action controls.

## Implementation Strategy

1. Refactor screen responsibility before adding new features.
2. Extract large screen files into smaller composables by product surface.
3. Preserve existing presentation/domain engines.
4. Update Compose smoke tests around user journeys, not implementation details.
5. Only after the IA is stable, polish motion, spacing, typography, and map visual hierarchy.

## Risks

- Large files can hide unrelated behavior; changes should be small and test-backed.
- Removing visible evidence too aggressively can reduce explainability; keep rationale as summarized expandable content.
- Map-first route UI must still work when AMap is unavailable; keep graceful fallback.
