# TrailMate P0 PMTiles Best Pack Selection

## Why

Offline basemap downloads happen in the field or during pre-trip preparation, where network quality, storage, and battery matter. After route coverage filtering, Android can still receive multiple valid PMTiles packs that fully cover the same route. Selecting the first returned pack can force the user to download a much larger archive than necessary.

## What Changes

- Android remote PMTiles catalog selection filters eligible MVT packs as before.
- When multiple eligible packs fully cover the route, Android chooses the pack with the smallest known positive `sizeBytes`.
- Packs with missing or non-positive `sizeBytes` remain eligible, but rank after packs with known positive sizes.

## Non-Goals

- Do not change the server catalog schema.
- Do not require the server to sort catalog results.
- Do not add multi-pack stitching or split downloads.
