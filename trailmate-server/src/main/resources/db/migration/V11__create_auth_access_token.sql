create table auth_access_token (
    id text primary key,
    user_id text not null references app_user(id),
    provider text not null
        check (provider in ('phone', 'wechat')),
    token_hash text not null,
    issued_at timestamptz not null default now(),
    expires_at timestamptz not null,
    revoked_at timestamptz,
    constraint uq_auth_access_token_hash unique (token_hash)
);

create index idx_auth_access_token_active
    on auth_access_token(user_id, expires_at)
    where revoked_at is null;
