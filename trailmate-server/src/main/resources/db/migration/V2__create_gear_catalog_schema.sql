create table gear_catalog_item (
    catalog_item_id text primary key,
    category text not null,
    brand text not null,
    model text not null,
    display_name text not null,
    weight_grams integer
        check (weight_grams is null or weight_grams >= 0),
    tags_csv text not null default '',
    image_url text,
    image_attribution text,
    source text not null default 'seed',
    active boolean not null default true,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    constraint uq_gear_catalog_brand_model unique (category, brand, model)
);

create index idx_gear_catalog_item_category
    on gear_catalog_item(category)
    where active = true;

create index idx_gear_catalog_item_brand_model
    on gear_catalog_item(brand, model)
    where active = true;

create table user_gear_inventory (
    inventory_item_id text primary key,
    user_id text not null references app_user(id),
    catalog_item_id text references gear_catalog_item(catalog_item_id),
    custom_category text,
    custom_brand text,
    custom_model text,
    custom_display_name text,
    custom_weight_grams integer
        check (custom_weight_grams is null or custom_weight_grams >= 0),
    available boolean not null default true,
    custom boolean not null default false,
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now(),
    deleted_at timestamptz,
    constraint chk_user_gear_inventory_catalog_or_custom check (
        (custom = false and catalog_item_id is not null)
        or
        (custom = true and catalog_item_id is null and custom_category is not null and custom_model is not null)
    )
);

create unique index uq_user_gear_inventory_catalog_active
    on user_gear_inventory(user_id, catalog_item_id)
    where catalog_item_id is not null and deleted_at is null;

create index idx_user_gear_inventory_user_active
    on user_gear_inventory(user_id, updated_at desc)
    where deleted_at is null;

insert into gear_catalog_item (
    catalog_item_id, category, brand, model, display_name,
    weight_grams, tags_csv, image_url, image_attribution, source
) values
(
    'cat_rain_arcteryx_beta_lt',
    '雨衣（防水透气）',
    'Arc''teryx',
    'Beta LT Jacket',
    'Arc''teryx Beta LT Jacket',
    395,
    '防水,硬壳,暴露路段',
    'https://cdn.trailmate.local/gear/arcteryx-beta-lt.png',
    'TrailMate catalog seed image',
    'seed'
),
(
    'cat_headlamp_bd_spot_400',
    '头灯',
    'Black Diamond',
    'Spot 400',
    'Black Diamond Spot 400',
    78,
    '夜间,备用电池,安全',
    'https://cdn.trailmate.local/gear/black-diamond-spot-400.png',
    'TrailMate catalog seed image',
    'seed'
),
(
    'cat_poles_leki_legacy_lite',
    '登山杖',
    'Leki',
    'Legacy Lite AS',
    'Leki Legacy Lite AS',
    510,
    '长距离,下坡,稳定',
    'https://cdn.trailmate.local/gear/leki-legacy-lite-as.png',
    'TrailMate catalog seed image',
    'seed'
),
(
    'cat_insulation_montbell_plasma',
    '保温层（抓绒/羽绒）',
    'Montbell',
    'Plasma 1000 Down Jacket',
    'Montbell Plasma 1000 Down Jacket',
    130,
    '保暖,轻量,高海拔',
    'https://cdn.trailmate.local/gear/montbell-plasma-1000.png',
    'TrailMate catalog seed image',
    'seed'
),
(
    'cat_water_source_hydrapak_2l',
    '备用水',
    'Hydrapak',
    'Seeker 2L',
    'Hydrapak Seeker 2L',
    76,
    '补水,长距离,可压缩',
    'https://cdn.trailmate.local/gear/hydrapak-seeker-2l.png',
    'TrailMate catalog seed image',
    'seed'
)
on conflict (catalog_item_id) do update set
    category = excluded.category,
    brand = excluded.brand,
    model = excluded.model,
    display_name = excluded.display_name,
    weight_grams = excluded.weight_grams,
    tags_csv = excluded.tags_csv,
    image_url = excluded.image_url,
    image_attribution = excluded.image_attribution,
    source = excluded.source,
    active = true,
    updated_at = now();
