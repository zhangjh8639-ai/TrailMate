# TrailMate OpenSpec Project

TrailMate is an Android-first hiking coach app. Its core loop is:

Import historical GPX -> build personal capability profile -> import target route GPX -> assess route fit -> generate plan checkpoints -> support light route following -> gather feedback.

## Product Constraints

- Android first.
- GPX first.
- No community, marketplace, paid membership, social feed, emergency dispatch, or iOS in MVP.
- Core route assessment must be deterministic, explainable, and reproducible.
- AI may explain or advise only from structured inputs. AI must not generate route scores, estimated durations, risk severity, or plan checkpoints.
- Position and route data is private by default.
- Data collection should be minimal and tied to user-visible value.

## OpenSpec Workflow

Changes live under `openspec/changes/<change-id>/`.

Each change should include:

- `proposal.md`: what and why
- `design.md`: product and technical design
- `tasks.md`: implementation checklist
- `specs/<capability>/spec.md`: requirement deltas with scenarios

The current environment does not have the `openspec` CLI installed, so these files are local OpenSpec-compatible drafts. Run CLI validation after the project toolchain is initialized.
