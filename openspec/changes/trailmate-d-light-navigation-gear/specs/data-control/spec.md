# data-control Specification

## ADDED Requirements

### Requirement: Cloud export shall isolate profile and gear-advisor data

Production export for profile and gear-advisor data SHALL package account-bound baseline profile, related gear checklist artifacts, and an audit record without including server-owned gear catalog data, route library, historical GPX activity data, or persisted GPX import jobs in the same export scope.

#### Scenario: Signed-in user exports profile and gear-advisor artifacts

- GIVEN a signed-in user has saved baseline profile data and generated gear checklist artifacts
- WHEN the user requests a profile and gear-advisor export
- THEN the export package includes profile, related gear checklist metadata, and an audit record
- AND the export package excludes server-owned gear catalog data, imported target routes, historical GPX activities, and GPX import queue jobs
- AND the UI explains that route and GPX exports are separate data scopes

#### Scenario: Signed-out user requests cloud export

- GIVEN the app has only local prototype data and no authenticated account
- WHEN the user requests cloud profile and gear-advisor export
- THEN the cloud export is unavailable
- AND the app explains that sign-in is required before account data can be exported

#### Scenario: Pending sync exists during export

- GIVEN profile or gear-advisor artifacts have pending sync or conflict state
- WHEN the signed-in user requests profile and gear-advisor export
- THEN the export may proceed with the last known account snapshot
- AND the export is labeled with a warning that unsynced local edits or conflicts may not be represented

### Requirement: Cloud delete shall target profile and gear-advisor data without deleting route evidence

Production delete for profile and gear-advisor data SHALL remove account-bound baseline profile, gear checklist artifacts, local profile and checklist caches, and write an audit tombstone without deleting server-owned gear catalog data, imported route library records, historical GPX evidence, or persisted GPX import jobs in that same operation.

#### Scenario: Signed-in user deletes profile and gear-advisor data

- GIVEN a signed-in user confirms deletion of profile and gear-advisor data
- WHEN the delete request is accepted
- THEN the deletion plan targets cloud profile records, related gear checklist artifacts, local profile cache, local checklist cache, and an audit tombstone
- AND the operation excludes server-owned gear catalog data, imported target routes, historical GPX activities, and GPX import queue jobs
- AND the app clearly names the data scopes that will remain

#### Scenario: Signed-out user requests cloud delete

- GIVEN the app has no authenticated account
- WHEN the user requests cloud profile and gear-advisor deletion
- THEN the cloud delete operation is unavailable
- AND the app explains that sign-in is required before account data can be deleted

#### Scenario: Pending sync or conflict exists during delete

- GIVEN profile or gear-advisor data has unresolved pending sync or conflict state
- WHEN the user requests profile and gear-advisor deletion
- THEN the app blocks the cloud delete request
- AND the app requires sync or conflict resolution before deleting account data
