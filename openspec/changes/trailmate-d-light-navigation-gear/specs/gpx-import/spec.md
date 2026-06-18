# gpx-import Specification

## ADDED Requirements

### Requirement: GPX imports shall use a persistent retryable job queue

Target-route and historical-activity GPX imports SHALL be represented as persisted jobs before parsing starts.

#### Scenario: User selects a GPX file for import

- GIVEN a signed-in or local-only prototype user selects a GPX file
- WHEN the app accepts the file for target-route or historical-activity import
- THEN the app creates a GPX import job with id, kind, source URI, file name, status, attempt count, retry budget, timestamps, and optional last error
- AND the job is saved before local parsing starts

#### Scenario: Import worker claims at most one runnable job

- GIVEN the GPX import queue contains multiple queued jobs or due retry jobs
- WHEN the import worker claims a runnable job
- THEN only that claimed job is marked running
- AND other runnable jobs remain queued or waiting retry until the running job reaches a terminal or retry-waiting state
- AND parsing is performed only for the job that was actually marked running

#### Scenario: Import job fails with retry budget remaining

- GIVEN a GPX import job is running
- WHEN parsing, content access, or persistence fails before the retry budget is exhausted
- THEN the app records the failure reason
- AND marks the job as waiting for retry
- AND records the next retry time without deleting any previously valid route or history data

#### Scenario: App restarts while a job was running

- GIVEN the persisted queue contains a running job whose update timestamp is older than the allowed running timeout
- WHEN the app restores the queue after process death or startup recovery
- THEN the app marks the interrupted job waiting retry if retry budget remains
- OR marks it failed if the retry budget is exhausted
- AND the interrupted job no longer blocks later runnable imports forever

#### Scenario: Retry time has not arrived

- GIVEN a GPX import job is waiting for retry
- WHEN the import worker checks the queue before the next retry time
- THEN the job is not restarted
- AND the previous route or history state remains available

#### Scenario: Retry time arrives

- GIVEN a GPX import job is waiting for retry
- WHEN the import worker checks the queue at or after the next retry time
- THEN the job can be marked running
- AND the attempt count increases
- AND stale retry error text is cleared for the running attempt

#### Scenario: Retry budget is exhausted

- GIVEN a GPX import job has reached its maximum attempt count
- WHEN the latest attempt fails
- THEN the job is marked failed
- AND automatic retry is disabled until the user or a future manual action creates a new queued job

#### Scenario: Import job succeeds

- GIVEN a GPX import job is running
- WHEN parsing and persistence succeed
- THEN the job is marked succeeded
- AND retry metadata and last error are cleared
- AND the parsed target route or historical activity evidence is saved through its own data boundary
