create table user_onboarding_profile (
    user_id text primary key references app_user(id),
    exercise_frequency text not null
        check (exercise_frequency in ('RARELY', 'ONE_TO_TWO_PER_WEEK', 'THREE_PLUS_PER_WEEK')),
    typical_duration text not null
        check (typical_duration in ('UNDER_30', 'MIN_30_TO_60', 'OVER_60')),
    experience_level text not null
        check (experience_level in ('BEGINNER', 'REGULAR', 'EXPERIENCED')),
    ascent_experience text not null
        check (ascent_experience in ('UNDER_300', 'M300_TO_800', 'OVER_800')),
    height_cm integer
        check (height_cm is null or (height_cm >= 80 and height_cm <= 230)),
    weight_kg integer
        check (weight_kg is null or (weight_kg >= 25 and weight_kg <= 250)),
    common_pack_weight_kg integer
        check (common_pack_weight_kg is null or (common_pack_weight_kg >= 0 and common_pack_weight_kg <= 60)),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

create index idx_user_onboarding_profile_updated
    on user_onboarding_profile(updated_at desc);
