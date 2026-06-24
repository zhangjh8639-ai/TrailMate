insert into app_user (
    id,
    status,
    display_name,
    locale,
    timezone,
    onboarding_status
) values (
    'local-preview-user',
    'active',
    'TrailMate Preview',
    'zh-CN',
    'Asia/Shanghai',
    'ready'
)
on conflict (id) do update set
    status = excluded.status,
    display_name = excluded.display_name,
    locale = excluded.locale,
    timezone = excluded.timezone,
    onboarding_status = excluded.onboarding_status,
    updated_at = now();
