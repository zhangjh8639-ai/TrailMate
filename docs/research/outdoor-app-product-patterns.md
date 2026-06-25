# Outdoor App Product Patterns

Date: 2026-06-19

Patterns that should shape TrailMate's production UX:

- Outdoor navigation apps make offline readiness explicit before field use. AllTrails highlights offline maps, wrong-turn alerts, route following, and condition planning as premium trail-confidence features.
- komoot centers the navigation flow around a planned route, offline maps, turn-by-turn guidance, and staying useful without reception.
- Gaia GPS frames the mobile app around seeing your location, downloading maps for offline use, recording tracks, and planning/reviewing trips across mobile and web.
- 2026 official help pages reinforce the same pattern: AllTrails supports area-based offline downloads for phone GPS navigation without data signal, Gaia GPS documents offline map use for being out of cell service and recommends testing with airplane mode, and komoot recommends downloading regions that cover the entire route area including start, finish, and return.
- Strava Beacon shows a safety pattern worth adapting later: during recording, the user can share a real-time location link with trusted contacts.
- Safety sharing belongs inside the recording/navigation context, not in a generic profile setting. The first production-ready TrailMate step can be a local Android share-sheet action with route name, current coordinates, map link, and recorded distance; account-based live links can come later.

TrailMate implication:

- Light navigation should not jump straight from map preview to `开始徒步`; it needs a compact pre-departure check that combines route geometry, offline route pack, GPS availability, and route-critical gear.
- Once GPS is available, light navigation should expose a visible `安全分享` affordance so users can quickly send their location to a trusted person before or during a hike.
- Real navigation tools need to explain GPS quality, not just show a blue dot. TrailMate should show accuracy, route-matching availability, and last update time so users know when route checks are safe to trust.
- Map-heavy hiking flows should explain active layers, not only draw them. TrailMate should label planned route, checkpoint hints, recorded track, current location, and base-map mode so users understand what each line or marker represents.
- GPX import and offline base maps must stay separate in both UX and release gates. GPX preserves the intended route geometry; target-region offline base maps preserve roads, junctions, place names, water features, terrain context, and retreat options when mobile data is weak or unavailable.
- Wrong-turn or off-route feedback should be conservative for hiking: stop automatic progress, ask the user to confirm direction with map/signage/terrain, and make safety sharing visible instead of silently recalculating a new line.
- When GPS returns to the planned route after a wrong-turn warning, TrailMate should confirm the route has been rejoined and ask the user to verify the next checkpoint before normal light navigation continues.
- Safety sharing should include enough trip context for a non-hiking contact: route distance, ascent, expected finish time when available, current location, and a clear overdue-contact instruction.
- The same safety-plan details should be visible before sharing, so hikers can quickly verify what their contact will receive.

References:

- AllTrails product page: https://www.alltrails.com/welcome
- AllTrails Offline Areas: https://support.alltrails.com/hc/en-us/articles/37758009767444-Download-custom-areas-for-offline-use
- AllTrails offline map navigation: https://support.alltrails.com/hc/en-us/articles/37213318235028-How-to-download-maps-to-your-phone-for-offline-use
- AllTrails Android GPS troubleshooting: https://support.alltrails.com/hc/en-us/articles/360019246391-Resolving-GPS-errors-on-Android
- komoot features page: https://www.komoot.com/features
- komoot offline routes and maps: https://support.komoot.com/hc/en-us/articles/10356476920986-Download-routes-and-maps-for-offline-use
- Gaia GPS help overview: https://help.gaiagps.com/hc/en-us/articles/9067661557399-How-to-Use-Gaia-GPS
- Gaia GPS offline maps: https://help.gaiagps.com/hc/en-us/articles/360047131513-Download-Maps-for-Offline-Use
- Strava Beacon help: https://support.strava.com/hc/en-us/articles/224357527-Strava-Beacon
- AMap Android map display lifecycle: https://lbs.amap.com/api/android-sdk/guide/create-map/show-map
- AMap Android polyline drawing: https://lbs.amap.com/api/android-sdk/guide/draw-on-map/draw-polyline
- AMap Android map controls: https://lbs.amap.com/api/android-sdk/guide/interaction-with-map/control-interaction
