create table gear_advice_artifact (
    artifact_id text primary key,
    user_id text not null references app_user(id),
    plan_id text not null,
    assessment_fingerprint text not null,
    recommendations_payload text not null,
    created_at timestamptz not null default now()
);

create index idx_gear_advice_artifact_user_plan_created
    on gear_advice_artifact(user_id, plan_id, created_at desc);

create index idx_gear_advice_artifact_fingerprint
    on gear_advice_artifact(assessment_fingerprint);
