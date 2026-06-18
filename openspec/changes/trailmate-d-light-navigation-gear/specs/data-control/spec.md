# data-control Specification

## ADDED Requirements

### Requirement: Cloud export shall isolate profile and gear data

Production export for profile and gear data SHALL package account-bound baseline profile, gear inventory, related gear checklist artifacts, and an audit record without including route library, historical GPX activity data, or persisted GPX import jobs in the same export scope.

#### Scenario: Signed-in user exports profile and gear

- GIVEN a signed-in user has saved baseline profile and gear inventory data
- WHEN the user requests a profile and gear export
- THEN the export package includes profile, gear, related gear checklist metadata, and an audit record
- AND the export package excludes imported target routes, historical GPX activities, and GPX import queue jobs
- AND the UI explains that route and GPX exports are separate data scopes

#### Scenario: Signed-out user requests cloud export

- GIVEN the app has only local prototype data and no authenticated account
- WHEN the user requests cloud profile and gear export
- THEN the cloud export is unavailable
- AND the app explains that sign-in is required before account data can be exported

#### Scenario: Pending sync exists during export

- GIVEN profile or gear edits have pending sync or conflict state
- WHEN the signed-in user requests profile and gear export
- THEN the export may proceed with the last known account snapshot
- AND the export is labeled with a warning that unsynced local edits or conflicts may not be represented

### Requirement: Cloud delete shall target profile and gear data without deleting route evidence

Production delete for profile and gear data SHALL remove account-bound baseline profile, gear inventory, gear checklist artifacts, local profile and gear caches, and write an audit tombstone without deleting imported route library records, historical GPX evidence, or persisted GPX import jobs in that same operation.

#### Scenario: Signed-in user deletes profile and gear

- GIVEN a signed-in user confirms deletion of profile and gear data
- WHEN the delete request is accepted
- THEN the deletion plan targets cloud profile records, cloud gear records, related gear checklist artifacts, local profile cache, local gear cache, and an audit tombstone
- AND the operation excludes imported target routes, historical GPX activities, and GPX import queue jobs
- AND the app clearly names the data scopes that will remain

#### Scenario: Signed-out user requests cloud delete

- GIVEN the app has no authenticated account
- WHEN the user requests cloud profile and gear deletion
- THEN the cloud delete operation is unavailable
- AND the app explains that sign-in is required before account data can be deleted

#### Scenario: Pending sync or conflict exists during delete

- GIVEN profile or gear data has unresolved pending sync or conflict state
- WHEN the user requests profile and gear deletion
- THEN the app blocks the cloud delete request
- AND the app requires sync or conflict resolution before deleting account data
