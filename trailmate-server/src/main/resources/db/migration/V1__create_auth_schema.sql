create table app_user (
    id text primary key,
    status text not null default 'active'
        check (status in ('active', 'disabled', 'deleted')),
    display_name text,
    avatar_url text,
    locale text not null default 'zh-CN',
    timezone text not null default 'Asia/Shanghai',
    onboarding_status text not null default 'account_created'
        check (onboarding_status in ('account_created', 'profile_completed', 'ready')),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    disabled_at timestamptz,
    deleted_at timestamptz
);

create index idx_app_user_status on app_user(status);

create table user_phone_identity (
    id text primary key,
    user_id text not null references app_user(id),
    phone_e164 text not null,
    phone_country_code text not null default '+86',
    verified_at timestamptz not null,
    last_login_at timestamptz,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    revoked_at timestamptz,
    constraint uq_user_phone_identity_phone unique (phone_e164),
    constraint chk_user_phone_identity_phone_format check (phone_e164 ~ '^\+861[0-9]{10}$')
);

create index idx_user_phone_identity_user on user_phone_identity(user_id);

create table user_wechat_identity (
    id text primary key,
    user_id text not null references app_user(id),
    app_id text not null,
    open_id text not null,
    union_id text,
    nickname text,
    avatar_url text,
    scope text,
    verified_at timestamptz not null,
    last_login_at timestamptz,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    revoked_at timestamptz,
    constraint uq_user_wechat_identity_openid unique (app_id, open_id)
);

create unique index uq_user_wechat_identity_unionid
    on user_wechat_identity(union_id)
    where union_id is not null and revoked_at is null;

create index idx_user_wechat_identity_user on user_wechat_identity(user_id);

create table auth_refresh_token (
    id text primary key,
    user_id text not null references app_user(id),
    provider text not null
        check (provider in ('phone', 'wechat')),
    token_hash text not null,
    token_family_id text not null,
    previous_token_id text references auth_refresh_token(id),
    device_id text,
    device_name text,
    platform text not null default 'android'
        check (platform in ('android', 'web', 'server_test')),
    issued_at timestamptz not null default now(),
    expires_at timestamptz not null,
    rotated_at timestamptz,
    revoked_at timestamptz,
    revoke_reason text,
    last_used_at timestamptz,
    last_used_ip_hash text,
    user_agent_hash text,
    constraint uq_auth_refresh_token_hash unique (token_hash)
);

create index idx_auth_refresh_token_user_active
    on auth_refresh_token(user_id, expires_at)
    where revoked_at is null;

create index idx_auth_refresh_token_family
    on auth_refresh_token(token_family_id);

create table auth_sms_code_attempt (
    id text primary key,
    phone_e164 text not null,
    scene text not null default 'login_or_register'
        check (scene in ('login_or_register')),
    provider text not null default 'noop',
    delivery_status text not null
        check (delivery_status in ('created', 'sent', 'failed')),
    failure_code text,
    failure_message text,
    request_ip_hash text,
    device_id text,
    created_at timestamptz not null default now(),
    expires_at timestamptz not null
);

create index idx_auth_sms_code_attempt_phone_created
    on auth_sms_code_attempt(phone_e164, created_at desc);

create table user_consent (
    id text primary key,
    user_id text not null references app_user(id),
    consent_type text not null
        check (consent_type in ('privacy_policy', 'terms', 'amap_privacy', 'data_processing')),
    consent_version text not null,
    accepted_at timestamptz not null,
    revoked_at timestamptz,
    source text not null default 'android_onboarding',
    created_at timestamptz not null default now()
);

create unique index uq_user_consent_active
    on user_consent(user_id, consent_type, consent_version)
    where revoked_at is null;

create table auth_audit_event (
    id text primary key,
    user_id text references app_user(id),
    event_type text not null
        check (event_type in (
            'sms_code_requested',
            'phone_login_succeeded',
            'phone_login_failed',
            'wechat_login_succeeded',
            'wechat_login_failed',
            'token_refreshed',
            'logout',
            'refresh_token_replay',
            'account_disabled',
            'account_deleted'
        )),
    provider text
        check (provider is null or provider in ('phone', 'wechat')),
    outcome text not null
        check (outcome in ('success', 'failure')),
    reason_code text,
    phone_e164_hash text,
    wechat_open_id_hash text,
    ip_hash text,
    device_id text,
    user_agent_hash text,
    metadata_json jsonb not null default '{}'::jsonb,
    created_at timestamptz not null default now()
);

create index idx_auth_audit_event_user_created
    on auth_audit_event(user_id, created_at desc);

create index idx_auth_audit_event_type_created
    on auth_audit_event(event_type, created_at desc);
